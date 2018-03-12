package org.arx;

/**
 * A message type describes a request or response type. The message type codes
 * lower than 200 describe request codes. All other message type codes are
 * response codes.
 */
public enum MessageType {
	/**
	 * Message type for a request to {@link Endpoint#ping Endpoint.ping(...)}.
	 */
	PING,
	/**
	 * Message type for a request to {@link Endpoint#create
	 * Endpoint.create(...)}.
	 */
	CREATE,
	/**
	 * Message type for a request to {@link Endpoint#update
	 * Endpoint.update(...)}.
	 */
	UPDATE,
	/**
	 * Message type for a request to {@link Endpoint#save Endpoint.save(...)}.
	 */
	SAVE,
	/**
	 * Message type for a request to {@link Endpoint#delete
	 * Endpoint.delete(...)}.
	 */
	DELETE,
	/**
	 * Message type for a request to {@link Endpoint#read Endpoint.read(...)}.
	 */
	READ,
	/**
	 * Message type for a request to {@link Endpoint#subscribe
	 * Endpoint.subscribe(...)}.
	 */
	SUBSCRIBE,
	/**
	 * Message type for a request to {@link Endpoint#subscribeStatus
	 * Endpoint.subscribeStatus(...)}.
	 */
	SUBSCRIBE_STATUS,
	/**
	 * Message type for a request to {@link Endpoint#unsubscribe
	 * Endpoint.unsubscribe(...)}.
	 */
	UNSUBSCRIBE,
	/**
	 * Message type for a request to {@link Endpoint#unsubscribeAll
	 * Endpoint.unsubscribeAll(...)}.
	 */
	UNSUBSCRIBE_ALL,
	/**
	 * Message type for a response message that corresponds to a successfully
	 * finished request.
	 */
	SUCCESS,
	/**
	 * Message type for a response message that contains data as a result of a
	 * previous READ, SUBSCRIBE or SUBSCRIBE_STATUS message. The corresponding
	 * request is not finished, yet.
	 */
	DATA,
	/**
	 * Message type for a response message that corresponds to a request
	 * finished with error. It informs the client about a malformed request.
	 * This is the case for a CREATE message that contains a resource pattern
	 * rather than a single resource.
	 */
	BAD_REQUEST,
	/**
	 * Message type for a response message that corresponds to a request
	 * finished with error. It is used if the client does not have the necessary
	 * authorization to perform an action on the specified resource or
	 * resources.
	 */
	FORBIDDEN,
	/**
	 * Message type for a response message that corresponds to a request
	 * finished with error. It is used to inform the client that the specified
	 * resource cannot be found. This can occur with DELETE, READ and UPDATE
	 * request specifying a single resource. It is also used if an UNSUBSCRIBE
	 * request has been sent and the corresponding SUBSCRIBE or SUBSCRIBE_STATUS
	 * request cannot be found.
	 */
	NOT_FOUND,
	/**
	 * Message type for a response message that corresponds to a request
	 * finished with error. It is used if a CREATE message refers to a resource
	 * that already exists.
	 */
	ALREADY_EXISTS,
	/**
	 * Message type for a response message that corresponds to a request
	 * finished with error. It is used if an internal server error occurred.
	 */
	INTERNAL_SERVER_ERROR,
	/**
	 * Message type for a response message that corresponds to a request
	 * finished with error. It is used if the server detects that a subscription
	 * (previous SUBSCRIBE or SUBSCRIBE_STATUS request) is out of sync.
	 */
	OUT_OF_SYNC;

	/**
	 * Code for the enum constant PING.
	 */
	public static final short PING_CODE = 0;
	/**
	 * Code for the enum constant CREATE.
	 */
	public static final short CREATE_CODE = 1;
	/**
	 * Code for the enum constant UPDATE.
	 */
	public static final short UPDATE_CODE = 2;
	/**
	 * Code for the enum constant SAVE.
	 */
	public static final short SAVE_CODE = 3;
	/**
	 * Code for the enum constant DELETE.
	 */
	public static final short DELETE_CODE = 4;
	/**
	 * Code for the enum constant READ.
	 */
	public static final short READ_CODE = 5;
	/**
	 * Code for the enum constant SUBSCRIBE.
	 */
	public static final short SUBSCRIBE_CODE = 6;
	/**
	 * Code for the enum constant SUBSCRIBE_STATUS.
	 */
	public static final short SUBSCRIBE_STATUS_CODE = 7;
	/**
	 * Code for the enum constant UNSUBSCRIBE.
	 */
	public static final short UNSUBSCRIBE_CODE = 8;
	/**
	 * Code for the enum constant UNSUBSCRIBE_ALL.
	 */
	public static final short UNSUBSCRIBE_ALL_CODE = 9;
	/**
	 * Code for the enum constant SUCCESS.
	 */
	public static final short SUCCESS_CODE = 200;
	/**
	 * Code for the enum constant DATA.
	 */
	public static final short DATA_CODE = 201;
	/**
	 * Code for the enum constant BAD_REQUEST.
	 */
	public static final short BAD_REQUEST_CODE = 400;
	/**
	 * Code for the enum constant FORBIDDEN.
	 */
	public static final short FORBIDDEN_CODE = 401;
	/**
	 * Code for the enum constant NOT_FOUND.
	 */
	public static final short NOT_FOUND_CODE = 402;
	/**
	 * Code for the enum constant ALREADY_EXISTS.
	 */
	public static final short ALREADY_EXISTS_CODE = 403;
	/**
	 * Code for the enum constant INTERNAL_SERVER_ERROR.
	 */
	public static final short INTERNAL_SERVER_ERROR_CODE = 500;
	/**
	 * Code for the enum constant OUT_OF_SYNC.
	 */
	public static final short OUT_OF_SYNC_CODE = 501;

	/**
	 * Returns the code for a message type
	 * 
	 * @return the code for a message type
	 * @throws IllegalStateException
	 *             if an unknown message type is used
	 */
	public short getCode() throws IllegalStateException {
		switch (this) {
		case PING:
			return PING_CODE;
		case CREATE:
			return CREATE_CODE;
		case UPDATE:
			return UPDATE_CODE;
		case SAVE:
			return SAVE_CODE;
		case DELETE:
			return DELETE_CODE;
		case READ:
			return READ_CODE;
		case SUBSCRIBE:
			return SUBSCRIBE_CODE;
		case UNSUBSCRIBE:
			return UNSUBSCRIBE_CODE;
		case SUBSCRIBE_STATUS:
			return SUBSCRIBE_STATUS_CODE;
		case UNSUBSCRIBE_ALL:
			return UNSUBSCRIBE_ALL_CODE;
		case SUCCESS:
			return SUCCESS_CODE;
		case DATA:
			return DATA_CODE;
		case BAD_REQUEST:
			return BAD_REQUEST_CODE;
		case FORBIDDEN:
			return FORBIDDEN_CODE;
		case NOT_FOUND:
			return NOT_FOUND_CODE;
		case ALREADY_EXISTS:
			return ALREADY_EXISTS_CODE;
		case INTERNAL_SERVER_ERROR:
			return INTERNAL_SERVER_ERROR_CODE;
		case OUT_OF_SYNC:
			return OUT_OF_SYNC_CODE;
		default:
			throw new IllegalStateException();
		}
	}

	/**
	 * Returns true, if this message type represents a request or false otherwise.
	 * That is equivalent to this.getCode() &lt; SUCCESS_CODE.
	 * 
	 * @return true, if this message type represents a request or false otherwise
	 */
	public boolean isRequest() {
		return getCode() < SUCCESS_CODE;
	}

	/**
	 * Returns true, if this message type represents a response or false otherwise.
	 * That is equivalent to this.getCode() &gt;= SUCCESS_CODE.
	 * 
	 * @return true, if this message type represents a response or false otherwise
	 */
	public boolean isResponse() {
		return getCode() >= SUCCESS_CODE;
	}

	/**
	 * Returns true, if this message type represents an error or false otherwise.
	 * That is equivalent to this.getCode() &gt;= BAD_REQUEST_CODE.
	 * 
	 * @return true, if this message type represents an error or false otherwise
	 */
	public boolean isError() {
		return getCode() >= BAD_REQUEST_CODE;
	}

	/**
	 * Returns the enum constant of this type for the specified code.
	 * 
	 * @param code
	 *            the code of the enum constant to be returned
	 * @return the enum constant for the specified code
	 * @throws IllegalArgumentException
	 *             if this enum type has no constant for the specified code
	 */
	public static MessageType valueOf(short code) throws IllegalArgumentException {
		switch (code) {
		case PING_CODE:
			return PING;
		case CREATE_CODE:
			return CREATE;
		case UPDATE_CODE:
			return UPDATE;
		case SAVE_CODE:
			return SAVE;
		case DELETE_CODE:
			return DELETE;
		case READ_CODE:
			return READ;
		case SUBSCRIBE_CODE:
			return SUBSCRIBE;
		case UNSUBSCRIBE_CODE:
			return UNSUBSCRIBE;
		case SUBSCRIBE_STATUS_CODE:
			return SUBSCRIBE_STATUS;
		case UNSUBSCRIBE_ALL_CODE:
			return UNSUBSCRIBE_ALL;
		case SUCCESS_CODE:
			return SUCCESS;
		case DATA_CODE:
			return DATA;
		case BAD_REQUEST_CODE:
			return BAD_REQUEST;
		case FORBIDDEN_CODE:
			return FORBIDDEN;
		case NOT_FOUND_CODE:
			return NOT_FOUND;
		case ALREADY_EXISTS_CODE:
			return ALREADY_EXISTS;
		case INTERNAL_SERVER_ERROR_CODE:
			return INTERNAL_SERVER_ERROR;
		case OUT_OF_SYNC_CODE:
			return OUT_OF_SYNC;
		default:
			throw new IllegalArgumentException();
		}
	}

}
