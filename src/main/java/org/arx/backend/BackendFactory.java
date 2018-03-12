package org.arx.backend;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;

import org.arx.Data;
import org.arx.Endpoint;
import org.arx.Resource;

/**
 * Factory to create backend specific objects. A backend is an
 * {@link org.arx.Endpoint} interface that implements the handling of a specific
 * resource type.
 */
public interface BackendFactory {
	/**
	 * Creates a backend object that implements the {@link org.arx.Endpoint}
	 * interface.
	 * 
	 * @param executor
	 *            the executor the backend shall use to asynchronously execute
	 *            its operations.
	 * @param parameters
	 *            parameters used to configure backend-specific settings
	 * @return a newly created backend object
	 * @throws IOException
	 *             if an IO error occurs during creation of the backend object
	 */
	Endpoint createBackend(Executor executor, Map<String, String> parameters) throws IOException;

	/**
	 * Creates a backend-specific Resource object for the specified name
	 * 
	 * @param name
	 *            the name of the Resource object to be created
	 * @return backend-specific Resource object
	 */
	Resource createResource(String name);

	/**
	 * Creates a backend-specific Data object for the specified mime-type and
	 * data content
	 * 
	 * @param mimeType
	 *            the mime-type to be used for the Data object creation
	 * @param data
	 *            the data content for the backend-specific Data object
	 * @return backend-specific Data object
	 */
	Data createData(String mimeType, byte[] data);
}
