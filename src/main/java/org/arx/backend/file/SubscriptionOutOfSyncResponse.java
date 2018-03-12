package org.arx.backend.file;

import java.io.IOException;

import org.arx.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An subscription out of sync response is sent to an observer whenever a
 * subscribed resource is out of sync.
 */
class SubscriptionOutOfSyncResponse implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionOutOfSyncResponse.class);
	private static final String SEND_ERROR = "Cannot send out of sync message";
	private Subscriptions subscriptions;
	private SubscriptionObserver subscription;

	/**
	 * Creates a subscription out of sync response for the specified parameters
	 * 
	 * @param subscriptions
	 *            the set of all subscriptions
	 * @param subscription
	 *            the subscription which is out of sync
	 */
	public SubscriptionOutOfSyncResponse(Subscriptions subscriptions, SubscriptionObserver subscription) {
		this.subscriptions = subscriptions;
		this.subscription = subscription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		subscriptions.unsubscribe(subscription);
		try {
			subscription.onError(subscription.getRequest(), subscription.getResourcePattern(), MessageType.OUT_OF_SYNC);
		} catch (IOException e) {
			LOGGER.error(SEND_ERROR);
		}
	}

}
