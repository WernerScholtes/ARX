package org.arx.backend.file;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.arx.Credentials;
import org.arx.Data;
import org.arx.Reason;
import org.arx.Resource;
import org.arx.Observer;
import org.arx.MessageType;

/**
 * A subscription observer is an observer that is used for subscriptions. It
 * implements an internal queue for ResponseMessages. When a subscription is
 * used to read the initial content of resources all data messages other than
 * those with reason INITIAL will be buffered until the initial read is
 * finished.
 */
class SubscriptionObserver implements Observer {
	private Credentials credentials;
	private Observer observer;
	private Resource resourcePattern;
	private boolean readStatus;
	private Queue<BufferEntry> buffer;

	/**
	 * Creates a subscription observer for the specified parameters
	 * 
	 * @param credentials
	 *            the credentials that are used to examine if the READ access
	 *            right is granted for the specified resources.
	 * @param observer
	 *            the observer that is used for the response message
	 * @param resourcePattern
	 *            the resources that are subscribed
	 * @param readStatus
	 *            true, if the initial status of the resources shall be
	 *            returned, false otherwise
	 */
	public SubscriptionObserver(Credentials credentials, Observer observer, Resource resourcePattern,
			boolean readStatus) {
		this.credentials = credentials;
		this.observer = observer;
		this.resourcePattern = resourcePattern;
		this.readStatus = readStatus;
		this.buffer = null;
	}

	/**
	 * Returns the credentials used for this subscription
	 * 
	 * @return the credentials used for this subscription
	 */
	public Credentials getCredentials() {
		return credentials;
	}

	/**
	 * Returns the observer used for this subscription
	 * 
	 * @return the observer used for this subscription
	 */
	public Observer getObserver() {
		return observer;
	}

	/**
	 * Returns the resource pattern used for this subscription
	 * 
	 * @return the resource pattern used for this subscription
	 */
	public Resource getResourcePattern() {
		return resourcePattern;
	}

	/**
	 * Returns the request types (SUBSCRIBE or SUBSCRIBE_STATUS) used for this
	 * subscription
	 * 
	 * @return the request types used for this subscription
	 */
	public MessageType getRequest() {
		if (readStatus) {
			return MessageType.SUBSCRIBE_STATUS;
		}
		return MessageType.SUBSCRIBE;
	}

	/**
	 * Starts buffering of {@link #onData onData(...)} messages that do not have
	 * INITIAL as reason.
	 */
	public synchronized void startBuffering() {
		this.buffer = new ConcurrentLinkedQueue<BufferEntry>();
	}

	/**
	 * Stops buffering of {@link #onData onData(...)} messages. All buffered
	 * messages will be sent to the observer.
	 * 
	 * @throws IOException
	 *             if the buffered messages cannot be sent to the observer.
	 */
	public synchronized void stopBuffering() throws IOException {
		while (buffer != null && !buffer.isEmpty()) {
			BufferEntry entry = buffer.poll();
			sendEntry(entry.getReason(), entry.getResource(), entry.getData());
		}
		buffer = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Observer#onSuccess(org.arx.MessageType, org.arx.Resource,
	 * org.arx.Resource[])
	 */
	@Override
	public void onSuccess(MessageType request, Resource resource, Resource... affectedResources) throws IOException {
		stopBuffering();
		observer.onSuccess(request, resource, affectedResources);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Observer#onData(org.arx.MessageType, org.arx.Resource,
	 * org.arx.Reason, org.arx.Resource, org.arx.Data)
	 */
	@Override
	public void onData(MessageType request, Resource resource, Reason reason, Resource affectedResource, Data data)
			throws IOException {
		if (reason == Reason.INITIAL) {
			observer.onData(request, resource, reason, affectedResource, data);
		} else {
			handleEntry(reason, affectedResource, data);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Observer#onError(org.arx.MessageType, org.arx.Resource,
	 * org.arx.MessageType)
	 */
	@Override
	public void onError(MessageType request, Resource resource, MessageType errorCode) throws IOException {
		stopBuffering();
		observer.onError(request, resource, errorCode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SubscriptionObserver) {
			SubscriptionObserver other = (SubscriptionObserver) obj;
			return this.toString().equals(other.toString());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.resourcePattern.getName() + "#" + observer.toString();
	}

	private synchronized void handleEntry(Reason reason, Resource affectedResource, Data data) throws IOException {
		if (buffer != null) {
			buffer.add(new BufferEntry(reason, affectedResource, data));
		} else {
			sendEntry(reason, affectedResource, data);
		}
	}

	private void sendEntry(Reason reason, Resource affectedResource, Data data) throws IOException {
		observer.onData(getRequest(), resourcePattern, reason, affectedResource, data);
	}

	/**
	 * A buffer entry used to buffer data messages with reason CREATED, UPDATED
	 * or DELETED.
	 */
	private static class BufferEntry {
		private Reason reason;
		private Resource resource;
		private Data data;

		/**
		 * Creates a buffer entry for the specified parameters
		 * 
		 * @param reason
		 *            the reason of the data message
		 * @param resource
		 *            the resource the data message belongs to
		 * @param data
		 *            the data of the message
		 */
		public BufferEntry(Reason reason, Resource resource, Data data) {
			this.reason = reason;
			this.resource = resource;
			this.data = data;
		}

		/**
		 * Returns the reason of the buffer entry.
		 * 
		 * @return the reason of the buffer entry.
		 */
		public Reason getReason() {
			return reason;
		}

		/**
		 * Returns the resource of the buffer entry.
		 * 
		 * @return the resource of the buffer entry.
		 */
		public Resource getResource() {
			return resource;
		}

		/**
		 * Returns the data object of the buffer entry.
		 * 
		 * @return the data object of the buffer entry.
		 */
		public Data getData() {
			return data;
		}

	}

}
