package org.arx;

/**
 * Describes the reason why data is sent to an observer. It is used as parameter
 * in the method {@link Observer#onData Observer.onData(...)} which is called as
 * a result of a previous call to one of the methods {@link Endpoint#read
 * Endpoint.read(...)}, {@link Endpoint#subscribe Endpoint.subscribe(...)} or
 * {@link Endpoint#subscribeStatus Endpoint.subscribeStatus(...)}.
 */
public enum Reason {
	/**
	 * This reason will be used, if initial data of a resource has been read in
	 * response to a {@link Endpoint#read Endpoint.read(...)} or
	 * {@link Endpoint#subscribeStatus Endpoint.subscribeStatus(...)} request.
	 */
	INITIAL,
	/**
	 * This reason will be used, if data of a newly created resource has been
	 * read in response to a {@link Endpoint#subscribe Endpoint.subscribe(...)}
	 * or {@link Endpoint#subscribeStatus Endpoint.subscribeStatus(...)}
	 * request.
	 */
	CREATED,
	/**
	 * This reason will be used, if data of an updated resource has been read in
	 * response to a {@link Endpoint#subscribe Endpoint.subscribe(...)} or
	 * {@link Endpoint#subscribeStatus Endpoint.subscribeStatus(...)} request.
	 */
	UPDATED,
	/**
	 * This reason will be used to notify an {@link Observer} of a deleted
	 * resource.
	 */
	DELETED;

	/**
	 * Code for the enum constant INITIAL.
	 */
	public static final byte INITIAL_CODE = 0;
	/**
	 * Code for the enum constant CREATED.
	 */
	public static final byte CREATED_CODE = 1;
	/**
	 * Code for the enum constant UPDATED.
	 */
	public static final byte UPDATED_CODE = 2;
	/**
	 * Code for the enum constant DELETED.
	 */
	public static final byte DELETED_CODE = 3;

	/**
	 * Returns the code for this enum constant.
	 * 
	 * @return the code for this enum constant
	 * @throws IllegalStateException
	 *             if the code of this enum constant is unknown
	 */
	public byte getCode() throws IllegalStateException {
		switch (this) {
		case INITIAL:
			return INITIAL_CODE;
		case CREATED:
			return CREATED_CODE;
		case UPDATED:
			return UPDATED_CODE;
		case DELETED:
			return DELETED_CODE;
		default:
			throw new IllegalStateException();
		}
	}

	/**
	 * Returns the enum constant of this type with the specified code. If the
	 * specified code is unknown an IllegalArgumentException is thrown.
	 * 
	 * @param code
	 *            the code of the enum constant to be returned
	 * @return the enum constant with the specified code.
	 * @throws IllegalArgumentException
	 *             if the specified code is unknown
	 */
	public static Reason valueOf(byte code) throws IllegalArgumentException {
		switch (code) {
		case INITIAL_CODE:
			return INITIAL;
		case CREATED_CODE:
			return CREATED;
		case UPDATED_CODE:
			return UPDATED;
		case DELETED_CODE:
			return DELETED;
		default:
			throw new IllegalArgumentException();
		}
	}
}
