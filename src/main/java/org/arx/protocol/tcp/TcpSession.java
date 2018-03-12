package org.arx.protocol.tcp;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import org.arx.Endpoint;
import org.arx.Credentials;
import org.arx.Data;
import org.arx.Reason;
import org.arx.Resource;
import org.arx.Observer;
import org.arx.MessageType;
import org.arx.util.Configuration;
import org.arx.util.Message;
import org.arx.util.RequestMessage;
import org.arx.util.ResponseMessage;
import org.arx.util.StringCredentials;
import org.arx.util.jwt.JWTDecodeException;
import org.arx.util.jwt.JwtCredentials;
import org.arx.util.jwt.SignatureVerificationException;
import org.arx.util.jwt.TokenExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A TCP sessions handles the TCP communication between one client and the TCP
 * server. It receives requests from a TCP socket, sends them to a backend,
 * receives the responses from the backend with its {@link Observer} interface
 * and sends the responses back to the TCP client.
 */
public class TcpSession implements Observer, Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(TcpSession.class);
	private Endpoint backend;
	private Socket socket;
	private String defaultCred;

	/**
	 * Creates a TCP session for the specified parameters.
	 * 
	 * @param backend
	 *            the backend to be used for requests
	 * @param socket
	 *            the socket used for reading requests and writing responses
	 */
	public TcpSession(Endpoint backend, Socket socket) {
		this.backend = backend;
		this.socket = socket;
		this.defaultCred = Configuration.getInstance().getParameter("defaultCredentials");
		if (this.defaultCred == null) {
			this.defaultCred = "";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Observer#onSuccess(org.arx.MessageType, org.arx.Resource,
	 * org.arx.Resource[])
	 */
	@Override
	public void onSuccess(MessageType request, Resource resource, Resource... affectedResources) throws IOException {
		ResponseMessage message = new ResponseMessage(null, MessageType.SUCCESS, request, resource, null, null,
				affectedResources);
		// Write message to TCP output stream
		OutputStream out = socket.getOutputStream();
		out.write(message.toByteArray());
		out.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Observer#onData(org.arx.MessageType, org.arx.Resource,
	 * org.arx.Reason, org.arx.Resource, org.arx.Data)
	 */
	@Override
	public void onData(MessageType request, Resource resource, Reason reason, Resource affectedResource, Data data)
			throws IOException {
		ResponseMessage message = new ResponseMessage(null, MessageType.DATA, request, resource, reason, data,
				affectedResource);
		// Write message to TCP output stream
		OutputStream out = socket.getOutputStream();
		out.write(message.toByteArray());
		out.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Observer#onError(org.arx.MessageType, org.arx.Resource,
	 * org.arx.MessageType)
	 */
	@Override
	public void onError(MessageType request, Resource resource, MessageType status) throws IOException {
		ResponseMessage message = new ResponseMessage(null, status, request, resource, null, null);
		// Write message to TCP output stream
		OutputStream out = socket.getOutputStream();
		out.write(message.toByteArray());
		out.flush();
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
				RequestMessage message = (RequestMessage) Message.createFromStream(in);
				Credentials credentials = getCredentials(message);
				MessageType request = message.getRequest();
				Resource resource = message.getResource();
				Data data = message.getData();
				switch (request) {
				case PING:
					ping();
					break;
				case CREATE:
					backend.create(credentials, resource, data, this);
					break;
				case UPDATE:
					backend.update(credentials, resource, data, this);
					break;
				case SAVE:
					backend.save(credentials, resource, data, this);
					break;
				case DELETE:
					backend.delete(credentials, resource, this);
					break;
				case READ:
					backend.read(credentials, resource, this);
					break;
				case SUBSCRIBE:
					backend.subscribe(credentials, resource, this);
					break;
				case UNSUBSCRIBE:
					backend.unsubscribe(credentials, resource, this);
					break;
				case SUBSCRIBE_STATUS:
					backend.subscribeStatus(credentials, resource, this);
					break;
				case UNSUBSCRIBE_ALL:
					backend.unsubscribeAll(credentials, this);
					break;
				default:
					throw new IllegalArgumentException("Illegal request type " + request);
				}

			}
		} catch (IOException | IllegalArgumentException | SignatureVerificationException | TokenExpiredException
				| JWTDecodeException | NoSuchAlgorithmException e) {
			LOGGER.error("Error occurred during execution of a request", e);
		}

	}

	private void ping() throws IOException {
		ResponseMessage message = new ResponseMessage(null, MessageType.SUCCESS, MessageType.PING, null, null, null);
		// Write message to TCP output stream
		OutputStream out = socket.getOutputStream();
		out.write(message.toByteArray());
		out.flush();
	}

	private Credentials getCredentials(Message message) throws IllegalArgumentException, SignatureVerificationException,
			TokenExpiredException, JWTDecodeException, NoSuchAlgorithmException {
		StringCredentials credentials = new StringCredentials();
		credentials.parseAuthorization(defaultCred);
		String authorization = message.getHeaderField("Authorization");
		if (authorization != null) {
			String[] words = authorization.split("[ \t]+", 2);
			if (words.length > 0) {
				String type = words[0].toUpperCase();
				if (type.equalsIgnoreCase("Bearer") && words.length > 1) {
					JwtCredentials jwt = new JwtCredentials(words[1]);
					credentials.parseAuthorization(jwt.getDecodedToken(), jwt.getExpirationTime());
				}
			}
		}
		return credentials;
	}

}
