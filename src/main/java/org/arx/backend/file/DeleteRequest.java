package org.arx.backend.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import org.arx.Credentials;
import org.arx.Resource;
import org.arx.Observer;
import org.arx.MessageType;
import org.arx.util.ResourceVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A delete request is used to execute the asynchronous deletion of resources.
 */
class DeleteRequest implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(DeleteRequest.class);
	private static final String RESOURCE_NOT_FOUND_FORMAT = "Resource %1$s not found";
	private static final String DELETE_SUCCESS_FORMAT = "Successfully deleted resource %1$s";
	private static final String DELETE_ERROR_FORMAT = "Cannot delete resource %1$s";
	private static final String FORBIDDEN_FORMAT = "Forbidden to delete resource %1$s";
	private Credentials credentials;
	private Resource resource;
	private Observer observer;

	/**
	 * Creates a delete request object for the specified parameters.
	 * 
	 * @param credentials
	 *            the credentials that are used to examine if the DELETE access
	 *            right is granted for the specified resources.
	 * @param resourcePattern
	 *            the resources to be deleted
	 * @param observer
	 *            the observer that is used for the response message
	 */
	public DeleteRequest(Credentials credentials, Resource resourcePattern, Observer observer) {
		this.credentials = credentials;
		this.resource = resourcePattern;
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
			if (!credentials.canDelete(resource)) {
				LOGGER.error(String.format(FORBIDDEN_FORMAT, resource));
				observer.onError(MessageType.DELETE, resource, MessageType.FORBIDDEN);
			} else if (resource.isPattern()) {
				final List<Resource> affectedResources = new LinkedList<Resource>();
				FileSystemWalker walker = new FileSystemWalker();
				try {
					walker.walkResource(resource, new ResourceVisitor() {
						@Override
						public void visitResource(Resource res) throws IOException {
							Path path = FileSystemFactory.getPath(res);
							Files.delete(path);
							LOGGER.debug(DELETE_SUCCESS_FORMAT, res);
							affectedResources.add(res);
						}
					});
					observer.onSuccess(MessageType.DELETE, resource,
							affectedResources.toArray(new Resource[affectedResources.size()]));
				} catch (IOException e) {
					LOGGER.error(String.format(DELETE_ERROR_FORMAT, resource), e);
					observer.onError(MessageType.DELETE, resource, MessageType.INTERNAL_SERVER_ERROR);
				}
			} else {
				Path path = FileSystemFactory.getPath(resource);
				if (Files.exists(path)) {
					try {
						Files.delete(path);
						LOGGER.debug(String.format(DELETE_SUCCESS_FORMAT, resource));
						observer.onSuccess(MessageType.DELETE, resource, resource);
					} catch (IOException e) {
						LOGGER.error(String.format(DELETE_ERROR_FORMAT, resource), e);
						observer.onError(MessageType.DELETE, resource, MessageType.INTERNAL_SERVER_ERROR);
					}
				} else {
					LOGGER.error(String.format(RESOURCE_NOT_FOUND_FORMAT, resource));
					observer.onError(MessageType.DELETE, resource, MessageType.NOT_FOUND);
				}
			}
		} catch (IOException e) {
			LOGGER.error(String.format(DELETE_ERROR_FORMAT, resource), e);
		}
	}

}
