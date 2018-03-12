package org.arx.backend.file;

import static org.junit.Assert.*;

import java.io.IOException;

import org.arx.Credentials;
import org.arx.Data;
import org.arx.MessageType;
import org.arx.Resource;
import org.arx.util.ByteArrayData;
import org.arx.util.Configuration;
import org.arx.util.ResponseMessage;
import org.arx.util.SimpleResource;
import org.arx.util.StringCredentials;
import org.arx.util.Utils;
import org.junit.Before;
import org.junit.Test;

public class TestCreateRequest {
	@Before
	public void init() {
		Configuration.createInstance(Utils.HOME);
	}

	@Test
	public void testSuccess() throws InterruptedException, IOException {
		try {
			// Make sure the resource test does not exist
			Utils.deleteIfExists("htdocs/test");
			Credentials credentials = new StringCredentials("# crud");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Data data = new ByteArrayData(null, "DATA".getBytes());
			Resource resource = new SimpleResource("test");
			CreateRequest request = new CreateRequest(credentials, resource, data, observer);
			request.run();
			ResponseMessage message = observer.take();
			assertEquals(MessageType.SUCCESS, message.getResponse());
			assertEquals(MessageType.CREATE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertEquals("test", message.getAffectedResource().getName());
			assertEquals("DATA", Utils.readString("htdocs/test"));
		} finally {
			Utils.deleteIfExists("htdocs/test");
		}
	}

	@Test
	public void testForbidden() throws InterruptedException, IOException {
		// Make sure the resource test does not exist
		Utils.deleteIfExists("htdocs/test");
		Credentials credentials = new StringCredentials("# r");
		Utils.QueingObserver observer = new Utils.QueingObserver();
		Data data = new ByteArrayData(null, "DATA".getBytes());
		Resource resource = new SimpleResource("test");
		CreateRequest request = new CreateRequest(credentials, resource, data, observer);
		request.run();
		ResponseMessage message = observer.take();
		assertEquals(MessageType.FORBIDDEN, message.getResponse());
		assertEquals(MessageType.CREATE, message.getRequest());
		assertEquals("test", message.getResource().getName());
		assertNull(message.getReason());
		assertNull(message.getData());
		assertNull(message.getAffectedResource());
		assertEquals(false, Utils.fileExists("htdocs/test"));
	}

	@Test
	public void testAlreadyExists() throws InterruptedException, IOException {
		try {
			// Make sure the resource test does exist
			Utils.write("htdocs/test", "DATA".getBytes());
			Credentials credentials = new StringCredentials("# crud");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Data data = new ByteArrayData(null, "DATA".getBytes());
			Resource resource = new SimpleResource("test");
			CreateRequest request = new CreateRequest(credentials, resource, data, observer);
			request.run();
			ResponseMessage message = observer.take();
			assertEquals(MessageType.ALREADY_EXISTS, message.getResponse());
			assertEquals(MessageType.CREATE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
			Utils.deleteIfExists("htdocs/test");
		} finally {
			Utils.deleteIfExists("htdocs/test");
		}
	}

	@Test
	public void testBadRequest() throws InterruptedException, IOException {
		Credentials credentials = new StringCredentials("# crud");
		Utils.QueingObserver observer = new Utils.QueingObserver();
		Data data = new ByteArrayData(null, "DATA".getBytes());
		Resource resource = new SimpleResource("test/+");
		CreateRequest request = new CreateRequest(credentials, resource, data, observer);
		request.run();
		ResponseMessage message = observer.take();
		assertEquals(MessageType.BAD_REQUEST, message.getResponse());
		assertEquals(MessageType.CREATE, message.getRequest());
		assertEquals("test/+", message.getResource().getName());
		assertNull(message.getReason());
		assertNull(message.getData());
		assertNull(message.getAffectedResource());
	}
}
