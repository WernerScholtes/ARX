package org.arx.util;

import static org.junit.Assert.*;

import org.arx.Data;
import org.junit.Test;

public class TestByteArrayData {
	@Test
	public void testByteArrayData() {
		Data data = new ByteArrayData("text","Test".getBytes());
		assertEquals("text",data.getMimeType());
		assertEquals("Test",new String(data.getContent()));
		data.setMimeType("xml");
		assertEquals("xml",data.getMimeType());
		byte[] content = {0,0,0,0};
		data.setContent(content);
		assertEquals(content,data.getContent());
	}
	
	@Test
	public void testNull() {
		Data data = new ByteArrayData(null,null);
		assertNull(data.getMimeType());
		assertEquals(0,data.getContent().length);
	}
}
