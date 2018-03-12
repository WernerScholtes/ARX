package org.arx.protocol.tcp;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.arx.Credentials;
import org.arx.Data;
import org.arx.Endpoint;
import org.arx.Reason;
import org.arx.Resource;
import org.arx.Observer;
import org.arx.MessageType;
import org.arx.util.Header;
import org.arx.util.Message;
import org.arx.util.RequestMessage;
import org.arx.util.RequestResource;
import org.arx.util.ResponseMessage;

/**
 * An ARX application can use a TCP client to access an ARX server via TCP.
 */
public class TcpClient implements Endpoint {
	private static final Logger LOGGER = LoggerFactory.getLogger(TcpClient.class);
	private static final String AUTHORIZATION = "Authorization";
	private static final String PROTOCOL_ERROR = "Protocol error while receiving an observer message";
	private Map<RequestResource, Observer> responses;
	private Socket socket;

	/**
	 * Creates a TCP client for the specified host and port.
	 * 
	 * @param host
	 *            the name of the host where the TCP server runs.
	 * @param port
	 *            the port where the TCP server listens.
	 * @throws UnknownHostException
	 *             if the specified host is unknown
	 * @throws IOException
	 *             if an IO error occurred while the connection is being
	 *             established
	 */
	public TcpClient(String host, int port) throws UnknownHostException, IOException {
		this.responses = new HashMap<RequestResource, Observer>();
		this.socket = new Socket(host, port);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Endpoint#ping(org.arx.Credentials, org.arx.Observer)
	 */
	@Override
	public void ping(Credentials credentials, Observer observer) throws IOException {
		writeRequest(credentials,MessageType.PING, observer, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Endpoint#create(org.arx.Credentials, org.arx.Resource,
	 * org.arx.Data, org.arx.Observer)
	 */
	@Override
	public void create(Credentials credentials, Resource resource, Data data, Observer observer) throws IOException {
		writeRequest(credentials,MessageType.CREATE, observer, resource, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Endpoint#update(org.arx.Credentials, org.arx.Resource,
	 * org.arx.Data, org.arx.Observer)
	 */
	@Override
	public void update(Credentials credentials, Resource resourcePattern, Data data, Observer observer)
			throws IOException {
		writeRequest(credentials,MessageType.UPDATE, observer, resourcePattern, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Endpoint#save(org.arx.Credentials, org.arx.Resource,
	 * org.arx.Data, org.arx.Observer)
	 */
	@Override
	public void save(Credentials credentials, Resource resourcePattern, Data data, Observer observer)
			throws IOException {
		writeRequest(credentials,MessageType.SAVE, observer, resourcePattern, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Endpoint#delete(org.arx.Credentials, org.arx.Resource,
	 * org.arx.Observer)
	 */
	@Override
	public void delete(Credentials credentials, Resource resourcePattern, Observer observer) throws IOException {
		writeRequest(credentials,MessageType.DELETE, observer, resourcePattern, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Endpoint#read(org.arx.Credentials, org.arx.Resource,
	 * org.arx.Observer)
	 */
	@Override
	public void read(Credentials credentials, Resource resourcePattern, Observer observer) throws IOException {
		writeRequest(credentials,MessageType.READ, observer, resourcePattern, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Endpoint#subscribe(org.arx.Credentials, org.arx.Resource,
	 * org.arx.Observer)
	 */
	@Override
	public void subscribe(Credentials credentials, Resource resourcePattern, Observer observer) throws IOException {
		writeRequest(credentials,MessageType.SUBSCRIBE, observer, resourcePattern, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Endpoint#subscribeStatus(org.arx.Credentials,
	 * org.arx.Resource, org.arx.Observer)
	 */
	@Override
	public void subscribeStatus(Credentials credentials, Resource resourcePattern, Observer observer)
			throws IOException {
		writeRequest(credentials,MessageType.SUBSCRIBE_STATUS, observer, resourcePattern, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Endpoint#unsubscribe(org.arx.Credentials, org.arx.Resource,
	 * org.arx.Observer)
	 */
	@Override
	public void unsubscribe(Credentials credentials, Resource resourcePattern, Observer observer) throws IOException {
		writeRequest(credentials,MessageType.UNSUBSCRIBE, observer, resourcePattern, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Endpoint#unsubscribeAll(org.arx.Credentials,
	 * org.arx.Observer)
	 */
	@Override
	public void unsubscribeAll(Credentials credentials, Observer observer) throws IOException {
		writeRequest(credentials,MessageType.UNSUBSCRIBE_ALL, observer, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try (DataInputStream in = new DataInputStream(socket.getInputStream())) {
			while (true) {
				ResponseMessage message = (ResponseMessage) Message.createFromStream(in);
				MessageType response = message.getResponse();
				MessageType request = message.getRequest();
				Resource resource = message.getResource();
				Resource[] affectedResources = message.getAffectedResources();
				Resource affectedResource = message.getAffectedResource();
				Reason reason = message.getReason();
				Data data = message.getData();
				Observer observer = null;
				switch (response) {
				case SUCCESS:
					observer = responses.remove(new RequestResource(request, resource));
					if (observer != null) {
						observer.onSuccess(request, resource, affectedResources);
					}
					break;
				case DATA:
					observer = responses.get(new RequestResource(request, resource));
					if (observer != null) {
						observer.onData(request, resource, reason, affectedResource, data);
					}
					break;
				default:
					observer = responses.remove(new RequestResource(request, resource));
					if (observer != null) {
						observer.onError(request, resource, response);
					}
					break;
				}
				if (observer == null) {
					LOGGER.error(PROTOCOL_ERROR);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private void writeRequest(Credentials credentials,MessageType request, Observer observer, Resource resource, Data data) throws IOException {
		if (observer != null) {
			responses.put(new RequestResource(request, resource), observer);
		}
		Header header = new Header();
		if ( credentials != null ) {
			header.put(AUTHORIZATION, credentials.serialize());
		}
		RequestMessage message = new RequestMessage(header, request, resource, data);
		// Write message to TCP output stream
		OutputStream out = socket.getOutputStream();
		out.write(message.toByteArray());
		out.flush();
	}

}
