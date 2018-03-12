package org.arx.protocol;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;

import org.arx.Endpoint;

/**
 * A factory to create protocol-specific objects. A protocol is used to
 * communicate between an ARX client and an ARX server. ARX does not prescribe
 * any specific protocol. Instead, it allows to implement any protocol that
 * facilitates the transfer of resource content. A protocol-specific client
 * implements the {@link Endpoint} interface and allows the application program
 * to read, subscribe and change resources. The client sends the requests to a
 * server using the implemented protocol. A protocol-specific server implements
 * the protocol by receiving requests from clients and converts them into calls
 * to the Endpoint interface which is supplied by a resource-backend.
 */
public interface ProtocolFactory {
	/**
	 * Creates a protocol-specific server
	 * 
	 * @param executor
	 *            the executor the server shall use to asynchronously execute
	 *            its operations.
	 * @param parameters
	 *            parameters used to configure protocol-specific settings
	 * @return a newly created server object
	 * @throws IOException
	 *             if an IO error occurs during creation of the server object
	 */
	Server createServer(Executor executor, Map<String, String> parameters) throws IOException;

	/**
	 * Creates a protocol-specific client.
	 * 
	 * @param parameters
	 *            parameters used to configure protocol-specific settings
	 * @return a newly created Endpoint object that implements the client
	 * @throws IOException
	 *             if an IO error occurs during creation of the client object
	 */
	Endpoint createClient(Map<String, String> parameters) throws IOException;
}
