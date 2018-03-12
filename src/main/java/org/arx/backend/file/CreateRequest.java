package org.arx.backend.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.arx.Credentials;
import org.arx.Data;
import org.arx.Resource;
import org.arx.Observer;
import org.arx.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A create request is used to execute the asynchronous creation of a resource.
 */
class CreateRequest implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(CreateRequest.class);
	private static final String RESOURCE_ALREADY_EXISTS_FORMAT = "Resource %1$s already exists";
	private static final String CREATE_SUCCESS_FORMAT = "Successfully created resource %1$s";
	private static final String CREATE_ERROR_FORMAT = "Cannot create resource %1$s";
	private static final String FORBIDDEN_FORMAT = "Forbidden to create resource %1$s";
	private static final String PATTERN_ERROR_FORMAT = "Cannot create resource pattern %1$s";
	private Credentials credentials;
	private Resource resource;
	private Data data;
	private Observer observer;

	/**
	 * Creates a create request object for the specified parameters.
	 * 
	 * @param credentials
	 *            the credentials that are used to examine if the CREATE access
	 *            right is granted for the specified resource.
	 * @param resource
	 *            the resource to be created
	 * @param data
	 *            the data content for the resource that shall be created
	 * @param observer
	 *            the observer that is used for the response message
	 */
	public CreateRequest(Credentials credentials, Resource resource, Data data, Observer observer) {
		this.credentials = credentials;
		this.resource = resource;
		this.data = data;
		this.observer = observer;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			if (!credentials.canCreate(resource)) {
				LOGGER.error(String.format(FORBIDDEN_FORMAT, resource));
				observer.onError(MessageType.CREATE, resource, MessageType.FORBIDDEN);
			} else if (resource.isPattern()) {
				LOGGER.error(String.format(PATTERN_ERROR_FORMAT, resource));
				observer.onError(MessageType.CREATE, resource, MessageType.BAD_REQUEST);
			} else {
				Path path = FileSystemFactory.getPath(resource);
				if (Files.notExists(path)) {
					Path parent = path.getParent();
					if (Files.notExists(parent)) {
						parent.toFile().mkdirs();
					}
					try {
						Files.write(path, data.getContent());
						LOGGER.debug(String.format(CREATE_SUCCESS_FORMAT, resource));
						observer.onSuccess(MessageType.CREATE, resource, resource);
					} catch (IOException e) {
						LOGGER.error(String.format(CREATE_ERROR_FORMAT, resource), e);
						observer.onError(MessageType.CREATE, resource, MessageType.INTERNAL_SERVER_ERROR);
					}
				} else {
					LOGGER.error(String.format(RESOURCE_ALREADY_EXISTS_FORMAT, resource));
					observer.onError(MessageType.CREATE, resource, MessageType.ALREADY_EXISTS);
				}
			}
		} catch (IOException e) {
			LOGGER.error(String.format(CREATE_ERROR_FORMAT, resource), e);
		}
	}

}
