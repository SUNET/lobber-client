/*
 * PeerAcceptor - Accepts incomming connections from peers. Copyright (C) 2003
 * Mark J. Wielaard
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Accepts incomming connections from peers. The ConnectionAcceptor will call
 * the connection() method when it detects an incomming BT protocol connection.
 * The PeerAcceptor will then create a new peer if the PeerCoordinator wants
 * more peers.
 */
public class PeerAcceptor
{
    private final PeerCoordinator coordinator;
    private final MessageListener mlistener;

    public PeerAcceptor (PeerCoordinator coordinator, MessageListener mlistener)
    {
        this.coordinator = coordinator;
        this.mlistener = mlistener;
    }

    public void connection (Socket socket, BufferedInputStream bis,
        BufferedOutputStream bos) throws IOException
    {
    	System.err.println("connection from "+socket.getInetAddress());
    	System.err.println("need peers? "+coordinator.needPeers());
        if (coordinator.needPeers()) {
            Peer peer = new Peer(socket, bis, bos, coordinator.getID(),
                coordinator.getMetaInfo());
            System.err.println("Adding peer "+peer);
            coordinator.addPeer(peer);
        } else {
        	System.err.println("Closing unwanted connection from "+socket.getInetAddress());
            socket.close();
        }
    }
}
