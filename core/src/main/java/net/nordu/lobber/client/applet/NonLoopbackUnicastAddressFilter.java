package net.nordu.lobber.client.applet;

import java.net.InetAddress;

public class NonLoopbackUnicastAddressFilter implements AddressFilter {
		
		public boolean accept(InetAddress ip) {
			return !ip.isLoopbackAddress() && !ip.isMulticastAddress();
		}
		
}
	
