package org.arx.backend.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.arx.Credentials;
import org.arx.MessageType;
import org.arx.util.ResponseMessage;
import org.arx.util.SimpleResource;
import org.arx.util.StringCredentials;
import org.arx.util.Utils;
import org.junit.Test;

public class TestUnsubscribeRequest {
	@Test
	public void testUnsubscribe() throws InterruptedException {
		String[] res = {"a/b/c", "+/b/c", "+/+/c"};
		Credentials credentials = new StringCredentials("# crud");
		Utils.QueingObserver observer = new Utils.QueingObserver();
		Subscriptions subs = new Subscriptions();
		for ( int i = 0; i < res.length; ++i) {
			subs.subscribe(new SubscriptionObserver(credentials, observer, new SimpleResource(res[i]), false));
		}
		UnsubscribeRequest request = new UnsubscribeRequest(new SimpleResource("+/b/c"), observer, subs);
		request.run();
		ResponseMessage message = observer.take();
		assertEquals(MessageType.SUCCESS,message.getResponse());
		assertEquals(MessageType.UNSUBSCRIBE,message.getRequest());
		assertEquals("+/b/c",message.getResource().getName());
		assertNull(message.getReason());
		assertNull(message.getData());
		assertNull(message.getAffectedResource());
		message = observer.take();
		assertEquals(MessageType.SUCCESS,message.getResponse());
		assertEquals(MessageType.SUBSCRIBE,message.getRequest());
		assertEquals("+/b/c",message.getResource().getName());
		assertNull(message.getReason());
		assertNull(message.getData());
		assertNull(message.getAffectedResource());
	}
}
