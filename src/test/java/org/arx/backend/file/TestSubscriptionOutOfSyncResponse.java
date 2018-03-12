package org.arx.backend.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.arx.Credentials;
import org.arx.MessageType;
import org.arx.Resource;
import org.arx.util.ResponseMessage;
import org.arx.util.SimpleResource;
import org.arx.util.StringCredentials;
import org.arx.util.Utils;
import org.junit.Test;

public class TestSubscriptionOutOfSyncResponse {
	@Test
	public void testSubscriptionOutOfSyncResponse() throws InterruptedException {
		Credentials credentials = new StringCredentials("# crud");
		Utils.QueingObserver observer = new Utils.QueingObserver();
		Resource resource = new SimpleResource("test");
		SubscriptionObserver subscription = new SubscriptionObserver(credentials, observer, resource, false);
		Subscriptions subscriptions = new Subscriptions();
		subscriptions.subscribe(subscription);
		SubscriptionOutOfSyncResponse response = new SubscriptionOutOfSyncResponse(subscriptions, subscription);
		response.run();
		ResponseMessage message = observer.take();
		assertEquals(MessageType.OUT_OF_SYNC, message.getResponse());
		assertEquals(MessageType.SUBSCRIBE, message.getRequest());
		assertEquals("test",message.getResource().getName());
		assertNull(message.getReason());
		assertNull(message.getData());
		assertNull(message.getAffectedResources());
		
	}
}
