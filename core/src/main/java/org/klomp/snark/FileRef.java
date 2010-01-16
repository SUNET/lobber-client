package org.klomp.snark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;

public class FileRef {

	private long length;
	private String path;
	private File _file;
	private RandomAccessFile _rafs;
	private String _mode;
	private Date lastUsed;
	
	public String toString() {
		return "FileRef: ["+path+" ("+length+" bytes)]";
	}
	
	public long getLength() {
		return length;
	}
	
	public void setLength(long length) {
		this.length = length;
	}
	
	public String getName() {
		return getFile().getName(); // XXX Parse path instead!
	}
	
	public String getPath() {
		return path;
	}
	
	public FileRef(File f) {
		this.path = f.getAbsolutePath();
		this.length = f.length();
		this.lastUsed = new Date();
	}
	
	public FileRef(String path, long length) {
		this.path = path;
		this.length = length;
		_rafs = null;
		_mode = null;
	}
	
	public synchronized File getFile() {
		if (_file == null) {
			_file = new File(path);
			lastUsed = new Date();
		}
		
		return _file;
	}
	
	public RandomAccessFile getRandomAccessFile(String mode) throws FileNotFoundException {
		if (_rafs == null || _mode == null || !_mode.contains(mode)) { // rw > r
			_rafs = new RandomAccessFile(getFile(), mode);
			_mode = mode;
		}
		
		return _rafs;
	}
	
	public void close() throws IOException {
		if (_rafs != null) {
			_rafs.close();
			_rafs = null;
		}
		if (_file != null)
			_file = null;
	}
	
	public synchronized void seekAndWrite(byte [] data, long start, int offset, int length) throws IOException {
		try {
			RandomAccessFile rafs = getRandomAccessFile("rw");
			rafs.seek(start);
			rafs.write(data,offset,length);
		} catch (IOException ex) {
			close();
			throw ex;
		} finally {
			close();
		}
	}
	
	public synchronized void seekAndRead(byte [] data, long start, int offset, int length) throws IOException {
		try {
			RandomAccessFile rafs = getRandomAccessFile("r");
			rafs.seek(start);
			rafs.readFully(data,offset,length);
		} catch (IOException ex) {
			close();
			throw ex;
		} finally {
			close();
		}
	}
	
	public synchronized long getActualLength() throws IOException {
		try {
			RandomAccessFile rafs = getRandomAccessFile("r");
			long length = rafs.length();
			return length;
		} catch (IOException ex) {
			close();
			throw ex;
		} finally {
			close();
		}
	}
}
