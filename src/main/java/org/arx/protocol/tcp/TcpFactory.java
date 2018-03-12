package org.arx.protocol.tcp;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;

import org.arx.protocol.Server;
import org.arx.util.Configuration;
import org.arx.Endpoint;
import org.arx.protocol.ProtocolFactory;

/**
 * A TCP factory is an implementation of the interface ProtocolFactory
 * for an ARX protocol that is implemented by TCP sockets. 
 */
public class TcpFactory implements ProtocolFactory {
	public static final String HOST_KEY = "org.arx.protocol.tcp.host";
	public static final String PORT_KEY = "org.arx.protocol.tcp.port";

	/* (non-Javadoc)
	 * @see org.arx.protocol.ProtocolFactory#createServer(java.util.concurrent.Executor, java.util.Map)
	 */
	@Override
	public Server createServer(Executor executor, Map<String, String> parameters) throws IOException {
		String portString = null;
		if ( parameters != null ) {
			portString = parameters.get(PORT_KEY);
		}
		if ( portString == null ) {
			portString = Configuration.getInstance().getParameter(PORT_KEY);
		}
		if ( portString != null ) {
			int port = Integer.decode(portString);
			return new TcpServer(executor,port);
		}
		throw new IllegalArgumentException();
	}

	/* (non-Javadoc)
	 * @see org.arx.protocol.ProtocolFactory#createClient(java.util.Map)
	 */
	@Override
	public Endpoint createClient(Map<String, String> parameters) throws IOException {
		String server = parameters.get(HOST_KEY);
		if ( server != null ) {
			String portString = null;
			if ( parameters != null ) {
				portString = parameters.get(PORT_KEY);
			}
			if ( portString == null ) {
				portString = Configuration.getInstance().getParameter(PORT_KEY);
			}
			if ( portString != null ) {
				int port = Integer.decode(portString);
				return new TcpClient(server,port);
			}
		}
		throw new IllegalArgumentException();
	}

}
