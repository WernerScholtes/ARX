package org.arx.util;

import org.arx.MessageType;
import org.arx.Resource;

/**
 * A request resource is a combination of a request typw and a resource. This
 * combination is used to uniquely identify a request.
 */
public class RequestResource {
	private MessageType request;
	private Resource resource;

	/**
	 * Constructs a request-resource combination
	 * 
	 * @param request
	 *            the request type to be used
	 * @param resource
	 *            the resource to be used or null, if no resource is specified
	 */
	public RequestResource(MessageType request, Resource resource) {
		this.request = request;
		if (resource != null) {
			this.resource = resource;
		} else {
			this.resource = new SimpleResource();
		}
	}

	/**
	 * Returns the request type of this request-resource combination.
	 * 
	 * @return the request type of this request-resource combination.
	 */
	public MessageType getRequest() {
		return request;
	}

	/**
	 * Returns the resource of this request-resource combination.
	 * 
	 * @return the resource of this request-resource combination.
	 */
	public Resource getResource() {
		return resource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return request.name() + " " + resource.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RequestResource) {
			RequestResource other = (RequestResource) obj;
			return this.toString().equals(other.toString());
		}
		return false;
	}
}
