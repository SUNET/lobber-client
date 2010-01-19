/*
 * Snark - Main snark program startup class. Copyright (C) 2003 Mark J. Wielaard
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sbbi.upnp.impls.InternetGatewayDevice;

/**
 * Main Snark object used to fetch or serve a given file.
 * 
 * @author Mark Wielaard (mark@klomp.org)
 */
public class SnarkSeeder
{
    /** The lowest port Snark will listen on for connections */
    public final static int MIN_PORT = 46881;

    /** The highest port Snark will listen on for connections */
    public final static int MAX_PORT = 47889;

    private String announce;
    
    /** The file being torrented */
    public File path;

    /** The metadata known about the torrent */
    public MetaInfo meta;

    /** The storage helper assisting us */
    public Storage storage;

    /** The coordinator managing our peers */
    public PeerCoordinator coordinator;

    /** Parcels out incoming requests to the appropriate places */
    public ConnectionAcceptor acceptor;

    /** Obtains information on new peers. */
    public TrackerClient trackerclient;

    private MessageListener mlistener;
    
    private InternetGatewayDevice[] igds;
    
    private boolean needsNATMapping;
    
    /**
     * Constructs a Snark client.
     * @param path The address of the torrent to download or file to serve
     * @param ip The IP address to use when serving data
     * @param user_port The port number to use
     * @param announce The announce url to use
     * @param slistener A custom {@link StorageListener} to use
     * @param clistener A custom {@link CoordinatorListener} to use
     */
    public SnarkSeeder (File path, String[] ip, boolean needsNATMapping, int user_port, String announce,
        StorageListener slistener, CoordinatorListener clistener, MessageListener mlistener)
    {
        this.slistener = slistener;
        this.clistener = clistener;
        this.path = path;
        this.user_port = user_port;
        this.ip = ip;
        this.announce = announce;
        this.mlistener = mlistener;
        this.needsNATMapping = needsNATMapping;
        
        // Create a new ID and fill it with something random. First nine
        // zeros bytes, then three bytes filled with snark and then
        // sixteen random bytes.
        Random random = new Random();
        int i;
        for (i = 0; i < 9; i++) {
            id[i] = 0;
        }
        id[i++] = snark;
        id[i++] = snark;
        id[i++] = snark;
        while (i < 20) {
            id[i++] = (byte)random.nextInt(256);
        }

        log.log(Level.FINE, "My peer id: " + PeerID.idencode(id));
    }

    /**
     * Sets the global logging level of Snark.
     */
    public static void setLogLevel (Level level)
    {
        log.setLevel(level);
        log.setUseParentHandlers(false);
        Handler handler = new ConsoleHandler();
        handler.setLevel(level);
        log.addHandler(handler);
    }

    /**
     * Returns a human-readable state of Snark.
     */
    public String getStateString ()
    {
        return activities[activity];
    }

    /**
     * Returns the integer code for the human-readable state of Snark.
     */
    public int getState ()
    {
        return activity;
    }

    private void upnpMapAnyIP(int port) {
    	for (String ipaddr: ip) {
        	if (upnpMapPort(port,ipaddr))
        		return;
        }
    }
    
    //TODO - fix exception logic here
    private boolean upnpMapPort(int port, String ip) {
    	try {
    		int discoveryTimeout = 5000; // 5 secs to receive a response from devices
	        igds = InternetGatewayDevice.getDevices( discoveryTimeout );
	        if ( igds != null ) {
	          for (InternetGatewayDevice igd : igds) {
		          System.err.println( "Found UPNP device " + igd.getIGDRootDevice().getModelName()+" ("+igd.getIGDRootDevice().getManufacturer()+")" );
		          if (mlistener != null)
		        	  mlistener.message("Found UPNP device " + igd.getIGDRootDevice().getModelName()+" ("+igd.getIGDRootDevice().getManufacturer()+")" );
		          boolean mapped = igd.addPortMapping( "Lobber BitTorrent Client",null, port, port,ip, 0, "TCP" );
		          if ( mapped ) {
		            System.err.println( "Port "+port+" mapped to " + ip );
		            if (mlistener != null)
		            	mlistener.message("Port "+port+" mapped to " + ip + " on "+
		            			igd.getIGDRootDevice().getModelName()+" ("+igd.getIGDRootDevice().getManufacturer()+")");
		            return true;
		          }
	          }
	        }
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	
    	return false;
    }

    private void upnpUnMapPort(int port) {
		if (igds != null) {
			for (InternetGatewayDevice igd : igds) {
				System.err.println( "Attempting to remove mapping for port "+port+" on UPNP device " + igd.getIGDRootDevice().getModelName()+" ("+igd.getIGDRootDevice().getManufacturer()+")" );
				try {
	          		igd.deletePortMapping(null, port, "TCP");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
    }
    
    /**
     * Establishes basic information such as {@link #id}, opens ports,
     * and determines whether to act as a peer or seed.
     */
    public void setupNetwork ()
        throws IOException
    {
        activity = NETWORK_SETUP;
        
        IOException lastException = null;
        if (user_port != -1) {
            port = user_port;
            try {
                serversocket = new ServerSocket(port);
            } catch (IOException ioe) {
                lastException = ioe;
            }
        } else {
            for (port = MIN_PORT; serversocket == null && port <= MAX_PORT; port++) {
                try {
                    serversocket = new ServerSocket(port);
                } catch (IOException ioe) {
                    lastException = ioe;
                }
            }
        }
        if (serversocket == null) {
            String message = "Cannot accept incoming connections ";
            if (user_port == -1) {
                message = message + "tried ports " + MIN_PORT + " - "
                    + MAX_PORT;
            } else {
                message = message + "on port " + user_port;
            }

            if (ip != null || user_port != -1) {
            	if (mlistener != null)
                	mlistener.message(message);
                abort(message, lastException);
            } else {
            	if (mlistener != null)
                	mlistener.message("warning: "+message);
                log.log(Level.WARNING, message);
            }
            port = -1;
        } else {
            log.log(Level.FINE, "Listening on port: " + port);
        }
      
        if (needsNATMapping) {
        	System.err.println("Trying upnp for port "+port);
            if (mlistener != null)
            	mlistener.message("UPNP configuration...");
        	upnpMapAnyIP(port);
        }
        
        if (mlistener != null)
        	mlistener.message("Listening on port "+port);

        activity = CREATING_TORRENT;
        storage = new Storage(path, announce, slistener);
        if (mlistener != null)
        	mlistener.message("Initializing torrent...");
        storage.create();
        meta = storage.getMetaInfo();
        
        try {
        	File tmpFile = File.createTempFile("lobber", ".torrent");
	        FileOutputStream fout = new FileOutputStream(tmpFile);
	        fout.write(meta.getTorrentData());
	        fout.flush();
	        fout.close();
        } catch (Throwable ex) {
        	ex.printStackTrace();
        }
    }

    /**
     * Start the upload/download process and begins exchanging pieces
     * with other peers.
     */
    public void collectPieces ()
        throws IOException
    {
        activity = COLLECTING_PIECES;
        coordinator = new PeerCoordinator(id, meta, storage, clistener);

        PeerAcceptor peeracceptor = new PeerAcceptor(coordinator, mlistener);
        acceptor = new ConnectionAcceptor(serversocket, null, peeracceptor);
        acceptor.start();

        trackerclient = new TrackerClient(meta, coordinator, mlistener, port);
        trackerclient.start();
        coordinator.setTracker(trackerclient);
    }

    public void halt() {
    	log.log(Level.INFO, "Shutting down...");

    	if (needsNATMapping) {
    		log.log(Level.FINE, "Removing UPNP NAT port mapping...");
    		upnpUnMapPort(port);
    	}
    		
        log.log(Level.FINE, "Halting ConnectionAcceptor...");
        if (this.acceptor != null) {
            this.acceptor.halt();
        }

        log.log(Level.FINE, "Halting TrackerClient...");
        if (this.trackerclient != null) {
            this.trackerclient.halt();
        }

        log.log(Level.FINE, "Halting PeerCoordinator...");
        if (this.coordinator != null) {
            this.coordinator.halt();
        }

        log.log(Level.FINE, "Closing Storage...");
        if (this.storage != null) {
            try {
                this.storage.close();
            } catch (IOException ioe) {
                log.log(Level.SEVERE, "Couldn't properly close storage", ioe);
            }
        }
    }
    
    /**
     * Aborts program abnormally.
     */
    public static void abort (String s)
        throws IOException
    {
        abort(s, null);
    }

    /**
     * Aborts program abnormally.
     */
    public static void abort (String s, IOException ioe)
        throws IOException
    {
        log.log(Level.SEVERE, s, ioe);
        throw new IOException(s);
    }

    /** The listen port requested by the user */
    protected int user_port;

    /** The port number Snark listens on */
    protected int port;

    /** My local addresses */
    protected String[] ip;

    /** The {@link StorageListener} to send updates to */
    protected StorageListener slistener;

    /** The {@link CoordinatorListener} to send updates to */
    protected CoordinatorListener clistener;

    /** Our BitTorrent client id number, randomly assigned */
    protected byte[] id = new byte[20];

    /** The server socket that we are using to listen for connections */
    protected ServerSocket serversocket;

    /**
     * A magic constant used to identify the Snark library in the clientid.
     * 
     * <pre>Taking Three as the subject to reason about--
     * A convenient number to state--
     * We add Seven, and Ten, and then multiply out
     * By One Thousand diminished by Eight.
     *
     * The result we proceed to divide, as you see,
     * By Nine Hundred and Ninety Two:
     * Then subtract Seventeen, and the answer must be
     * Exactly and perfectly true.</pre>
     */
    protected static final byte snark =
        (((3 + 7 + 10) * (1000 - 8)) / 992) - 17;

    /** An integer indicating Snark's current activity. */
    protected int activity = NOT_STARTED;

    /** The list of possible activities */
    protected static final String[] activities =
        {"Not started", "Network setup", "Getting torrent", "Creating torrent",
        "Checking storage", "Collecting pieces", "Seeding"};

    public static final int NOT_STARTED = 0;
    public static final int NETWORK_SETUP = 1;
    public static final int GETTING_TORRENT = 2;
    public static final int CREATING_TORRENT = 3;
    public static final int CHECKING_STORAGE = 4;
    public static final int COLLECTING_PIECES = 5;
    public static final int SEEDING = 6;

    /** The Java logger used to process our log events. */
    protected static final Logger log = Logger.getLogger("org.klomp.snark");
}
