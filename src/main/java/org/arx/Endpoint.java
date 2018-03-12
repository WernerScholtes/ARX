package org.arx;

import java.io.IOException;

/**
 * An endpoint is an object that allows asynchronous access to resources. Every
 * request is started asynchronously by invoking one of the methods of the
 * endpoint. The outcome of every request will be sent to an {@link Observer}
 * object.
 * <p>
 * Endpoints are used as a central access point for ARX clients and as internal
 * interfaces used by ARX servers to separate the protocol layer from the
 * backend layer.
 */
public interface Endpoint extends Runnable {
	/**
	 * Creates a single resource. If the credentials allow to create the
	 * specified resource, the resource is created and populated with the
	 * specified data. Otherwise the resource is not created. The specified
	 * observer is then notified about the outcome of the create request by
	 * calling either {@link Observer#onSuccess Observer.onSuccess(...)}, if the
	 * request could be finished successfully, or by calling
	 * {@link Observer#onError Observer.onError(...)}, if an error occurred
	 * during execution of the request. Unlike the other methods of this
	 * interface, the create method can only operate on a single resource and
	 * not on a resource pattern. The following error codes can occur (specified
	 * as {@link MessageType}):
	 * 
	 * <ul>
	 * <li>ALREADY_EXISTS: if the resource already exists
	 * <li>BAD_REQUEST: if a resource pattern has been specified instead of a
	 * single resource
	 * <li>FORBIDDEN: if the credentials do not allow to create the specified
	 * resource
	 * <li>INTERNAL_SERVER_ERROR: if an error occurred while creating the
	 * resource
	 * </ul>
	 * 
	 * @param credentials
	 *            credentials used to determine whether the creation of the
	 *            specified resource is allowed
	 * @param resource
	 *            resource to be created
	 * @param data
	 *            data to be used as content for the resource
	 * @param observer
	 *            observer to be notified about the outcome of the request
	 * @throws IOException
	 *             if an IO error occurs while handling the request.
	 */
	void create(Credentials credentials, Resource resource, Data data, Observer observer) throws IOException;

	/**
	 * Updates a single resource or all resources of a resource pattern. If the
	 * credentials allow to update the specified resources the resources are
	 * updated by overwriting their content with the specified data. Otherwise
	 * the resources are not created. The specified observer is then notified
	 * about the outcome of the update request by calling either
	 * {@link Observer#onSuccess Observer.onSuccess(...)}, if the request could
	 * be finished successfully, or by calling {@link Observer#onError
	 * Observer.onError(...)}, if an error occurred during execution of the
	 * request. The following error codes can occur (specified as {@link MessageType}
	 * ):
	 * 
	 * <ul>
	 * <li>FORBIDDEN: if the credentials do not allow to update the specified
	 * resources
	 * <li>INTERNAL_SERVER_ERROR: if an error occurred while updating the
	 * resources
	 * <li>NOT_FOUND: if a single resource can not be found
	 * </ul>
	 * 
	 * @param credentials
	 *            credentials used to determine whether the update of the
	 *            specified resources is allowed
	 * @param resourcePattern
	 *            resources to be updated
	 * @param data
	 *            data to be used as content for the resources
	 * @param observer
	 *            observer to be notified about the outcome of the request
	 * @throws IOException
	 *             if an IO error occurs while handling the request.
	 */
	void update(Credentials credentials, Resource resourcePattern, Data data, Observer observer) throws IOException;

	/**
	 * Saves a single resource or all resources of a resource pattern. If the
	 * credentials allow to create and update the specified resources the
	 * resources are saved by overwriting their content with the specified data.
	 * Otherwise the resources are not saved. The specified observer is then
	 * notified about the outcome of the save request by calling either
	 * {@link Observer#onSuccess Observer.onSuccess(...)}, if the request could
	 * be finished successfully, or by calling {@link Observer#onError
	 * Observer.onError(...)}, if an error occurred during execution of the
	 * request. The following error codes can occur (specified as {@link MessageType}
	 * ):
	 * 
	 * <ul>
	 * <li>FORBIDDEN: if the credentials do not allow to create and update the
	 * specified resources
	 * <li>INTERNAL_SERVER_ERROR: if an error occurred while writing the
	 * resources
	 * </ul>
	 * 
	 * @param credentials
	 *            credentials used to determine whether the update and creation
	 *            of the specified resources is allowed
	 * @param resourcePattern
	 *            resources to be saved
	 * @param data
	 *            data to be used as content for the resources
	 * @param observer
	 *            observer to be notified about the outcome of the request
	 * @throws IOException
	 *             if an IO error occurs while handling the request.
	 */
	void save(Credentials credentials, Resource resourcePattern, Data data, Observer observer) throws IOException;

	/**
	 * Deletes a single resource or all resources of a resource pattern. If the
	 * credentials allow to delete the specified resources the resources are
	 * deleted. Otherwise the resources are not deleted. The specified observer
	 * is then notified about the outcome of the delete request by calling
	 * either {@link Observer#onSuccess Observer.onSuccess(...)}, if the request
	 * could be finished successfully, or by calling {@link Observer#onError
	 * Observer.onError(...)}, if an error occurred during execution of the
	 * request. The following error codes can occur (specified as {@link MessageType}
	 * ):
	 * 
	 * <ul>
	 * <li>FORBIDDEN: if the credentials do not allow to delete the specified
	 * resources
	 * <li>INTERNAL_SERVER_ERROR: if an error occurred while deleting the
	 * resources
	 * <li>NOT_FOUND: if a single resource can not be found
	 * </ul>
	 * 
	 * @param credentials
	 *            credentials used to determine whether the deletion of the
	 *            specified resources is allowed
	 * @param resourcePattern
	 *            resources to be deleted
	 * @param observer
	 *            observer to be notified about the outcome of the request
	 * @throws IOException
	 *             if an IO error occurs while handling the request.
	 */
	void delete(Credentials credentials, Resource resourcePattern, Observer observer) throws IOException;

	/**
	 * Reads a single resource or all resources of a resource pattern. If the
	 * credentials allow to read the specified resources the resources are read.
	 * Otherwise the resources are not read. For every resource that can be read
	 * successfully the method {@link Observer#onData Observer.onData(...)} is
	 * called. After successfully reading all specified resources, the method
	 * {@link Observer#onSuccess Observer.onSuccess(...)} is called. If the
	 * request could not be finished successfully the method
	 * {@link Observer#onError Observer.onError(...)} is called. The following
	 * error codes can occur (specified as {@link MessageType}):
	 * 
	 * <ul>
	 * <li>FORBIDDEN: if the credentials do not allow to read the specified
	 * resources
	 * <li>INTERNAL_SERVER_ERROR: if an error occurred while reading the
	 * resources
	 * <li>NOT_FOUND: if a single resource can not be found
	 * </ul>
	 * 
	 * @param credentials
	 *            credentials used to determine whether the reading of the
	 *            specified resources is allowed
	 * @param resourcePattern
	 *            resources to be read
	 * @param observer
	 *            observer to be notified about the outcome of the request
	 * @throws IOException
	 *             if an IO error occurs while handling the request.
	 */
	void read(Credentials credentials, Resource resourcePattern, Observer observer) throws IOException;

	/**
	 * Subscribes to a single resource or to all resources of a resource
	 * pattern. If the credentials allow to read the specified resources the
	 * resources are subscribed. Otherwise the resources are not subscribed. For
	 * every unique combination of resourcePattern and observer only one
	 * subscription will be maintained. A method call with a combination of
	 * resourcePattern and observer that already exists, will overwrite the
	 * existing subscription. A subscription to a single resource or resource
	 * pattern means that whenever one of the specified resources is created,
	 * updated or deleted, the specified observer will be called. For every
	 * resource that will be created, updated or deleted the method
	 * {@link Observer#onData Observer.onData(...)} will be called. The methods
	 * {@link Endpoint#unsubscribe unsubscribe(...)} or
	 * {@link Endpoint#unsubscribeAll unsubscribeAll(...)} can be called to
	 * unsubscribe a previous subscription. If the request could not be finished
	 * successfully the method {@link Observer#onError Observer.onError(...)} is
	 * called. If the credentials for reading the specified resources expire,
	 * the subscription will be unsubscribed automatically and call
	 * Observer.onError(...)the method Observer.onError(...) will be called. The
	 * following error codes can occur (specified as {@link MessageType}):
	 * 
	 * <ul>
	 * <li>FORBIDDEN: if the credentials do not allow to read the specified
	 * resources or if the credentials have expired during an already running
	 * subscription
	 * <li>INTERNAL_SERVER_ERROR: if an error occurred while reading the content
	 * of the resources
	 * <li>OUT_OF_SYNC: if the subscription is out of sync
	 * </ul>
	 * 
	 * If, in addition to future changes to the resources, the initial content
	 * of the resources is to be read as well call the method
	 * {@link Endpoint#subscribeStatus subscribeStatus(...)} instead.
	 * 
	 * @param credentials
	 *            credentials used to determine whether the reading of the
	 *            specified resources is allowed
	 * @param resourcePattern
	 *            resources to be subscribed
	 * @param observer
	 *            observer to be notified about the outcome of the request
	 * @throws IOException
	 *             if an IO error occurs while handling the request.
	 */
	void subscribe(Credentials credentials, Resource resourcePattern, Observer observer) throws IOException;

	/**
	 * Subscribes to a single resource or to all resources of a resource
	 * pattern. In addition to future changes of resources, also the initial
	 * content of the specified resources will be read. If the credentials allow
	 * to read the specified resources the resources are read and subscribed.
	 * Otherwise the resources are neither read nor subscribed. For every unique
	 * combination of resourcePattern and observer only one subscription will be
	 * maintained. A method call with a combination of resourcePattern and
	 * observer that already exists, will overwrite the existing subscription. A
	 * subscription to a single resource or resource pattern means that whenever
	 * one of the specified resources is created, updated or deleted, the
	 * specified observer will be called. For every resource whose initial
	 * content is read and that will be created, updated or deleted the method
	 * {@link Observer#onData Observer.onData(...)} will be called. The methods
	 * {@link Endpoint#unsubscribe unsubscribe(...)} or
	 * {@link Endpoint#unsubscribeAll unsubscribeAll(...)} can be called to
	 * unsubscribe a previous subscription. If the request could not be finished
	 * successfully the method {@link Observer#onError Observer.onError(...)} is
	 * called. If the credentials for reading the specified resources expire,
	 * the subscription will be unsubscribed automatically and call
	 * Observer.onError(...) the method Observer.onError(...) will be called.
	 * The following error codes can occur (specified as {@link MessageType}):
	 * 
	 * <ul>
	 * <li>FORBIDDEN: if the credentials do not allow to read the specified
	 * resources or if the credentials have expired during an already running
	 * subscription
	 * <li>INTERNAL_SERVER_ERROR: if an error occurred while reading the content
	 * of the resources
	 * <li>OUT_OF_SYNC: if the subscription is out of sync
	 * </ul>
	 * 
	 * @param credentials
	 *            credentials used to determine whether the reading of the
	 *            specified resources is allowed
	 * @param resourcePattern
	 *            resources to be read and subscribed
	 * @param observer
	 *            observer to be notified about the outcome of the request
	 * @throws IOException
	 *             if an IO error occurs while handling the request.
	 */
	void subscribeStatus(Credentials credentials, Resource resourcePattern, Observer observer) throws IOException;

	/**
	 * Cancels a previous subscription specified by a resource pattern and an
	 * observer. The subscription to be cancelled will be identified by the
	 * combination of resourcePattern and observer. If the subscription cannot
	 * be found the method {@link Observer#onError Observer.onError(...)} is
	 * called with a status code of NOT_FOUND. Otherwise the method
	 * {@link Observer#onSuccess Observer.onSuccess(...)} is called.
	 * 
	 * @param credentials
	 *            credentials used to determine whether the cancellation of the
	 *            subscription of the specified resources is allowed
	 * @param resourcePattern
	 *            the resource pattern used to identify the subscription to be
	 *            cancelled
	 * @param observer
	 *            the observer used to identify the subscription to be cancelled
	 * @throws IOException
	 *             if an IO error occurs while handling the request.
	 */
	void unsubscribe(Credentials credentials, Resource resourcePattern, Observer observer) throws IOException;

	/**
	 * Cancels all subscriptions for the specified observer. As a result of this
	 * operation the method {@link Observer#onSuccess Observer.onSuccess(...)}
	 * is called.
	 * 
	 * @param credentials
	 *            credentials used to determine whether the cancellation of all
	 *            subscriptions for the specified observer is allowed
	 * @param observer
	 *            the observer used to identify the subscriptions to be
	 *            cancelled
	 * @throws IOException
	 *             if an IO error occurs while handling the request.
	 */
	void unsubscribeAll(Credentials credentials, Observer observer) throws IOException;

	/**
	 * Sends a ping request. As a result of this operation the method
	 * {@link Observer#onSuccess Observer.onSuccess(...)} is called.
	 * 
	 * @param credentials
	 *            credentials used to determine whether a ping request is
	 *            allowed
	 * @param observer
	 *            the observer that shall receive the response
	 * @throws IOException
	 *             if an IO error occurs while handling the request.
	 */
	void ping(Credentials credentials, Observer observer) throws IOException;
}
