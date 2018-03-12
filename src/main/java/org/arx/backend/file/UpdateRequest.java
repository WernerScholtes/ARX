package org.arx.backend.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import org.arx.Credentials;
import org.arx.Data;
import org.arx.Resource;
import org.arx.Observer;
import org.arx.MessageType;
import org.arx.util.ResourceVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An update request is used to asynchronously execute the update of resources
 */
class UpdateRequest implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateRequest.class);
	private static final String RESOURCE_NOT_FOUND_FORMAT = "Resource %1$s not found";
	private static final String UPDATE_SUCCESS_FORMAT = "Successfully updated resource %1$s";
	private static final String UPDATE_ERROR_FORMAT = "Cannot update resource %1$s";
	private static final String FORBIDDEN_FORMAT = "Forbidden to delete resource %1$s";
	private Credentials credentials;
	private Resource resource;
	private Data data;
	private Observer observer;

	/**
	 * Creates an update request for the specified parameters.
	 * 
	 * @param credentials
	 *            the credentials that are used to examine if the UPDATE access
	 *            right is granted for the specified resources.
	 * @param resourcePattern
	 *            the resources to be updated
	 * @param data
	 *            the data content for the resource that shall be updated
	 * @param observer
	 *            the observer that is used for the response messages
	 */
	public UpdateRequest(Credentials credentials, Resource resourcePattern, Data data, Observer observer) {
		this.credentials = credentials;
		this.resource = resourcePattern;
		this.data = data;
		this.observer = observer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			if (!credentials.canUpdate(resource)) {
				LOGGER.error(String.format(FORBIDDEN_FORMAT, resource));
				observer.onError(MessageType.UPDATE, resource, MessageType.FORBIDDEN);
			} else if (resource.isPattern()) {
				final List<Resource> affectedResources = new LinkedList<Resource>();
				FileSystemWalker walker = new FileSystemWalker();
				try {
					walker.walkResource(resource, new ResourceVisitor() {
						@Override
						public void visitResource(Resource res) throws IOException {
							Path path = FileSystemFactory.getPath(res);
							Files.write(path, data.getContent());
							LOGGER.debug(UPDATE_SUCCESS_FORMAT, res);
							affectedResources.add(res);
						}
					});
					observer.onSuccess(MessageType.UPDATE, resource,
							affectedResources.toArray(new Resource[affectedResources.size()]));
				} catch (IOException e) {
					LOGGER.error(String.format(UPDATE_ERROR_FORMAT, resource), e);
					observer.onError(MessageType.UPDATE, resource, MessageType.INTERNAL_SERVER_ERROR);
				}
			} else {
				Path path = FileSystemFactory.getPath(resource);
				if (Files.exists(path)) {
					try {
						Files.write(path, data.getContent());
						LOGGER.debug(String.format(UPDATE_SUCCESS_FORMAT, resource));
						observer.onSuccess(MessageType.UPDATE, resource, resource);
					} catch (IOException e) {
						LOGGER.error(String.format(UPDATE_ERROR_FORMAT, resource), e);
						observer.onError(MessageType.UPDATE, resource, MessageType.INTERNAL_SERVER_ERROR);
					}
				} else {
					LOGGER.error(String.format(RESOURCE_NOT_FOUND_FORMAT, resource));
					observer.onError(MessageType.UPDATE, resource, MessageType.NOT_FOUND);
				}
			}
		} catch (IOException e) {
			LOGGER.error(String.format(UPDATE_ERROR_FORMAT, resource), e);
		}
	}

}
