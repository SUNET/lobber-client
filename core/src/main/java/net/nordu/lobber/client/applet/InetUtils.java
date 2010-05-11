package net.nordu.lobber.client.applet;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class InetUtils {

	public static String[] getAddresses(AddressFilter filter) throws SocketException {
		 List<String> addrs = new ArrayList<String>();
		 
         Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

         while(e.hasMoreElements()) {
            NetworkInterface ni = (NetworkInterface) e.nextElement();
            
            //System.err.println(ni.getName());
            if (ni.getName().startsWith("vmnet"))
            	continue;
            
            Enumeration<InetAddress> e2 = ni.getInetAddresses();

            while (e2.hasMoreElements()){
               InetAddress ip = (InetAddress) e2.nextElement();
               if (filter.accept(ip)) {
            	   addrs.add(ip.getHostAddress());
               }
            }
         }
         
         String[] res = new String[addrs.size()];
         return (String[])addrs.toArray(res);
	 }

	 public static String[] getAddresses() throws SocketException {
		 return getAddresses(new NonLocalUnicastAddressFilter());
	 }
	 
}
