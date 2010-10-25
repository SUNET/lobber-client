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
	private long maxul;
	
	public AppletCoordinatorListener(JProgressBar progressBar) {
		this.progressBar = progressBar;
		this.size = 0;
		this.maxul = 0;
	}
	
	public void setSize(long size) {
		this.size = size;
	}

	public void peerChange(PeerCoordinator coordinator, Peer peer) {
		long ul = peer.getUploaded();
		if (ul > maxul) {
			int pdone = Math.round((100*((float)ul)/((float)size)));
			progressBar.setValue(pdone);
			System.err.println("% done: "+100*ul+"/"+size+"="+pdone);
			//progressBar.setIndeterminate(false);
			this.maxul = ul;
		}
		System.err.println("Peer "+peer.getPeerID()+" has downloaded "+ peer.getUploaded()+" bytes");
	}
}