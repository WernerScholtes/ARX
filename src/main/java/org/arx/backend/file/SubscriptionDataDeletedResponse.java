package org.arx.backend.file;

import java.io.IOException;

import org.arx.Resource;
import org.arx.Reason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A subscription data deleted response is sent to an observer whenever a
 * subscribed resource has been deleted.
 */
class SubscriptionDataDeletedResponse implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionDataDeletedResponse.class);
	private static final String DELETED_SUCCESS_FORMAT = "Successfully sent deleted notification for resource %1$s";
	private static final String DELETED_ERROR_FORMAT = "Cannot send deleted message for resource %1$s";
	private SubscriptionObserver subscription;
	private Resource resource;

	/**
	 * Creates a subscription data deleted response for the specified
	 * parameters.
	 * 
	 * @param subscription
	 *            the subscription that is used for the response message
	 * @param resource
	 *            the resource that has been deleted
	 */
	public SubscriptionDataDeletedResponse(SubscriptionObserver subscription, Resource resource) {
		this.subscription = subscription;
		this.resource = resource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			subscription.onData(subscription.getRequest(), subscription.getResourcePattern(), Reason.DELETED, resource,
					null);
			LOGGER.debug(String.format(DELETED_SUCCESS_FORMAT, resource));
		} catch (IOException e) {
			LOGGER.error(String.format(DELETED_ERROR_FORMAT, resource), e);
		}
	}

}
