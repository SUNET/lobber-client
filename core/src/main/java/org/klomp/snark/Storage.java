/*
 * Storage - Class used to store and retrieve pieces. Copyright (C) 2003 Mark J.
 * Wielaard
 * 
 * This file is part of Snark.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.klomp.snark;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Maintains pieces on disk. Can be used to store and retrieve pieces.
 */
public class Storage
{
    private MetaInfo metainfo;

    private List<FileRef> refs;

    private final StorageListener listener;

    private final BitField bitfield;

    private int needed;

    // XXX - Not always set correctly
    int piece_size;

    int pieces;

    /** The default piece size. */
    private static int MIN_PIECE_SIZE = 256 * 1024;

    /** The maximum number of pieces in a torrent. */
    private static long MAX_PIECES = 100 * 1024 / 20;

    /**
     * Creates a new storage based on the supplied MetaInfo. This will try to
     * create and/or check all needed files in the MetaInfo.
     * 
     * @exception IOException
     *                when creating and/or checking files fails.
     */
    public Storage (MetaInfo metainfo, StorageListener listener)
        throws IOException
    {
        this.metainfo = metainfo;
        this.listener = listener;
        needed = metainfo.getPieces();
        bitfield = new BitField(needed);
    }

    /**
     * Creates a storage from the existing file or directory together with an
     * appropriate MetaInfo file as can be announced on the given announce
     * String location.
     */
    public Storage (File baseFile, String announce, StorageListener listener)
        throws IOException
    {
        this.listener = listener;

        // Create names, rafs and lengths arrays.
        getFiles(baseFile);

        long total = 0;
        ArrayList<Long> lengthsList = new ArrayList<Long>();
        List<List<String>> files = new ArrayList<List<String>>();
        for (FileRef ref : refs) {
            total += ref.getLength();
            lengthsList.add(new Long(ref.getLength()));
            List<String> file = new ArrayList<String>();
            StringTokenizer st = new StringTokenizer(ref.getPath(), File.separator);
            while (st.hasMoreTokens()) {
                String part = st.nextToken();
                file.add(part);
            }
            files.add(file);
        }

        piece_size = MIN_PIECE_SIZE;
        pieces = (int)((total - 1) / piece_size) + 1;
        while (pieces > MAX_PIECES) {
            piece_size = piece_size * 2;
            pieces = (int)((total - 1) / piece_size) + 1;
        }

        // Note that piece_hashes and the bitfield will be filled after
        // the MetaInfo is created.
        byte[] piece_hashes = new byte[20 * pieces];
        bitfield = new BitField(pieces);
        needed = 0;

        if (files.size() == 1) {
            files = null;
            lengthsList = null;
        }

        // Note that the piece_hashes are not correctly setup yet.
        metainfo = new MetaInfo(announce, baseFile.getName(), files,
            lengthsList, piece_size, piece_hashes, total);

    }

    // Creates piece hases for a new storage.
    public void create () throws IOException
    {
        // Calculate piece_hashes
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException nsa) {
            throw new InternalError(nsa.toString());
        }

        byte[] piece_hashes = metainfo.getPieceHashes();
        
        byte[] piece = new byte[piece_size];
        for (int i = 0; i < pieces; i++) {
        	listener.storateGetPiece(this, i);
            int length = getUncheckedPiece(i, piece, 0);
            listener.storateGetPiece(this, i);
            digest.update(piece, 0, length);
            byte[] hash = digest.digest();
            for (int j = 0; j < 20; j++) {
                piece_hashes[20 * i + j] = hash[j];
            }

            bitfield.set(i);

            if (listener != null) {
                listener.storageChecked(this, i, true);
            }
        }

        if (listener != null) {
            listener.storageAllChecked(this);
        }

        // Reannounce to force recalculating the info_hash.
        metainfo = metainfo.reannounce(metainfo.getAnnounce());
    }

    private void getFiles (File base) throws IOException
    {
        refs = new ArrayList<FileRef>();
        findFiles(refs, base);
    }

    private void findFiles(List<FileRef> l, File f) { 	
    	if (!f.isDirectory()) {
            l.add(new FileRef(f));
        } else {
            File[] files = f.listFiles();
            if (files == null) {
                log.log(Level.WARNING, "Skipping '" + f
                    + "' not a normal file.");
                return;
            }
            for (File element : files) {
                findFiles(l, element);
            }
        }
    }

    /**
     * Returns the MetaInfo associated with this Storage.
     */
    public MetaInfo getMetaInfo ()
    {
        return metainfo;
    }

    /**
     * How many pieces are still missing from this storage.
     */
    public int needed ()
    {
        return needed;
    }

    /**
     * Whether or not this storage contains all pieces if the MetaInfo.
     */
    public boolean complete ()
    {
        return needed == 0;
    }

    /**
     * The BitField that tells which pieces this storage contains. Do not change
     * this since this is the current state of the storage.
     */
    public BitField getBitField ()
    {
        return bitfield;
    }

    /**
     * Creates (and/or checks) all files from the metainfo file list.
     */
    public void check () throws IOException
    {
        File base = new File(filterName(metainfo.getName()));

        List<List<String>> files = metainfo.getFiles();
        if (files == null) {
            // Create base as file.
            log.log(Level.INFO, "Creating/Checking file: " + base);
            if (!base.createNewFile() && !base.exists()) {
                throw new IOException("Could not create file " + base);
            }
            
            refs = new ArrayList<FileRef>(1);
            refs.add(new FileRef(base.getName(),metainfo.getTotalLength()));
            
//            lengths = new long[1];
//            rafs = new RandomAccessFile[1];
//            names = new String[1];
//            lengths[0] = metainfo.getTotalLength();
//            rafs[0] = new RandomAccessFile(base, "rw");
//            names[0] = base.getName();
        } else {
            // Create base as dir.
            log.log(Level.INFO, "Creating/Checking directory: " + base);
            if (!base.mkdir() && !base.isDirectory()) {
                throw new IOException("Could not create directory " + base);
            }

            List<Long> ls = metainfo.getLengths();
            int size = files.size();
            long total = 0;
            refs = new ArrayList<FileRef>(size);
            
            for (int i = 0; i < size; i++) {
                File f = createFileFromNames(base, files.get(i));
                FileRef ref = new FileRef(f.getAbsolutePath(),ls.get(i).longValue());
                total += ref.getLength();
            }

            // Sanity check for metainfo file.
            long metalength = metainfo.getTotalLength();
            if (total != metalength) {
                throw new IOException("File lengths do not add up " + total
                    + " != " + metalength);
            }
        }
        checkCreateFiles();
    }

    /**
     * Removes 'suspicious' characters from the give file name.
     */
    private String filterName (String name)
    {
        // XXX - Is this enough?
        return name.replace(File.separatorChar, '_');
    }

    private File createFileFromNames (File base, List<String> names) throws IOException
    {
        File f = null;
        Iterator<String> it = names.iterator();
        while (it.hasNext()) {
            String name = filterName((String)it.next());
            if (it.hasNext()) {
                // Another dir in the hierarchy.
                f = new File(base, name);
                if (!f.mkdir() && !f.isDirectory()) {
                    throw new IOException("Could not create directory " + f);
                }
                base = f;
            } else {
                // The final element (file) in the hierarchy.
                f = new File(base, name);
                if (!f.createNewFile() && !f.exists()) {
                    throw new IOException("Could not create file " + f);
                }
            }
        }
        return f;
    }

    private void checkCreateFiles () throws IOException
    {
        // Whether we are resuming or not,
        // if any of the files already exists we assume we are resuming.
        boolean resume = false;

        // Make sure all files are available and of correct length
        for (FileRef ref : refs) {
            long length = ref.getLength();
            RandomAccessFile rafs = ref.getRandomAccessFile("rw");
            if (length == rafs.length()) {
                if (listener != null) {
                    listener.storageAllocated(this, length);
                }
                resume = true; // XXX Could dynamicly check
            } else if (length == 0) {
                allocateFile(ref);
            } else {
                log.log(Level.FINE, "Truncating '" + ref.getName()
                    + "' from " + rafs.length() + " to " + length + "bytes");
                rafs.setLength(length);
                allocateFile(ref);
            }
            ref.close();
        }

        // Check which pieces match and which don't
        if (resume) {
            pieces = metainfo.getPieces();
            byte[] piece = new byte[metainfo.getPieceLength(0)];
            for (int i = 0; i < pieces; i++) {
                int length = getUncheckedPiece(i, piece, 0);
                boolean correctHash = metainfo.checkPiece(i, piece, 0, length);
                if (correctHash) {
                    bitfield.set(i);
                    needed--;
                }

                if (listener != null) {
                    listener.storageChecked(this, i, correctHash);
                }
            }
        }

        if (listener != null) {
            listener.storageAllChecked(this);
        }
    }

    private void allocateFile (FileRef ref) throws IOException
    {
        // XXX - Is this the best way to make sure we have enough space for
        // the whole file?
    	long length = ref.getLength();
        listener.storageCreateFile(this, ref.getName(), length);
        final int ZEROBLOCKSIZE = metainfo.getPieceLength(0);
        byte[] zeros = new byte[ZEROBLOCKSIZE];
        int i;
        for (i = 0; i < length / ZEROBLOCKSIZE; i++) {
            ref.getRandomAccessFile("rw").write(zeros);
            if (listener != null) {
                listener.storageAllocated(this, ZEROBLOCKSIZE);
            }
        }
        int size = (int)(length - i * ZEROBLOCKSIZE);
        ref.getRandomAccessFile("rw").write(zeros, 0, size);
        if (listener != null) {
            listener.storageAllocated(this, size);
        }
    }

    /**
     * Closes the Storage and makes sure that all RandomAccessFiles are closed.
     * The Storage is unusable after this.
     */
    public void close () throws IOException
    {
        for (FileRef ref : refs) {
            synchronized (ref) {
                ref.close();
            }
        }
    }

    /**
     * Returns a byte array containing the requested piece or null if the
     * storage doesn't contain the piece yet.
     */
    public byte[] getPiece (int piece) throws IOException
    {
        if (!bitfield.get(piece)) {
            return null;
        }
        
        if (listener != null) {
        	listener.storateGetPiece(this, piece);
        }

        byte[] bs = new byte[metainfo.getPieceLength(piece)];
        getUncheckedPiece(piece, bs, 0);
        return bs;
    }

    /**
     * Put the piece in the Storage if it is correct.
     * 
     * @return true if the piece was correct (sha metainfo hash matches),
     *         otherwise false.
     * @exception IOException
     *                when some storage related error occurs.
     */
    public boolean putPiece (int piece, byte[] bs) throws IOException
    {
        // First check if the piece is correct.
        // If we were paranoia we could copy the array first.
        int length = bs.length;
        boolean correctHash = metainfo.checkPiece(piece, bs, 0, length);
        if (listener != null) {
            listener.storageChecked(this, piece, correctHash);
        }
        if (!correctHash) {
            return false;
        }

        synchronized (bitfield) {
            if (bitfield.get(piece)) {
                return true; // No need to store twice.
            } else {
                bitfield.set(piece);
                needed--;
            }
        }

        long start = piece * metainfo.getPieceLength(0);
        int i = 0;
        FileRef ref = refs.get(i);
        long raflen = ref.getLength();
        while (start > raflen) {
            start -= raflen;
            ref = refs.get(++i);
            raflen = ref.getLength();
        }
        
        int written = 0;
        int off = 0;
        while (written < length) {
            int need = length - written;
            int len = (start + need < raflen) ? need : (int)(raflen - start);
            ref.seekAndWrite(bs, start, off+written, len);
            written += len;
            if (need - len > 0) {
                ref = refs.get(++i);
                raflen = ref.getLength();
                start = 0;
            }
        }

        return true;
    }

    private FileRef findPieceRef(int piece) {
    	long start = piece * metainfo.getPieceLength(0);
        int length = metainfo.getPieceLength(piece);
        int i = 0;
        FileRef ref = refs.get(i);
        long raflen = ref.getLength();
        while (start > raflen) {
        	start -= raflen;
        	ref = refs.get(++i);
            raflen = ref.getLength();
        }
        
        return ref;
    }
    
    private int getUncheckedPiece (int piece, byte[] bs, int off)
        throws IOException
    {
    	long start = piece * metainfo.getPieceLength(0);
    	int length = metainfo.getPieceLength(piece);
    	listener.message("start at "+start);
    	listener.message("read "+length+" bytes");
        int i = 0;
        FileRef ref = refs.get(i);
        
        listener.message("start ref("+i+")="+ref);
        
        long raflen = ref.getLength();
        while (start > raflen) {
            start -= raflen;
            ref = refs.get(++i);
            raflen = ref.getLength();
        }
        
        listener.message("seeked to ref("+i+")="+ref+" @ "+start);

        int read = 0;
        while (read < length) {
        	listener.message("read: ("+i+") "+read+"/"+length);
            int need = length - read;
            listener.message("need: "+need+" bytes from "+raflen+" bytes");
            int len = (start + need < raflen) ? need : (int)(raflen - start);
            listener.message("seekAndRead "+start+" -> "+(off+read)+" "+len+" bytes");
            ref.seekAndRead(bs, start, off+read, len);
            
//            try {
//    			RandomAccessFile rafs = ref.getRandomAccessFile("rw");
//    			synchronized (rafs) {
//	    			listener.message(rafs);
//	    			rafs.seek(start);
//	    			listener.message("seek to "+start);
//	    			rafs.readFully(bs,off+read,len);
//	    			listener.message("read "+len+" bytes");
//	    			ref.close();
//    			}
//    		} catch (IOException ex) {
//    			ref.close();
//    			throw ex;
//    		}
            
            read += len;
            if (need - len > 0) {
                ref = refs.get(++i);
                raflen = ref.getLength();
                start = 0;
            }
        }

        return length;
    }

    /** The Java logger used to process our log events. */
    protected static final Logger log = Logger.getLogger("org.klomp.snark.Storage");
}
