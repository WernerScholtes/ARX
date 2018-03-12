package org.arx.protocol.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;

import org.arx.Endpoint;
import org.arx.protocol.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A TCP server is an implementation of the interface Server. It listens for
 * client connections and spawns a {@link TcpSession} for every established
 * connection. Before using the TCP server, a backend must be set (see
 * {@link #setBackend(Endpoint)}) and the TCP server must be executed to start
 * listening to its port.
 */
public class TcpServer implements Server {
	private static final Logger LOGGER = LoggerFactory.getLogger(TcpServer.class);
	private static final String ACCEPT_ERROR = "Error during ServerSocket.accept()";
	private static final String PORT_FORMAT = "ARX-TcpServer now listening on port %1$s";
	private Executor executor;
	private ServerSocket serverSocket;
	private Endpoint backend;

	/**
	 * Creates a TCP server for the specified port.
	 * 
	 * @param executor
	 *            the executor used to spawn {@link TcpSession}-objects
	 * @param port
	 *            the port this TCP server listens to
	 * @throws IOException
	 *             if an IO error occurs during initialization of the server
	 *             socket
	 */
	public TcpServer(Executor executor, int port) throws IOException {
		this.executor = executor;
		serverSocket = new ServerSocket(port);
		if (serverSocket == null) {
			throw new IllegalStateException();
		}
		LOGGER.info(String.format(PORT_FORMAT, port));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				executor.execute(new TcpSession(backend, socket));
			} catch (IOException e) {
				LOGGER.error(ACCEPT_ERROR, e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.protocol.Server#setBackend(org.arx.Endpoint)
	 */
	@Override
	public void setBackend(Endpoint backend) {
		this.backend = backend;
	}

}
