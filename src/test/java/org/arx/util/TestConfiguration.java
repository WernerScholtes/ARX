package org.arx.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

public class TestConfiguration {
	@Test
	public void testSingleton() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Configuration configuration = Configuration.createInstance(Utils.HOME);
		assertEquals(Utils.HOME_PATH,configuration.getHome());
		assertEquals(Utils.HOME_PATH.resolve("conf"),configuration.getConfDir());
		assertEquals("# crud", configuration.getParameter("defaultCredentials"));
		assertNull(configuration.getParameter("notPresent"));
		assertEquals("x-conference/x-cooltalk",configuration.getMimeType("ice"));
		assertNull(configuration.getMimeType("notPresent"));
		Object bf = configuration.createBackendFactory();
		assertEquals("org.arx.backend.file.FileSystemFactory",bf.getClass().getName());
		Object pf = configuration.createProtocolFactory();
		assertEquals("org.arx.protocol.tcp.TcpFactory",pf.getClass().getName());
	}
	
	@Test
	public void testChanges() throws IOException, InterruptedException {
		ExecutorService executor = Executors.newCachedThreadPool();
		try {
			Configuration configuration = Configuration.createInstance(Utils.HOME);
			executor.execute(configuration);
			assertEquals("# crud", configuration.getParameter("defaultCredentials"));
			try {
				Utils.copyFile("conf/arx.conf", "conf/arx-ori.conf");
				Utils.copyFile("conf/arx2.conf", "conf/arx.conf");
				Thread.sleep(500);
				assertEquals("# -",configuration.getParameter("defaultCredentials"));
			} finally {
				Utils.copyFile("conf/arx-ori.conf", "conf/arx.conf");			
			}
			Thread.sleep(500);
			assertEquals("# crud", configuration.getParameter("defaultCredentials"));
	
			assertEquals("x-conference/x-cooltalk",configuration.getMimeType("ice"));
			try {
				Utils.copyFile("conf/mime.types", "conf/mime-ori.types");
				Utils.copyFile("conf/mime2.types", "conf/mime.types");
				Thread.sleep(500);
				assertNull(configuration.getMimeType("ice"));
			} finally {
				Utils.copyFile("conf/mime-ori.types", "conf/mime.types");
			}
			Thread.sleep(500);
			assertEquals("x-conference/x-cooltalk",configuration.getMimeType("ice"));
		} finally {
			executor.shutdown();
		}
	}

}
