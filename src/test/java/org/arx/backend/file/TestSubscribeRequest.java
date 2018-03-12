package org.arx.backend.file;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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

public class TestSubscribeRequest {
	@Before
	public void init() {
		Configuration.createInstance(Utils.HOME);
	}

	@Test
	public void testSuccess() throws IOException, InterruptedException {
		try {
			// Make sure the resource test does exist
			Utils.write("htdocs/test", "DATA".getBytes());
			Credentials credentials = new StringCredentials("# crud");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Resource resource = new SimpleResource("test");
			Subscriptions subscriptions = new Subscriptions();
			SubscribeRequest request = new SubscribeRequest(credentials, false, resource, observer, subscriptions);
			request.run();
			// Nothing happens
			ResponseMessage message = observer.poll(100, TimeUnit.MILLISECONDS);
			assertNull(message);
			assertEquals(1,subscriptions.match(resource).size());
			// Try again with initial read
			request = new SubscribeRequest(credentials, true, resource, observer, subscriptions);
			request.run();
			message = observer.take();
			assertEquals(MessageType.DATA, message.getResponse());
			assertEquals(MessageType.SUBSCRIBE_STATUS, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertEquals(Reason.INITIAL,message.getReason());
			assertEquals("DATA",new String(message.getData().getContent()));
			assertEquals("test", message.getAffectedResource().getName());
		} finally {
			Utils.cleanup("htdocs");
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
			Subscriptions subscriptions = new Subscriptions();
			SubscribeRequest request = new SubscribeRequest(credentials, false, resource, observer, subscriptions);
			request.run();
			ResponseMessage message = observer.take();
			assertEquals(MessageType.FORBIDDEN, message.getResponse());
			assertEquals(MessageType.SUBSCRIBE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResources());
		} finally {
			Utils.deleteIfExists("htdocs/test");
		}
	}

}
