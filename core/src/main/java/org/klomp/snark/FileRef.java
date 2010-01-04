package org.klomp.snark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class FileRef {

	private long length;
	private String path;
	private File _file;
	private RandomAccessFile _rafs;
	private String _mode;
	
	public long getLength() {
		return length;
	}
	
	public String getPath() {
		return path;
	}
	
	public FileRef(File f) {
		this.path = f.getAbsolutePath();
		this.length = f.length();
	}
	
	public FileRef(String path, long length) {
		this.path = path;
		this.length = length;
	}
	
	public synchronized File getFile() {
		if (_file == null)
			_file = new File(path);
		
		return _file;
	}
	
	public RandomAccessFile getRandomAccessFile(String mode) throws FileNotFoundException {
		if (_rafs == null || !mode.equals(_mode)) {
			_rafs = new RandomAccessFile(getFile(), mode);
			_mode = mode;
		}
		
		return _rafs;
	}
}
