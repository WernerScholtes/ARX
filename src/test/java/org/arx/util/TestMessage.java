package org.arx.util;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.arx.MessageType;
import org.arx.Reason;
import org.junit.Test;

public class TestMessage {
	@Test
	public void testPingMessage() throws IOException {
		//                      MSIZE   VERSION    HEADER  PING
		byte[] serialized = {0,0,0,10,  0,0,0,1,  0,0,0,0,  0,0};
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(serialized));
		Message message = Message.createFromStream(in);
		assertEquals(RequestMessage.class.getName(),message.getClass().getName());
		RequestMessage rm = (RequestMessage)message;
		assertEquals(0,rm.getHeader().size());
		assertNull(rm.getResource());
		assertEquals(MessageType.PING,rm.getRequest());
		assertNull(rm.getData());
	}
	
	@Test
	public void testCreateMessage() throws IOException {
		//                      MSIZE   VERSION    HEADER CREATE   RLENGTH          RESOURCE
		byte[] serialized = {0,0,0,30,  0,0,0,1,  0,0,0,0,   0,1,  0,0,0,4,  116,101,115,116,
				// MLENGTH   DLENGTH            DATA
				   0,0,0,0,  0,0,0,4,  100,97,116,97};
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(serialized));
		Message message = Message.createFromStream(in);
		assertEquals(RequestMessage.class.getName(),message.getClass().getName());
		RequestMessage rm = (RequestMessage)message;
		assertEquals(0,rm.getHeader().size());
		assertEquals("test",rm.getResource().getName());
		assertEquals(MessageType.CREATE,rm.getRequest());
		assertEquals(0,rm.getData().getMimeType().length());
		assertEquals(4,rm.getData().getContent().length);
		assertEquals("data",new String(rm.getData().getContent()));
	}
	
	@Test
	public void testUpdateMessage() throws IOException {
		//                      MSIZE   VERSION   HLENGHT H0KLENGTH H0KEY H0VLENGTH H0VAL
		byte[] serialized = {0,0,0,40,  0,0,0,1,  0,0,0,1,  0,0,0,1,  107,  0,0,0,1,  118,
				// UPDATE   RLENGTH          RESOURCE   MLENGTH   DLENGTH            DATA
				      0,2,  0,0,0,4,  116,101,115,116,  0,0,0,0,  0,0,0,4,  100,97,116,97};
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(serialized));
		Message message = Message.createFromStream(in);
		assertEquals(RequestMessage.class.getName(),message.getClass().getName());
		RequestMessage rm = (RequestMessage)message;
		assertEquals(1,rm.getHeader().size());
		assertEquals("v",rm.getHeaderField("k"));
		assertEquals("test",rm.getResource().getName());
		assertEquals(MessageType.UPDATE,rm.getRequest());
		assertEquals(0,rm.getData().getMimeType().length());
		assertEquals(4,rm.getData().getContent().length);
		assertEquals("data",new String(rm.getData().getContent()));
	}
	
	@Test
	public void testSaveMessage() throws IOException {
		//                      MSIZE   VERSION   HLENGHT H0KLENGTH H0KEY H0VLENGTH H0VAL
		byte[] serialized = {0,0,0,50,  0,0,0,1,  0,0,0,1,  0,0,0,1,  107,  0,0,0,1,  118,
				// SAVE   RLENGTH          RESOURCE    MLENGTH                               MIME-TYPE
				    0,3,  0,0,0,4,  116,101,115,116,  0,0,0,10,  116,101,120,116,47,112,108,97,105,110,
				// DLENGTH            DATA
				   0,0,0,4,  100,97,116,97};
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(serialized));
		Message message = Message.createFromStream(in);
		assertEquals(RequestMessage.class.getName(),message.getClass().getName());
		RequestMessage rm = (RequestMessage)message;
		assertEquals(1,rm.getHeader().size());
		assertEquals("v",rm.getHeaderField("k"));
		assertEquals("test",rm.getResource().getName());
		assertEquals(MessageType.SAVE,rm.getRequest());
		assertEquals("text/plain",rm.getData().getMimeType());
		assertEquals(4,rm.getData().getContent().length);
		assertEquals("data",new String(rm.getData().getContent()));
	}
	
	@Test
	public void testReadMessage() throws IOException {
		//					      SIZE   VERSION    HEADER  READ    LENGTH        RESOURCE
		byte[] serialized = { 0,0,0,18,  0,0,0,1,  0,0,0,0,  0,5,  0,0,0,4,  100,97,116,97};
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(serialized));
		Message message = Message.createFromStream(in);
		assertEquals(RequestMessage.class.getName(),message.getClass().getName());
		RequestMessage rm = (RequestMessage)message;
		assertEquals(0,rm.getHeader().size());
		assertEquals("data",rm.getResource().getName());
		assertEquals(MessageType.READ,rm.getRequest());
		assertNull(rm.getData());
	}
	
	@Test
	public void testSubscribeMessage() throws IOException {
		//					      SIZE   VERSION    HEADER SUBSCRIBE    LENGTH        RESOURCE
		byte[] serialized = { 0,0,0,18,  0,0,0,1,  0,0,0,0,      0,6,  0,0,0,4,  100,97,116,97};
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(serialized));
		Message message = Message.createFromStream(in);
		assertEquals(RequestMessage.class.getName(),message.getClass().getName());
		RequestMessage rm = (RequestMessage)message;
		assertEquals(0,rm.getHeader().size());
		assertEquals("data",rm.getResource().getName());
		assertEquals(MessageType.SUBSCRIBE,rm.getRequest());
		assertNull(rm.getData());
	}
	
	@Test
	public void testSubscribeStatusMessage() throws IOException {
		//					      SIZE   VERSION    HEADER SUBSCRIBE_STATUS    LENGTH        RESOURCE
		byte[] serialized = { 0,0,0,18,  0,0,0,1,  0,0,0,0,             0,7,  0,0,0,4,  100,97,116,97};
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(serialized));
		Message message = Message.createFromStream(in);
		assertEquals(RequestMessage.class.getName(),message.getClass().getName());
		RequestMessage rm = (RequestMessage)message;
		assertEquals(0,rm.getHeader().size());
		assertEquals("data",rm.getResource().getName());
		assertEquals(MessageType.SUBSCRIBE_STATUS,rm.getRequest());
		assertNull(rm.getData());
	}
	
	@Test
	public void testUnsubscribeMessage() throws IOException {
		//					      SIZE   VERSION    HEADER UNSUBSCRIBE    LENGTH        RESOURCE
		byte[] serialized = { 0,0,0,18,  0,0,0,1,  0,0,0,0,        0,8,  0,0,0,4,  100,97,116,97};
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(serialized));
		Message message = Message.createFromStream(in);
		assertEquals(RequestMessage.class.getName(),message.getClass().getName());
		RequestMessage rm = (RequestMessage)message;
		assertEquals(0,rm.getHeader().size());
		assertEquals("data",rm.getResource().getName());
		assertEquals(MessageType.UNSUBSCRIBE,rm.getRequest());
		assertNull(rm.getData());
	}
	
	@Test
	public void testUnsubscribeAllMessage() throws IOException {
		//					      SIZE   VERSION    HEADER UNSUBSCRIBE_ALL
		byte[] serialized = { 0,0,0,10,  0,0,0,1,  0,0,0,0,            0,9};
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(serialized));
		Message message = Message.createFromStream(in);
		assertEquals(RequestMessage.class.getName(),message.getClass().getName());
		RequestMessage rm = (RequestMessage)message;
		assertEquals(0,rm.getHeader().size());
		assertNull(rm.getResource());
		assertEquals(MessageType.UNSUBSCRIBE_ALL,rm.getRequest());
		assertNull(rm.getData());
	}
	
	@Test
	public void testPingResponseMessage() throws IOException {
		//                      MSIZE   VERSION    HEADER SUCCESS  PING
		byte[] serialized = {0,0,0,12,  0,0,0,1,  0,0,0,0,  0,-56,  0,0};
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(serialized));
		Message message = Message.createFromStream(in);
		assertEquals(ResponseMessage.class.getName(),message.getClass().getName());
		ResponseMessage rm = (ResponseMessage)message;
		assertEquals(0,rm.getHeader().size());
		assertEquals(MessageType.SUCCESS,rm.getResponse());
		assertEquals(MessageType.PING,rm.getRequest());
		assertNull(rm.getResource());
		assertNull(rm.getData());
		assertNull(rm.getReason());
		assertNull(rm.getAffectedResources());
	}
	
	@Test
	public void testSuccessMessage() throws IOException {
		//                      MSIZE   VERSION    HEADER SUCCESS  READ   RLENGTH          RESOURCE
		byte[] serialized = {0,0,0,20,  0,0,0,1,  0,0,0,0,  0,-56,  0,5,  0,0,0,4,  116,101,115,116};
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(serialized));
		Message message = Message.createFromStream(in);
		assertEquals(ResponseMessage.class.getName(),message.getClass().getName());
		ResponseMessage rm = (ResponseMessage)message;
		assertEquals(0,rm.getHeader().size());
		assertEquals(MessageType.SUCCESS,rm.getResponse());
		assertEquals(MessageType.READ,rm.getRequest());
		assertEquals("test",rm.getResource().getName());
		assertNull(rm.getData());
		assertNull(rm.getReason());
		assertNull(rm.getAffectedResources());
	}
	
	@Test
	public void testDataMessage() throws IOException {
		//                      MSIZE   VERSION    HEADER    DATA  READ   RLENGTH          RESOURCE
		byte[] serialized = {0,0,0,55,  0,0,0,1,  0,0,0,0,  0,-55,  0,5,  0,0,0,4,  116,101,115,116,
				// INITIAL    MLENGTH                               MIME-TYPE
				         0,  0,0,0,10,  116,101,120,116,47,112,108,97,105,110,
				// DLENGTH            DATA   ALENGTH    A0SIZE                A0
				   0,0,0,4,  100,97,116,97,  0,0,0,1,  0,0,0,4,  116,101,115,116};
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(serialized));
		Message message = Message.createFromStream(in);
		assertEquals(ResponseMessage.class.getName(),message.getClass().getName());
		ResponseMessage rm = (ResponseMessage)message;
		assertEquals(0,rm.getHeader().size());
		assertEquals(MessageType.DATA,rm.getResponse());
		assertEquals(MessageType.READ,rm.getRequest());
		assertEquals("test",rm.getResource().getName());
		assertEquals("text/plain",rm.getData().getMimeType());
		assertEquals(4,rm.getData().getContent().length);
		assertEquals(Reason.INITIAL,rm.getReason());
		assertEquals("test",rm.getAffectedResource().getName());
	}
	
	
	@Test
	public void testDeletedMessage() throws IOException {
		//                      MSIZE   VERSION    HEADER    DATA  READ   RLENGTH          RESOURCE
		byte[] serialized = {0,0,0,33,  0,0,0,1,  0,0,0,0,  0,-55,  0,5,  0,0,0,4,  116,101,115,116,
				// DELETED   ALENGTH    A0SIZE                A0
				         3,  0,0,0,1,  0,0,0,4,  116,101,115,116};
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(serialized));
		Message message = Message.createFromStream(in);
		assertEquals(ResponseMessage.class.getName(),message.getClass().getName());
		ResponseMessage rm = (ResponseMessage)message;
		assertEquals(0,rm.getHeader().size());
		assertEquals(MessageType.DATA,rm.getResponse());
		assertEquals(MessageType.READ,rm.getRequest());
		assertEquals("test",rm.getResource().getName());
		assertNull(rm.getData());
		assertEquals(Reason.DELETED,rm.getReason());
		assertEquals("test",rm.getAffectedResource().getName());
	}
	
	@Test
	public void testErrorMessage() throws IOException {
		//                      MSIZE   VERSION    HEADER BAD_REQUEST  READ   RLENGTH          RESOURCE
		byte[] serialized = {0,0,0,20,  0,0,0,1,  0,0,0,0,     1,-112,  0,5,  0,0,0,4,  116,101,115,116};
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(serialized));
		Message message = Message.createFromStream(in);
		assertEquals(ResponseMessage.class.getName(),message.getClass().getName());
		ResponseMessage rm = (ResponseMessage)message;
		assertEquals(0,rm.getHeader().size());
		assertEquals(MessageType.BAD_REQUEST,rm.getResponse());
		assertEquals(MessageType.READ,rm.getRequest());
		assertEquals("test",rm.getResource().getName());
		assertNull(rm.getData());
		assertNull(rm.getReason());
		assertNull(rm.getAffectedResources());
	}
	
}
