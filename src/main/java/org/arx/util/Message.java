package org.arx.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.arx.Data;
import org.arx.Reason;
import org.arx.Resource;
import org.arx.MessageType;

/**
 * A message is used to serialize or de-serialize requests and responses.
 */
public abstract class Message {
	public static final int VERSION = 1;
	protected Header header;

	/**
	 * Constructs a message with the specified header.
	 * 
	 * @param header
	 *            the header of the message or null, if no header shall be used.
	 *            If the header is null, an emptz header will be created.
	 */
	public Message(Header header) {
		if (header == null) {
			header = new Header();
		}
		this.header = header;
	}

	/**
	 * Returns the header of the message
	 * 
	 * @return the header of the message
	 */
	public Header getHeader() {
		return header;
	}

	/**
	 * Returns the header field to which the specified key is mapped, or null if
	 * this header contains no mapping for the key.
	 * 
	 * @param key
	 *            the key whose associated header field is to be returned
	 * @return the header field to which the specified key is mapped, or null if
	 *         this header contains no mapping for the key
	 */
	public String getHeaderField(String key) {
		if (header != null) {
			return header.get(key);
		}
		return null;
	}

	/**
	 * Serializes this message into a byte array
	 * @return a byte array containing the serialized message
	 */
	public abstract byte[] toByteArray();

	/**
	 * Deserializes a message from a data input stream.
	 * @param in the data input stream from which the message is to be read
	 * @return the message that has been deserialized from the data input stream
	 * @throws IOException if an IO error occurs during deserialization
	 */
	public static Message createFromStream(DataInputStream in) throws IOException {
		// Read size of message
		int messageSize = in.readInt();
		byte[] message = new byte[messageSize];
		int bytesRead = in.read(message);
		if (bytesRead != messageSize) {
			throw new ProtocolException();
		}
		ByteBuffer buffer = ByteBuffer.wrap(message);
		int version = buffer.getInt();
		if (version != VERSION) {
			throw new ProtocolException();
		}
		Header header = readHeader(buffer);
		MessageType messageType = readMessageType(buffer);
		if (messageType.isRequest()) {
			return new RequestMessage(header, messageType, buffer);
		} else {
			return new ResponseMessage(header, messageType, buffer);
		}
	}

	protected static byte[] toByteArray(String string) {
		if (string == null) {
			return new byte[0];
		}
		return toByteArray(string.getBytes(StandardCharsets.UTF_8));
	}
	
	protected static byte[] toByteArray(String string,boolean returnSize) {
		if (string == null) {
			string = "";
		}
		return toByteArray(string.getBytes(StandardCharsets.UTF_8));
	}
	
	protected static byte[] toByteArray(byte[] bytes) {
		if ( bytes == null ) {
			return new byte[0];
		}
		ByteBuffer buffer = ByteBuffer.allocate(bytes.length+4);
		buffer.putInt(bytes.length);
		buffer.put(bytes);
		return buffer.array();
	}

	protected static byte[] toByteArray(byte[] bytes,boolean returnSize) {
		if ( bytes == null ) {
			bytes = new byte[0];
		}
		ByteBuffer buffer = ByteBuffer.allocate(bytes.length+4);
		buffer.putInt(bytes.length);
		buffer.put(bytes);
		return buffer.array();
	}

	protected static byte[] toByteArray(Header header) {
		if (header == null) {
			return new byte[0];
		}
		List<byte[]> arrays = new LinkedList<byte[]>();
		int size = 0;
		for (Map.Entry<String, String> entry : header.entrySet()) {
			String key = entry.getKey();
			byte[] keyBytes = toByteArray(key);
			size += keyBytes.length;
			arrays.add(keyBytes);
			String value = entry.getValue();
			byte[] valueBytes = toByteArray(value);
			size += valueBytes.length;
			arrays.add(valueBytes);
		}
		ByteBuffer buffer = ByteBuffer.allocate(size+4);
		buffer.putInt(header.size());
		for (byte[] array : arrays) {
			buffer.put(array);
		}
		return buffer.array();
	}

	protected static byte[] toByteArray(Resource[] resources) {
		if (resources == null) {
			return new byte[0];
		}
		List<byte[]> arrays = new LinkedList<byte[]>();
		int size = 0;
		for (Resource resource : resources) {
			String name = resource.getName();
			byte[] nameBytes = toByteArray(name);
			size += nameBytes.length;
			arrays.add(nameBytes);
		}
		ByteBuffer buffer = ByteBuffer.allocate(size+4);
		buffer.putInt(resources.length);
		for (byte[] array : arrays) {
			buffer.put(array);
		}
		return buffer.array();
	}

	protected static byte[] toByteArray(Resource resource) {
		if (resource == null) {
			return new byte[0];
		}
		return toByteArray(resource.getName());
	}
	
	protected static byte[] toByteArray(Data data) {
		if ( data == null ) {
			return new byte[0];
		}
		byte[] mimeTypeBytes = toByteArray(data.getMimeType(),true);
		byte[] contentBytes = toByteArray(data.getContent(),true);
		ByteBuffer buffer = ByteBuffer.allocate(mimeTypeBytes.length + contentBytes.length);
		buffer.put(mimeTypeBytes);
		buffer.put(contentBytes);
		return buffer.array();
	}

	protected static Header readHeader(ByteBuffer buffer) {
		int headerNum = buffer.getInt();
		Header header = new Header();
		for (int i = 0; i < headerNum; ++i) {
			String key = readString(buffer);
			String value = readString(buffer);
			header.put(key, value);
		}
		return header;
	}

	protected static Resource readResource(ByteBuffer buffer) {
		String resourceName = readString(buffer);
		return new SimpleResource(resourceName);
	}

	protected static Data readData(ByteBuffer buffer) {
		String mimeType = readString(buffer);
		byte[] bytes = readByteArray(buffer);
		return new ByteArrayData(mimeType, bytes);
	}

	protected static String readString(ByteBuffer buffer) {
		byte[] bytes = readByteArray(buffer);
		return new String(bytes, StandardCharsets.UTF_8);
	}

	protected static byte[] readByteArray(ByteBuffer buffer) {
		int size = buffer.getInt();
		byte[] bytes = new byte[size];
		buffer.get(bytes);
		return bytes;
	}

	protected static Reason readReason(ByteBuffer buffer) {
		byte code = buffer.get();
		return Reason.valueOf(code);
	}

	protected static MessageType readMessageType(ByteBuffer buffer) {
		short code = buffer.getShort();
		return MessageType.valueOf(code);
	}

	protected static Resource[] readResources(ByteBuffer buffer) {
		if ( buffer.position() == buffer.limit() ) {
			return null;
		}
		int num = buffer.getInt();
		Resource[] resources = new Resource[num];
		for (int i = 0; i < num; ++i) {
			resources[i] = readResource(buffer);
		}
		return resources;
	}

}
