/**
 * 
 */
package net.nordu.lobber.client.applet;

import org.klomp.snark.CoordinatorListener;
import org.klomp.snark.Peer;
import org.klomp.snark.PeerCoordinator;

public final class AppletCoordinatorListener implements
CoordinatorListener {


	public void peerChange(PeerCoordinator coordinator, Peer peer) {
		System.err.println("Peer "+peer.getPeerID()+" has downloaded "+ peer.getUploaded()+" bytes");
	}
}