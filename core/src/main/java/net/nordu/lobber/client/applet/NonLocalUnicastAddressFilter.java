/**
 * 
 */
package net.nordu.lobber.client.applet;

import java.net.InetAddress;


public class NonLocalUnicastAddressFilter implements AddressFilter {
	
	public boolean accept(InetAddress ip) {
		return !ip.isLinkLocalAddress() && !ip.isLoopbackAddress() && !ip.isSiteLocalAddress() && !ip.isMulticastAddress();
	}
	
}