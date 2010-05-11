/**
 * 
 */
package net.nordu.lobber.client.applet;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.klomp.snark.Storage;
import org.klomp.snark.StorageListener;

public final class AppletStorageListener implements StorageListener {

	public void storageCreateFile(Storage arg0, String arg1, long arg2) {
		System.err.println("created "+arg1+" "+arg2);
	}

	public void storageChecked(Storage arg0, int arg1, boolean arg2) {
		System.err.println("checked "+arg1+" "+arg2);
	}

	public void storageAllocated(Storage arg0, long arg1) {
		System.err.println("allocated "+arg1);
	}

	public void storageAllChecked(Storage arg0) {
		System.err.println("all checked");
	}

	public void storateGetPiece(Storage storage,int num) {
		System.err.println("get piece "+num);
	}

	public void message(Object msg) {
		System.err.println(msg.toString());
	}

	public void exception(Throwable t) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		t.printStackTrace(new PrintStream(baos));
	}
}