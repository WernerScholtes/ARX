package org.arx;

/**
 * A credential can be used to query the access grants for a single resource or
 * a resource pattern.
 */
public interface Credentials {
	/**
	 * Returns true, if the CREATE access is granted for the specified resource
	 * or resource pattern and false otherwise. The CREATE access for a resource
	 * or resource pattern is needed to execute the method
	 * {@link Endpoint#create Endpoint.create(...)}.
	 * 
	 * @param resourcePattern
	 *            the resource or resource pattern for which the access grant is
	 *            checked
	 * @return true, if the CREATE access is granted for the specified resource
	 *         or resource pattern and false otherwise
	 */
	boolean canCreate(Resource resourcePattern);

	/**
	 * Returns true, if the UPDATE access is granted for the specified resource
	 * or resource pattern and false otherwise. The UPDATE access for a resource
	 * or resource pattern is needed to execute the method
	 * {@link Endpoint#update Endpoint.update(...)}.
	 * 
	 * @param resourcePattern
	 *            the resource or resource pattern for which the access grant is
	 *            checked
	 * @return true, if the UPDATE access is granted for the specified resource
	 *         or resource pattern and false otherwise
	 */
	boolean canUpdate(Resource resourcePattern);

	/**
	 * Returns true, if the DELETE access is granted for the specified resource
	 * or resource pattern and false otherwise. The DELETE access for a resource
	 * or resource pattern is needed to execute the method
	 * {@link Endpoint#delete Endpoint.delete(...)}.
	 * 
	 * @param resourcePattern
	 *            the resource or resource pattern for which the access grant is
	 *            checked
	 * @return true, if the DELETE access is granted for the specified resource
	 *         or resource pattern and false otherwise
	 */
	boolean canDelete(Resource resourcePattern);

	/**
	 * Returns true, if the READ access is granted for the specified resource or
	 * resource pattern and false otherwise. The READ access for a resource or
	 * resource pattern is needed to execute the methods {@link Endpoint#read
	 * Endpoint.read(...)}, {@link Endpoint#subscribe Endpoint.subscribe(...)}
	 * or {@link Endpoint#subscribeStatus Endpoint.subscribeStatus(...)}.
	 * 
	 * @param resourcePattern
	 *            the resource or resource pattern for which the access grant is
	 *            checked
	 * @return true, if the READ access is granted for the specified resource or
	 *         resource pattern and false otherwise
	 */
	boolean canRead(Resource resourcePattern);

	/**
	 * Returns true, if the SAVE access is granted for the specified resource or
	 * resource pattern and false otherwise. The SAVE access is granted, if the
	 * CREATE and UPDATE access is granted. The SAVE access for a resource or
	 * resource pattern is needed to execute the method {@link Endpoint#save
	 * Endpoint.save(...)}.
	 * 
	 * @param resourcePattern
	 *            the resource or resource pattern for which the access grant is
	 *            checked
	 * @return true, if the SAVE access is granted for the specified resource or
	 *         resource pattern and false otherwise
	 */
	default boolean canSave(Resource resourcePattern) {
		return canCreate(resourcePattern) && canUpdate(resourcePattern);
	}
	
	/**
	 * Returns a serialization string for this credentials.
	 * @return a serialization string for this credentials.
	 */
	String serialize();
}
