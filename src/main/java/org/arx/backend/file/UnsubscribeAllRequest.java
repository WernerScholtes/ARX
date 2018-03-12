package org.arx.backend.file;

import java.io.IOException;
import java.util.Set;

import org.arx.MessageType;
import org.arx.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An unsubscribe all request is used to asynchronously unsubscribe all
 * subscriptions for a specific observer.
 */
class UnsubscribeAllRequest implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(UnsubscribeAllRequest.class);
	private static final String SEND_ERROR = "Cannot send message to client";
	private Observer observer;
	private Subscriptions subscriptions;

	/**
	 * Unsubscribe all subscriptions for the specified observer.
	 * 
	 * @param observer
	 *            the observer whose subscriptions are to be unsubscribed
	 * @param subscriptions
	 *            the set of all subscriptions
	 */
	public UnsubscribeAllRequest(Observer observer, Subscriptions subscriptions) {
		this.observer = observer;
		this.subscriptions = subscriptions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		Set<SubscriptionObserver> subs = subscriptions.unsubscribeAll(observer);
		try {
			observer.onSuccess(MessageType.UNSUBSCRIBE_ALL, null);
			for (SubscriptionObserver sub : subs) {
				sub.onSuccess(sub.getRequest(), sub.getResourcePattern());
			}
		} catch (IOException e) {
			LOGGER.error(SEND_ERROR, e);
		}
	}

}
