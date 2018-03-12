package org.arx.backend.file;

import java.io.IOException;

import org.arx.Credentials;
import org.arx.Reason;
import org.arx.Resource;
import org.arx.Observer;
import org.arx.MessageType;
import org.arx.util.ResourceVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A subscribe request is used to subscribe to future changes of resources. It
 * is also possible to read the initial state of the subscribed resources.
 */
class SubscribeRequest implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(SubscribeRequest.class);
	private static final String READ_ERROR_FORMAT = "Cannot read initial state for resource pattern %1$s";
	private static final String FORBIDDEN_FORMAT = "Forbidden to subscribe to resource %1$s";
	private static final String SEND_ERROR = "Cannot send message to client";
	private Credentials credentials;
	private boolean readStatus;
	private Resource resource;
	private Subscriptions subscriptions;
	private SubscriptionObserver subscription;

	/**
	 * Creates a subscribe request for the specified parameters.
	 * 
	 * @param credentials
	 *            the credentials that are used to examine if the READ access
	 *            right is granted for the specified resources.
	 * @param readStatus
	 *            true, if the initial status of the resources shall be
	 *            returned, false otherwise
	 * @param resourcePattern
	 *            the resources to be created
	 * @param observer
	 *            the observer that is used for the response messages
	 * @param subscriptions
	 *            the set of all subscriptions
	 */
	public SubscribeRequest(Credentials credentials, boolean readStatus, Resource resourcePattern, Observer observer,
			Subscriptions subscriptions) {
		this.credentials = credentials;
		this.readStatus = readStatus;
		this.resource = resourcePattern;
		this.subscriptions = subscriptions;
		this.subscription = new SubscriptionObserver(credentials, observer, resourcePattern, readStatus);
		this.subscriptions.subscribe(this.subscription);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		if (!credentials.canRead(resource)) {
			LOGGER.error(String.format(FORBIDDEN_FORMAT, resource));
			try {
				subscription.onError(subscription.getRequest(), resource, MessageType.FORBIDDEN);
			} catch (IOException e) {
				LOGGER.error(SEND_ERROR, e);
			}
		} else {
			if (readStatus) {
				try {
					subscription.startBuffering();
					FileSystemWalker walker = new FileSystemWalker();
					walker.walkResource(resource, new ResourceVisitor() {
						@Override
						public void visitResource(Resource res) throws IOException {
							SubscriptionDataResponse response = new SubscriptionDataResponse(subscription, res,
									Reason.INITIAL);
							response.execute();
						}

					});
				} catch (IOException e) {
					subscriptions.unsubscribe(subscription);
					try {
						LOGGER.error(String.format(READ_ERROR_FORMAT, resource), e);
						subscription.onError(subscription.getRequest(), resource, MessageType.INTERNAL_SERVER_ERROR);
					} catch (IOException e1) {
						LOGGER.error(SEND_ERROR, e1);
					}
				} finally {
					try {
						subscription.stopBuffering();
					} catch (IOException e) {
						LOGGER.error(SEND_ERROR, e);
					}
				}
			}
		}
	}

}
