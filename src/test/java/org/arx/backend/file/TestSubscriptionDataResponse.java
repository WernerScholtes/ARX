package org.arx.backend.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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

public class TestSubscriptionDataResponse {
	@Before
	public void init() {
		Configuration.createInstance(Utils.HOME);
	}

	@Test
	public void testData() throws InterruptedException, IOException {
		try {
			// Make sure the resource test does exist
			Utils.write("htdocs/test", "DATA".getBytes());
			Credentials credentials = new StringCredentials("# crud");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Resource resource = new SimpleResource("test");
			SubscriptionObserver subscription = new SubscriptionObserver(credentials, observer, resource, false);
			SubscriptionDataResponse response = new SubscriptionDataResponse(subscription, resource, Reason.INITIAL);
			response.run();
			ResponseMessage message = observer.take();
			assertEquals(MessageType.DATA, message.getResponse());
			assertEquals(MessageType.SUBSCRIBE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertEquals(Reason.INITIAL, message.getReason());
			assertEquals("DATA", new String(message.getData().getContent()));
			assertEquals("test", message.getAffectedResource().getName());
		} finally {
			Utils.deleteIfExists("htdocs/test");
		}
	}

	@Test
	public void testNoData() throws InterruptedException, IOException {
		try {
			// Make sure the resource test does not exist
			Utils.deleteIfExists("htdocs/test");
			Credentials credentials = new StringCredentials("# crud");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Resource resource = new SimpleResource("test");
			SubscriptionObserver subscription = new SubscriptionObserver(credentials, observer, resource, false);
			SubscriptionDataResponse response = new SubscriptionDataResponse(subscription, resource, Reason.INITIAL);
			response.run();
			ResponseMessage message = observer.poll(100,TimeUnit.MILLISECONDS);
			assertNull(message);
		} finally {
			Utils.deleteIfExists("htdocs/test");
		}
	}

}
