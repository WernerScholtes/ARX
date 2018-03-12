package org.arx.backend.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.arx.Credentials;
import org.arx.Data;
import org.arx.Reason;
import org.arx.Resource;
import org.arx.Observer;
import org.arx.MessageType;
import org.arx.util.ByteArrayData;
import org.arx.util.ResourceVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A read request is used to asynchronously read resources.
 */
class ReadRequest implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReadRequest.class);
	private static final String RESOURCE_NOT_FOUND_FORMAT = "Resource %1$s not found";
	private static final String READ_SUCCESS_FORMAT = "Successfully read resource %1$s";
	private static final String READ_ERROR_FORMAT = "Cannot read resource %1$s";
	private static final String FORBIDDEN_FORMAT = "Forbidden to read resource %1$s";
	private Credentials credentials;
	private Resource resource;
	private Observer observer;

	/**
	 * Creates a read request for the specified parameters
	 * 
	 * @param credentials
	 *            the credentials that are used to examine if the READ access
	 *            right is granted for the specified resources.
	 * @param resourcePattern
	 *            the resources to be created
	 * @param observer
	 *            the observer that is used for the response messages
	 */
	public ReadRequest(Credentials credentials, Resource resourcePattern, Observer observer) {
		this.credentials = credentials;
		this.resource = resourcePattern;
		this.observer = observer;
	}

	@Override
	public void run() {
		try {
			if (!credentials.canRead(resource)) {
				LOGGER.error(String.format(FORBIDDEN_FORMAT, resource));
				observer.onError(MessageType.READ, resource, MessageType.FORBIDDEN);
			} else if (resource.isPattern()) {
				FileSystemWalker walker = new FileSystemWalker();
				try {
					walker.walkResource(resource, new ResourceVisitor() {
						@Override
						public void visitResource(Resource res) throws IOException {
							Path path = FileSystemFactory.getPath(res);
							byte[] bytes = Files.readAllBytes(path);
							String mimeType = FileSystemFactory.getMimeType(res);
							Data data = new ByteArrayData(mimeType, bytes);
							observer.onData(MessageType.READ, resource, Reason.INITIAL, res, data);
							LOGGER.debug(READ_SUCCESS_FORMAT, res);
						}
					});
					observer.onSuccess(MessageType.READ, resource);
				} catch (IOException e) {
					LOGGER.error(String.format(READ_ERROR_FORMAT, resource), e);
					observer.onError(MessageType.READ, resource, MessageType.INTERNAL_SERVER_ERROR);
				}
			} else {
				Path path = FileSystemFactory.getPath(resource);
				if (Files.exists(path)) {
					try {
						byte[] bytes = Files.readAllBytes(path);
						String mimeType = FileSystemFactory.getMimeType(resource);
						Data data = new ByteArrayData(mimeType, bytes);
						observer.onData(MessageType.READ, resource, Reason.INITIAL, resource, data);
						LOGGER.debug(String.format(READ_SUCCESS_FORMAT, resource));
						observer.onSuccess(MessageType.READ, resource);
					} catch (IOException e) {
						LOGGER.error(String.format(READ_ERROR_FORMAT, resource), e);
						observer.onError(MessageType.READ, resource, MessageType.INTERNAL_SERVER_ERROR);
					}
				} else {
					LOGGER.error(String.format(RESOURCE_NOT_FOUND_FORMAT, resource));
					observer.onError(MessageType.READ, resource, MessageType.NOT_FOUND);
				}
			}
		} catch (IOException e) {
			LOGGER.error(String.format(READ_ERROR_FORMAT, resource), e);
		}
	}

}
