package org.arx.protocol.tcp;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.arx.protocol.Server;
import org.junit.Test;

public class TestTcpFactory {
	@Test
	public void testFactory() throws IOException {
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(TcpFactory.HOST_KEY, "localhost");
		parameters.put(TcpFactory.PORT_KEY, "6789");
		TcpFactory factory = new TcpFactory();
		ExecutorService executor = Executors.newCachedThreadPool();
		try {
			Server server = factory.createServer(executor, parameters);
			assertEquals(TcpServer.class,server.getClass());
			executor.execute(server);
			assertEquals(TcpClient.class,factory.createClient(parameters).getClass());
		} finally {
			executor.shutdown();
		}
	}
}
