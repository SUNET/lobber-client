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
		progressBar.setValue(pdone);
		if (pdone >= 100 || pdone <= 0) {
			progressBar.setIndeterminate(true);
		}
		System.err.println("% done: "+100*ul+"/"+size+"="+pdone);
		System.err.println("Peer "+peer.getPeerID()+" has downloaded "+ peer.getUploaded()+" bytes");
	}
}