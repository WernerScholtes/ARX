package org.arx.backend.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.arx.Data;
import org.arx.Reason;
import org.arx.Resource;
import org.arx.util.ByteArrayData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A subscription data response is sent to an observer whenever a subscribed
 * resource shall be read initially or has been created or updated.
 */
class SubscriptionDataResponse implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionDataResponse.class);
	private static final String READ_SUCCESS_FORMAT = "Successfully read resource %1$s";
	private static final String READ_ERROR_FORMAT = "Cannot read resource %1$s";
	private SubscriptionObserver subscription;
	private Resource resource;
	private Reason reason;

	/**
	 * Creates a subscription data response for the specified parameters
	 * 
	 * @param subscription
	 *            the subscription that is used for the response message
	 * @param resource
	 *            the resource that has been deleted
	 * @param reason
	 *            the reason for the response (INITIAL, CREATED or UPDATED)
	 */
	public SubscriptionDataResponse(SubscriptionObserver subscription, Resource resource, Reason reason) {
		this.subscription = subscription;
		this.resource = resource;
		this.reason = reason;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			execute();
		} catch (IOException e) {
			LOGGER.error(String.format(READ_ERROR_FORMAT, resource), e);
		}
	}

	/**
	 * Executes this subscription data request
	 * 
	 * @throws IOException
	 *             if an IO error occurs while reading the resource
	 */
	public void execute() throws IOException {
		Path path = FileSystemFactory.getPath(resource);
		if (Files.exists(path)) {
			byte[] bytes = Files.readAllBytes(path);
			String mimeType = FileSystemFactory.getMimeType(resource);
			Data data = new ByteArrayData(mimeType, bytes);
			subscription.onData(subscription.getRequest(), subscription.getResourcePattern(), reason, resource, data);
			LOGGER.debug(String.format(READ_SUCCESS_FORMAT, subscription.getResourcePattern()));
		}
	}

}
