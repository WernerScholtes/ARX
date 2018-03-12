package org.arx.backend.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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

public class TestUpdateRequest {
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
			Data data = new ByteArrayData(null, "DUTU".getBytes());
			Resource resource = new SimpleResource("test");
			UpdateRequest request = new UpdateRequest(credentials, resource, data, observer);
			request.run();
			ResponseMessage message = observer.take();
			assertEquals(MessageType.SUCCESS, message.getResponse());
			assertEquals(MessageType.UPDATE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertEquals("test", message.getAffectedResource().getName());
			assertEquals("DUTU", Utils.readString("htdocs/test"));
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
			Data data = new ByteArrayData(null, "DATA".getBytes());
			Resource resource = new SimpleResource("test");
			UpdateRequest request = new UpdateRequest(credentials, resource, data, observer);
			request.run();
			ResponseMessage message = observer.take();
			assertEquals(MessageType.NOT_FOUND, message.getResponse());
			assertEquals(MessageType.UPDATE, message.getRequest());
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
		// Make sure the resource test does not exist
		Utils.deleteIfExists("htdocs/test");
		Credentials credentials = new StringCredentials("# crd");
		Utils.QueingObserver observer = new Utils.QueingObserver();
		Data data = new ByteArrayData(null, "DATA".getBytes());
		Resource resource = new SimpleResource("test");
		UpdateRequest request = new UpdateRequest(credentials, resource, data, observer);
		request.run();
		ResponseMessage message = observer.take();
		assertEquals(MessageType.FORBIDDEN, message.getResponse());
		assertEquals(MessageType.UPDATE, message.getRequest());
		assertEquals("test", message.getResource().getName());
		assertNull(message.getReason());
		assertNull(message.getData());
		assertNull(message.getAffectedResource());
		assertEquals(false, Utils.fileExists("htdocs/test"));
	}

	@Test
	public void testMultipleUpdate() throws InterruptedException, IOException {
		try {
			// Make sure some resources do exist
			Utils.write("htdocs/a/b/c/test", "DATA".getBytes());
			Utils.write("htdocs/a/x/c/test", "DATA".getBytes());
			Utils.write("htdocs/a/x/test", "DATA".getBytes());
			Credentials credentials = new StringCredentials("# crud");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Data data = new ByteArrayData(null, "DUTU".getBytes());
			Resource resource = new SimpleResource("a/+/c/test");
			UpdateRequest request = new UpdateRequest(credentials, resource, data, observer);
			request.run();
			ResponseMessage message = observer.take();
			assertEquals(MessageType.SUCCESS, message.getResponse());
			assertEquals(MessageType.UPDATE, message.getRequest());
			assertEquals("a/+/c/test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			Set<String> affected = new HashSet<String>();
			for ( Resource res : message.getAffectedResources() ) {
				affected.add(res.getName());
			}
			assertEquals(2,affected.size());
			assertEquals(true,affected.contains("a/b/c/test"));
			assertEquals(true,affected.contains("a/x/c/test"));
			assertEquals("DUTU", Utils.readString("htdocs/a/b/c/test"));
			assertEquals("DUTU", Utils.readString("htdocs/a/x/c/test"));
			assertEquals("DATA", Utils.readString("htdocs/a/x/test"));
		} finally {
			Utils.cleanup("htdocs");
		}
	}


}
