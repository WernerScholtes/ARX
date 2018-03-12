package org.arx.util;

import java.nio.ByteBuffer;

import org.arx.Data;
import org.arx.Reason;
import org.arx.Resource;
import org.arx.MessageType;

/**
 * A response message is used to serialize or de-serialize messages.
 */
public class ResponseMessage extends Message {
	private MessageType response;
	private MessageType request;
	private Resource resource;
	private Reason reason;
	private Data data;
	private Resource[] affectedResources;

	/**
	 * Constructs a response message for serialization
	 * 
	 * @param header
	 *            the header of the response message
	 * @param response
	 *            the response type of the response message
	 * @param request
	 *            the request type of the corresponding request message
	 * @param resource
	 *            the resource of the corresponding request message
	 * @param reason
	 *            the reason for transmitting data
	 * @param data
	 *            transmitted data of the response message
	 * @param affectedResources
	 *            the affected resources of the response message
	 */
	public ResponseMessage(Header header, MessageType response, MessageType request, Resource resource, Reason reason,
			Data data, Resource... affectedResources) {
		super(header);
		this.response = response;
		this.request = request;
		this.resource = resource;
		this.reason = reason;
		this.data = data;
		if (affectedResources != null && affectedResources.length == 0) {
			this.affectedResources = null;
		} else {
			this.affectedResources = affectedResources;
		}
	}

	/**
	 * Constructs a response message for de-serialization
	 * 
	 * @param header
	 *            the header of the response message
	 * @param response
	 *            the response type
	 * @param buffer
	 *            the byte buffer which is used to de-serialize the response
	 *            message
	 */
	protected ResponseMessage(Header header, MessageType response, ByteBuffer buffer) {
		super(header);
		this.response = response;
		this.request = readMessageType(buffer);
		if (this.request != MessageType.PING && this.request != MessageType.UNSUBSCRIBE_ALL) {
			this.resource = readResource(buffer);
			switch (response) {
			case SUCCESS: // fall through
				this.affectedResources = readResources(buffer);
				break;
			case DATA:
				this.reason = readReason(buffer);
				if (this.reason != Reason.DELETED) {
					this.data = readData(buffer);
				}
				this.affectedResources = readResources(buffer);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Returns the response type of this response message.
	 * 
	 * @return the response type of this response message.
	 */
	public MessageType getResponse() {
		return response;
	}

	/**
	 * Returns the request type of the corresponding request message.
	 * 
	 * @return the request type of the corresponding request message.
	 */
	public MessageType getRequest() {
		return request;
	}

	/**
	 * Returns the resource of the corresponding request message.
	 * 
	 * @return the resource of the corresponding request message or null, if
	 *         this response message does not contain a resource.
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * Returns the reason for transmitting data.
	 * 
	 * @return the reason for transmitting data or null, if this response
	 *         message does not contain a reason.
	 */
	public Reason getReason() {
		return reason;
	}

	/**
	 * Returns data of the response message.
	 * 
	 * @return data of the response message or null, if this response message
	 *         has no data.
	 */
	public Data getData() {
		return data;
	}

	/**
	 * Returns the affected resources of this response message.
	 * 
	 * @return the affected resources of this response message or null, if this
	 *         response message does not contain affected resources.
	 */
	public Resource[] getAffectedResources() {
		return affectedResources;
	}

	/**
	 * Returns the first affected resource of this response message.
	 * 
	 * @return the first affected resource of this response message or null if
	 *         this response message contains more or less than one affected
	 *         resource.
	 */
	public Resource getAffectedResource() {
		if (affectedResources != null && affectedResources.length == 1) {
			return affectedResources[0];
		}
		return null;
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
		byte[] affectedResourcesBytes = toByteArray(affectedResources);
		// Calculate message size
		int size = 4 + 4; // Size of message + version number
		size += headerBytes.length; // header
		size += 2; // Response code
		size += 2; // Request code
		size += resourceBytes.length; // Resource name
		if (reason != null) {
			size += 1; // Size of reason
		}
		size += dataBytes.length; // Data
		size += affectedResourcesBytes.length; // Affected resources
		ByteBuffer buffer = ByteBuffer.allocate(size);
		// Write size of message
		buffer.putInt(size - 4);
		// Write version number
		buffer.putInt(VERSION);
		// Write header
		buffer.put(headerBytes);
		// Write response code
		buffer.putShort(response.getCode());
		// Write request code
		buffer.putShort(request.getCode());
		// Write resource
		buffer.put(resourceBytes);
		// Write reason
		if (reason != null) {
			buffer.put(reason.getCode());
		}
		// Write data
		buffer.put(dataBytes);
		// Write affected resources
		buffer.put(affectedResourcesBytes);
		return buffer.array();
	}

}
