package org.arx.backend.file;

import java.io.IOException;

import org.arx.Resource;
import org.arx.Observer;
import org.arx.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An unsubscribe request is used to asynchronously remove a subscription.
 */
class UnsubscribeRequest implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(UnsubscribeRequest.class);
	private static final String SEND_ERROR = "Cannot send message to client";
	private Resource resource;
	private Subscriptions subscriptions;
	private Observer observer;

	/**
	 * Creates an unsubscribe request for the specified observer and resource
	 * pattern
	 * 
	 * @param resourcePattern
	 *            the resource pattern to be unsubscribed
	 * @param observer
	 *            the observer to be unsubscribed
	 * @param subscriptions
	 *            the set of all subscriptions
	 */
	public UnsubscribeRequest(Resource resourcePattern, Observer observer, Subscriptions subscriptions) {
		this.resource = resourcePattern;
		this.subscriptions = subscriptions;
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
			SubscriptionObserver sub = subscriptions
					.unsubscribe(new SubscriptionObserver(null, observer, resource, false));
			if (sub != null) {
				observer.onSuccess(MessageType.UNSUBSCRIBE, resource);
				sub.onSuccess(sub.getRequest(), sub.getResourcePattern());
			} else {
				observer.onError(MessageType.UNSUBSCRIBE, resource, MessageType.NOT_FOUND);
			}
		} catch (IOException e) {
			LOGGER.error(SEND_ERROR, e);
		}
	}

}
