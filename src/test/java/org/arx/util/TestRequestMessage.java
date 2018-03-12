package org.arx.util;

import static org.junit.Assert.*;

import org.arx.Data;
import org.arx.MessageType;
import org.junit.Test;

public class TestRequestMessage {
	@Test
	public void testGetters() {
		Header header = new Header();
		header.put("key", "value");
		Data data = new ByteArrayData("text/plain","data".getBytes());
		RequestMessage rm = new RequestMessage(header,MessageType.SAVE,new SimpleResource("test"),data);
		assertEquals(1,rm.getHeader().size());
		assertEquals("value",rm.getHeaderField("key"));
		assertEquals(MessageType.SAVE,rm.getRequest());
		assertEquals("test",rm.getResource().getName());
		assertEquals("text/plain",rm.getData().getMimeType());
		assertEquals("data", new String(rm.getData().getContent()));
	}
	
	@Test
	public void testToByteArray() {
		Header header = new Header();
		header.put("key", "value");
		Data data = new ByteArrayData("text/plain","data".getBytes());
		RequestMessage rm = new RequestMessage(header,MessageType.SAVE,new SimpleResource("test"),data);
						//      SIZE   VERSION   HLENGHT H0KLENGTH         H0KEY H0VLENGTH                H0VAL
		byte[] expected = { 0,0,0,56,  0,0,0,1,  0,0,0,1,  0,0,0,3,  107,101,121,  0,0,0,5,  118,97,108,117,101,
				// SAVE   RLENGTH          RESOURCE    MLENGTH                               MIME-TYPE
				    0,3,  0,0,0,4,  116,101,115,116,  0,0,0,10,  116,101,120,116,47,112,108,97,105,110,
				// DLENGHT            DATA
				   0,0,0,4,  100,97,116,97};
		byte[] bytes = rm.toByteArray();
		assertArrayEquals(expected,bytes);
	}
}
