package net.nordu.lobber.client.test;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.klomp.snark.MetaInfo;
import org.klomp.snark.Storage;
import org.klomp.snark.StorageListener;

public class TestMetaInfo {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateMetaInfo() throws IOException, URISyntaxException {
		URL url = Thread.currentThread().getContextClassLoader().getResource("a-file.txt");
		File file = new File(url.toURI());
		Storage storage = new Storage(file, "http://announce.example.org", new StorageListener() {
			
			public void storageCreateFile(Storage arg0, String arg1, long arg2) {
				System.err.println("created "+arg1+" "+arg2);
			}
			
			public void storageChecked(Storage arg0, int arg1, boolean arg2) {
				System.err.println("checked "+arg1+" "+arg2);
			}
			
			public void storageAllocated(Storage arg0, long arg1) {
				System.err.println("allocated "+arg1);
			}
			
			public void storageAllChecked(Storage arg0) {
				System.err.println("all checked");
			}
		});
		
		MetaInfo metaInfo = storage.getMetaInfo();
		Assert.assertNotNull(metaInfo);
		Assert.assertNotNull(metaInfo.getName());
		System.err.println(metaInfo.getName());
		System.err.println(metaInfo.getInfoHash());
	}
	
}
