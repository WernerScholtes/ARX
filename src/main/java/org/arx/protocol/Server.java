package org.arx.protocol;

import org.arx.Endpoint;

/**
 * A server implements a communication protocol to receive requests from
 * clients. It converts these requests into calls to the {@link Endpoint}
 * interface that is implemented by a resource backend.
 */
public interface Server extends Runnable {
	/**
	 * Sets the backend object that shall be used to read, change and subscribe
	 * to resources.
	 * 
	 * @param backend
	 *            the backend object that this server shall use.
	 */
	void setBackend(Endpoint backend);
}
