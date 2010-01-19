package net.nordu.lobber.client.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import net.nordu.lobber.client.applet.InetUtils;
import net.nordu.lobber.client.applet.NonLocalUnicastAddressFilter;
import net.nordu.lobber.client.applet.NonLoopbackUnicastAddressFilter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InetUtilsTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetAddresses() {
		try {
			String[] nonLocal = InetUtils.getAddresses(new NonLocalUnicastAddressFilter());
			String[] nonLoopBack = InetUtils.getAddresses(new NonLoopbackUnicastAddressFilter());
			
			assertNotNull(nonLocal);
			System.err.println("Non local addresses: ");
			for (String addr : nonLocal) {
				System.err.println(addr);
			}
			
			assertNotNull(nonLoopBack);
			System.err.println("Non loopback addresses: ");
			for (String addr : nonLoopBack) {
				System.err.println(addr);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail(t.getMessage());
		}
	}

}
