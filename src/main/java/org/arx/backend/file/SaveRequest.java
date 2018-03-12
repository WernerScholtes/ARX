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
 * A save request is used to asynchronously save (create or update) resources
 */
class SaveRequest implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(SaveRequest.class);
	private static final String SAVE_SUCCESS_FORMAT = "Successfully saved resource %1$s";
	private static final String SAVE_ERROR_FORMAT = "Cannot save resource %1$s";
	private static final String FORBIDDEN_FORMAT = "Forbidden to save resource %1$s";
	private Credentials credentials;
	private Resource resource;
	private Data data;
	private Observer observer;

	/**
	 * Creates a save request for the specified parameters
	 * 
	 * @param credentials
	 *            the credentials that are used to examine if the CREATE and
	 *            UPDATE access rights are granted for the specified resource.
	 * @param resourcePattern
	 *            the resources to be saved
	 * @param data
	 *            the data content for the resources that shall be saved
	 * @param observer
	 *            the observer that is used for the response messages
	 */
	public SaveRequest(Credentials credentials, Resource resourcePattern, Data data, Observer observer) {
		this.credentials = credentials;
		this.resource = resourcePattern;
		this.data = data;
		this.observer = observer;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			if (!credentials.canSave(resource)) {
				LOGGER.error(String.format(FORBIDDEN_FORMAT, resource));
				observer.onError(MessageType.SAVE, resource, MessageType.FORBIDDEN);
			} else if (resource.isPattern()) {
				final List<Resource> affectedResources = new LinkedList<Resource>();
				FileSystemWalker walker = new FileSystemWalker(false);
				try {
					walker.walkResource(resource, new ResourceVisitor() {
						@Override
						public void visitResource(Resource res) throws IOException {
							Path path = FileSystemFactory.getPath(res);
							Path parent = path.getParent();
							if (Files.notExists(parent)) {
								parent.toFile().mkdirs();
							}
							Files.write(path, data.getContent());
							LOGGER.debug(SAVE_SUCCESS_FORMAT, res);
							affectedResources.add(res);
						}
					});
					observer.onSuccess(MessageType.SAVE, resource,
							affectedResources.toArray(new Resource[affectedResources.size()]));
				} catch (IOException e) {
					LOGGER.error(String.format(SAVE_ERROR_FORMAT, resource), e);
					observer.onError(MessageType.SAVE, resource, MessageType.INTERNAL_SERVER_ERROR);
				}
			} else {
				Path path = FileSystemFactory.getPath(resource);
				if (Files.exists(path)) {
					try {
						Files.write(path, data.getContent());
						LOGGER.debug(String.format(SAVE_SUCCESS_FORMAT, resource));
						observer.onSuccess(MessageType.SAVE, resource, resource);
					} catch (IOException e) {
						LOGGER.error(String.format(SAVE_ERROR_FORMAT, resource), e);
						observer.onError(MessageType.SAVE, resource, MessageType.INTERNAL_SERVER_ERROR);
					}
				} else {
					Path parent = path.getParent();
					try {
						if (Files.notExists(parent)) {
							parent.toFile().mkdirs();
						}
						Files.write(path, data.getContent());
						LOGGER.debug(SAVE_SUCCESS_FORMAT, resource);
						observer.onSuccess(MessageType.SAVE, resource, resource);
					} catch (IOException e) {
						LOGGER.error(String.format(SAVE_ERROR_FORMAT, resource), e);
						observer.onError(MessageType.SAVE, resource, MessageType.INTERNAL_SERVER_ERROR);
					}
				}
			}
		} catch (IOException e) {
			LOGGER.error(String.format(SAVE_ERROR_FORMAT, resource), e);
		}
	}

}
