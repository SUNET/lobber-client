/**
 * 
 */
package net.nordu.lobber.client.applet;

import javax.swing.JProgressBar;

import org.klomp.snark.CoordinatorListener;
import org.klomp.snark.Peer;
import org.klomp.snark.PeerCoordinator;

public final class AppletCoordinatorListener implements
CoordinatorListener {

	private JProgressBar progressBar;
	private long size;
	
	public AppletCoordinatorListener(JProgressBar progressBar) {
		this.progressBar = progressBar;
		this.size = 0;
	}
	
	public void setSize(long size) {
		this.size = size;
	}

	public void peerChange(PeerCoordinator coordinator, Peer peer) {
		long ul = coordinator.getUploaded();
		int pdone = Math.round((100*((float)ul)/((float)size)));
		if (pdone >= 100) {
			progressBar.setValue(99);
		} else if (pdone <= 0) {
			progressBar.setValue(0);
		} else {
			progressBar.setValue(pdone-1);
		}
		System.err.println("% done: "+100*ul+"/"+size+"="+pdone);
		System.err.println("Peer "+peer.getPeerID()+" has downloaded "+ peer.getUploaded()+" bytes");
	}
}