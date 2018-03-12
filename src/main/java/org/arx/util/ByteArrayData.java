package org.arx.util;

import org.arx.Data;

/**
 * A simple byte array backed implementation of the Data interface.
 */
public class ByteArrayData implements Data {
	private String mimeType;
	private byte[] content;

	/**
	 * Constructs a byte array for the specified mime type and content.
	 * 
	 * @param mimeType
	 *            mime type for this data object
	 * @param content
	 *            content for this data object
	 */
	public ByteArrayData(String mimeType, byte[] content) {
		setMimeType(mimeType);
		setContent(content);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Data#getMimeType()
	 */
	@Override
	public String getMimeType() {
		return mimeType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Data#setMimeType(java.lang.String)
	 */
	@Override
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Data#getContent()
	 */
	@Override
	public byte[] getContent() {
		return content;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Data#setContent(byte[])
	 */
	@Override
	public void setContent(byte[] content) {
		if (content == null) {
			this.content = new byte[0];
		} else {
			this.content = content;
		}
	}

}
