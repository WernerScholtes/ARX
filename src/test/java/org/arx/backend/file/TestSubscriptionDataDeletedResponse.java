package org.arx.backend.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.arx.Credentials;
import org.arx.MessageType;
import org.arx.Reason;
import org.arx.Resource;
import org.arx.util.ResponseMessage;
import org.arx.util.SimpleResource;
import org.arx.util.StringCredentials;
import org.arx.util.Utils;
import org.junit.Test;

public class TestSubscriptionDataDeletedResponse {
	@Test
	public void testDeleted() throws InterruptedException {
		Credentials credentials = new StringCredentials("# crud");
		Utils.QueingObserver observer = new Utils.QueingObserver();
		Resource resource = new SimpleResource("test");
		SubscriptionObserver subscription = new SubscriptionObserver(credentials, observer, resource, false);
		SubscriptionDataDeletedResponse response = new SubscriptionDataDeletedResponse(subscription,resource);
		response.run();
		ResponseMessage message = observer.take();
		assertEquals(MessageType.DATA,message.getResponse());
		assertEquals(MessageType.SUBSCRIBE,message.getRequest());
		assertEquals("test",message.getResource().getName());
		assertEquals(Reason.DELETED,message.getReason());
		assertNull(message.getData());
		assertEquals("test",message.getAffectedResource().getName());
	}
}
