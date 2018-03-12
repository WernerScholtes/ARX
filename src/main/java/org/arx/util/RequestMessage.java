package org.arx.util;

import java.nio.ByteBuffer;

import org.arx.Data;
import org.arx.Resource;
import org.arx.MessageType;

/**
 * A request message is used to serialize or de-serialize requests.
 */
public class RequestMessage extends Message {
	private MessageType request;
	private Resource resource;
	private Data data;

	/**
	 * Constructs a request message for serialization.
	 * 
	 * @param header
	 *            the header of the request message
	 * @param request
	 *            the request type
	 * @param resource
	 *            the resource of the request message or null
	 * @param data
	 *            the data of the request message or null
	 */
	public RequestMessage(Header header, MessageType request, Resource resource, Data data) {
		super(header);
		this.request = request;
		this.resource = resource;
		this.data = data;
	}

	/**
	 * Constructs a request message for de-serialization.
	 * 
	 * @param header
	 *            the header of the request message
	 * @param request
	 *            the request type
	 * @param buffer
	 *            the byte buffer which is used to de-serialize the request
	 *            message
	 */
	protected RequestMessage(Header header, MessageType request, ByteBuffer buffer) {
		super(header);
		this.request = request;
		switch (request) {
		case PING: // fall through
		case UNSUBSCRIBE_ALL:
			break;
		case DELETE: // fall through
		case READ: // fall through
		case SUBSCRIBE: // fall through
		case UNSUBSCRIBE: // fall through
		case SUBSCRIBE_STATUS:
			resource = readResource(buffer);
			break;
		case CREATE: // fall through
		case UPDATE: // fall through
		case SAVE:
			resource = readResource(buffer);
			data = readData(buffer);
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Returns the request type of this request message.
	 * 
	 * @return the request type of this request message.
	 */
	public MessageType getRequest() {
		return request;
	}

	/**
	 * Returns the resource of this request message.
	 * 
	 * @return the resource of this request message.
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * Returns the data of this request message.
	 * 
	 * @return the data of this request message.
	 */
	public Data getData() {
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.util.Message#toByteArray()
	 */
	@Override
	public byte[] toByteArray() {
		// Convert information to byte arrays
		byte[] headerBytes = toByteArray(header);
		byte[] resourceBytes = toByteArray(resource);
		byte[] dataBytes = toByteArray(data);
		// Calculate message size
		int size = 4 + 4; // Size of message + version number
		size += headerBytes.length; // Header
		size += 2; // Request code
		size += resourceBytes.length; // Resource name
		size += dataBytes.length; // Data
		ByteBuffer buffer = ByteBuffer.allocate(size);
		// Write size of message
		buffer.putInt(size - 4);
		// Write version number
		buffer.putInt(VERSION);
		// Write header
		buffer.put(headerBytes);
		// Write request code
		buffer.putShort(request.getCode());
		// Write resource
		buffer.put(resourceBytes);
		// Write data
		buffer.put(dataBytes);
		return buffer.array();
	}

}
