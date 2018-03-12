package org.arx.backend.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.arx.Credentials;
import org.arx.MessageType;
import org.arx.Resource;
import org.arx.util.Configuration;
import org.arx.util.ResponseMessage;
import org.arx.util.SimpleResource;
import org.arx.util.StringCredentials;
import org.arx.util.Utils;
import org.junit.Before;
import org.junit.Test;

public class TestDeleteRequest {
	@Before
	public void init() {
		Configuration.createInstance(Utils.HOME);
	}

	@Test
	public void testDeleteSingleResource() throws InterruptedException, IOException {
		try {
			// Make sure the resource test does exist
			Utils.write("htdocs/test", "DATA".getBytes());
			Credentials credentials = new StringCredentials("# crud");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Resource resource = new SimpleResource("test");
			DeleteRequest request = new DeleteRequest(credentials, resource, observer);
			request.run();
			ResponseMessage message = observer.take();
			assertEquals(MessageType.SUCCESS, message.getResponse());
			assertEquals(MessageType.DELETE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertEquals("test", message.getAffectedResource().getName());
			assertEquals(false, Utils.fileExists("htdocs/test"));
		} finally {
			Utils.deleteIfExists("htdocs/test");
		}
	}

	@Test
	public void testDeleteMultipleResources() throws InterruptedException, IOException {
		try {
			// Make sure the resources do exist
			Utils.write("htdocs/test1", "DATA".getBytes());
			Utils.write("htdocs/test2", "DATA".getBytes());
			Credentials credentials = new StringCredentials("# crud");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Resource resource = new SimpleResource("#");
			DeleteRequest request = new DeleteRequest(credentials, resource, observer);
			request.run();
			ResponseMessage message = observer.take();
			assertEquals(MessageType.SUCCESS, message.getResponse());
			assertEquals(MessageType.DELETE, message.getRequest());
			assertEquals("#", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			Set<String> resources = new TreeSet<String>();
			for (Resource res : message.getAffectedResources()) {
				resources.add(res.getName());
			}
			assertEquals(2, resources.size());
			assertEquals(true, resources.contains("test1"));
			assertEquals(true, resources.contains("test2"));
			assertEquals(false, Utils.fileExists("htdocs/test1"));
			assertEquals(false, Utils.fileExists("htdocs/test2"));
		} finally {
			Utils.deleteIfExists("htdocs/test1");
			Utils.deleteIfExists("htdocs/test2");
		}
	}

	@Test
	public void testForbidden() throws InterruptedException, IOException {
		try {
			// Make sure the resource test does exist
			Utils.write("htdocs/test", "DATA".getBytes());
			Credentials credentials = new StringCredentials("# r");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Resource resource = new SimpleResource("test");
			DeleteRequest request = new DeleteRequest(credentials, resource, observer);
			request.run();
			ResponseMessage message = observer.take();
			assertEquals(MessageType.FORBIDDEN, message.getResponse());
			assertEquals(MessageType.DELETE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
			assertEquals(true, Utils.fileExists("htdocs/test"));
		} finally {
			Utils.deleteIfExists("htdocs/test");
		}
	}

	@Test
	public void testNotFound() throws InterruptedException, IOException {
		// Make sure the resource test does not exist
		Utils.deleteIfExists("htdocs/test");
		Credentials credentials = new StringCredentials("# crud");
		Utils.QueingObserver observer = new Utils.QueingObserver();
		Resource resource = new SimpleResource("test");
		DeleteRequest request = new DeleteRequest(credentials, resource, observer);
		request.run();
		ResponseMessage message = observer.take();
		assertEquals(MessageType.NOT_FOUND, message.getResponse());
		assertEquals(MessageType.DELETE, message.getRequest());
		assertEquals("test", message.getResource().getName());
		assertNull(message.getReason());
		assertNull(message.getData());
		assertNull(message.getAffectedResource());
	}
}
