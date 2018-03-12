package org.arx;

/**
 * Contains data to be exchanged between ARX applications and servers. A data
 * object contains an optional mime type and option content.
 */
public interface Data {
	/**
	 * Returns the mime type of this data object or null if the mime type is not
	 * specified.
	 * 
	 * @return the mime type of this data object or null if the mime type is not
	 *         specified.
	 */
	String getMimeType();

	/**
	 * Sets the mime type of this data object.
	 * 
	 * @param mimeType
	 *            the mime type of this data object
	 */
	void setMimeType(String mimeType);

	/**
	 * Returns the content of this data object as byte array or null if the
	 * content is not specified.
	 * 
	 * @return the content of this data object as byte array or null if the
	 *         content is not specified.
	 */
	byte[] getContent();

	/**
	 * Sets the content of this data object.
	 * 
	 * @param content
	 *            the content of this data object
	 */
	void setContent(byte[] content);
}
