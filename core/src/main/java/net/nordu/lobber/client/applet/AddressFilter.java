/**
 * 
 */
package net.nordu.lobber.client.applet;

import java.net.InetAddress;

public interface AddressFilter {
	
	public abstract boolean accept(InetAddress ip);
	
}