package org.arx.backend.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.arx.Credentials;
import org.arx.MessageType;
import org.arx.Reason;
import org.arx.Resource;
import org.arx.util.Configuration;
import org.arx.util.ResponseMessage;
import org.arx.util.SimpleResource;
import org.arx.util.StringCredentials;
import org.arx.util.Utils;
import org.junit.Before;
import org.junit.Test;

public class TestReadRequest {
	@Before
	public void init() {
		Configuration.createInstance(Utils.HOME);
	}

	@Test
	public void testSuccess() throws InterruptedException, IOException {
		try {
			// Make sure the resource test does exist
			Utils.write("htdocs/test", "DATA".getBytes());
			Credentials credentials = new StringCredentials("# crud");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Resource resource = new SimpleResource("test");
			ReadRequest request = new ReadRequest(credentials, resource, observer);
			request.run();
			ResponseMessage message = observer.take();
			assertEquals(MessageType.DATA, message.getResponse());
			assertEquals(MessageType.READ, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertEquals(Reason.INITIAL,message.getReason());
			assertEquals("DATA",new String(message.getData().getContent()));
			assertEquals("test", message.getAffectedResource().getName());
			message = observer.take();
			assertEquals(MessageType.SUCCESS, message.getResponse());
			assertEquals(MessageType.READ, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResources());
		} finally {
			Utils.deleteIfExists("htdocs/test");
		}
	}

	@Test
	public void testForbidden() throws InterruptedException, IOException {
		try {
			// Make sure the resource test does exist
			Utils.write("htdocs/test", "DATA".getBytes());
			Credentials credentials = new StringCredentials("# cud");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Resource resource = new SimpleResource("test");
			ReadRequest request = new ReadRequest(credentials, resource, observer);
			request.run();
			ResponseMessage message = observer.take();
			assertEquals(MessageType.FORBIDDEN, message.getResponse());
			assertEquals(MessageType.READ, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResources());
		} finally {
			Utils.deleteIfExists("htdocs/test");
		}
	}

	@Test
	public void testNotFound() throws InterruptedException, IOException {
		try {
			// Make sure the resource test does not exist
			Utils.deleteIfExists("htdocs/test");
			Credentials credentials = new StringCredentials("# crud");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Resource resource = new SimpleResource("test");
			ReadRequest request = new ReadRequest(credentials, resource, observer);
			request.run();
			ResponseMessage message = observer.take();
			assertEquals(MessageType.NOT_FOUND, message.getResponse());
			assertEquals(MessageType.READ, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResources());
		} finally {
			Utils.deleteIfExists("htdocs/test");
		}
	}

	@Test
	public void testMultipleRead() throws InterruptedException, IOException {
		try {
			// Make sure some resources do exist
			Utils.write("htdocs/a/b/test", "DATA".getBytes());
			Utils.write("htdocs/a/c/test", "DUTU".getBytes());
			Credentials credentials = new StringCredentials("# crud");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Resource resource = new SimpleResource("a/+/test");
			ReadRequest request = new ReadRequest(credentials, resource, observer);
			request.run();
			ResponseMessage message = observer.take();
			assertEquals(MessageType.DATA, message.getResponse());
			assertEquals(MessageType.READ, message.getRequest());
			assertEquals("a/+/test", message.getResource().getName());
			assertEquals(Reason.INITIAL,message.getReason());
			assertEquals("DATA",new String(message.getData().getContent()));
			assertEquals("a/b/test", message.getAffectedResource().getName());
			message = observer.take();
			assertEquals(MessageType.DATA, message.getResponse());
			assertEquals(MessageType.READ, message.getRequest());
			assertEquals("a/+/test", message.getResource().getName());
			assertEquals(Reason.INITIAL,message.getReason());
			assertEquals("DUTU",new String(message.getData().getContent()));
			assertEquals("a/c/test", message.getAffectedResource().getName());
			message = observer.take();
			assertEquals(MessageType.SUCCESS, message.getResponse());
			assertEquals(MessageType.READ, message.getRequest());
			assertEquals("a/+/test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResources());
		} finally {
			Utils.cleanup("htdocs");
		}
	}

}
