package org.arx.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.arx.Data;
import org.arx.MessageType;
import org.arx.Reason;
import org.junit.Test;

public class TestResponseMessage {
	@Test
	public void testGetters() {
		Header header = new Header();
		header.put("key", "value");
		Data data = new ByteArrayData("text/plain","data".getBytes());
		ResponseMessage rm = new ResponseMessage(header,MessageType.DATA,MessageType.READ,new SimpleResource("test"),Reason.INITIAL,data,new SimpleResource("test"));
		assertEquals(1,rm.getHeader().size());
		assertEquals("value",rm.getHeaderField("key"));
		assertEquals(MessageType.DATA,rm.getResponse());
		assertEquals(MessageType.READ,rm.getRequest());
		assertEquals("test",rm.getResource().getName());
		assertEquals(Reason.INITIAL,rm.getReason());
		assertEquals("text/plain",rm.getData().getMimeType());
		assertEquals("data", new String(rm.getData().getContent()));
		assertEquals("test",rm.getAffectedResource().getName());
		assertEquals(1,rm.getAffectedResources().length);
	}
	
	@Test
	public void testToByteArray() {
		Header header = new Header();
		header.put("key", "value");
		Data data = new ByteArrayData("text/plain","data".getBytes());
		ResponseMessage rm = new ResponseMessage(header,MessageType.DATA,MessageType.READ,new SimpleResource("test"),Reason.INITIAL,data,new SimpleResource("test"));
					   //      SIZE   VERSION   HLENGHT H0KLENGTH         H0KEY H0VLENGTH                H0VAL
		byte[] expected = {0,0,0,71,  0,0,0,1,  0,0,0,1,  0,0,0,3,  107,101,121,  0,0,0,5,  118,97,108,117,101,
				// DATA  READ   RLENGTH          RESOURCE INITIAL    MLENGTH                               MIME-TYPE
 				  0,-55,  0,5,  0,0,0,4,  116,101,115,116,      0,  0,0,0,10,  116,101,120,116,47,112,108,97,105,110,
 				// DLENGTH            DATA   ALENGTH    A0SIZE                A0
				   0,0,0,4,  100,97,116,97,  0,0,0,1,  0,0,0,4,  116,101,115,116};
		byte[] bytes = rm.toByteArray();
		assertArrayEquals(expected,bytes);
	}

}
