package org.arx;

import java.io.IOException;

/**
 * An observer object is used to receive responses to previously sent requests.
 * It is the counterpart of the {@link Endpoint} interface. A user of the
 * Endpoint interface must implement the Observer interface to obtain the
 * results of the method calls to the client interface .
 */
public interface Observer {
	/**
	 * Receives the response of a successfully finished request to the interface
	 * {@link Endpoint}.
	 * 
	 * @param request
	 *            the request type previously sent to the Endpoint interface
	 * @param resource
	 *            the resource or resource pattern that was part of the
	 *            corresponding request
	 * @param affectedResources
	 *            the resources that have been affected by the corresponding
	 *            request
	 * @throws IOException
	 *             if an IO error occurs while handling the response
	 */
	void onSuccess(MessageType request, Resource resource, Resource... affectedResources) throws IOException;

	/**
	 * Receives data or information about resources that are a result of a
	 * previously sent READ, SUBSCRIBE or SUBSCRIBE_STATUS request. The
	 * parameter reason specifies, why this method has been called and the
	 * parameter affectedResource specifies the resource that has been affected
	 * by the corresponding request. If the reason specifies DELETED the data
	 * parameter is null, otherwise it contains the created, updated or
	 * initially read content of the affected resource.
	 * 
	 * @param request
	 *            the request type previously sent to the Endpoint interface
	 * @param resource
	 *            the resource or resource pattern that was part of the
	 *            corresponding request
	 * @param reason
	 *            the reason why this method has been called
	 * @param affectedResource
	 *            the resource that has been affected by the corresponding
	 *            request
	 * @param data
	 *            the newly created, updated or initially read data or null, if
	 *            the reason specifies DELETED
	 * @throws IOException
	 *             if an IO error occurs while handling the response
	 */
	void onData(MessageType request, Resource resource, Reason reason, Resource affectedResource, Data data)
			throws IOException;

	/**
	 * Receives the response of a request to the interface {@link Endpoint} that
	 * resulted in an error.
	 * 
	 * @param request
	 *            the request type previously sent to the Endpoint interface
	 * @param resource
	 *            the resource or resource pattern that was part of the
	 *            corresponding request
	 * @param status
	 *            the error code describing the type of error that occurred
	 * @throws IOException
	 *             if an IO error occurs while handling the response
	 */
	void onError(MessageType request, Resource resource, MessageType status) throws IOException;
}
