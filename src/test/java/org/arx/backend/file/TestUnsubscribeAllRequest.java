package org.arx.backend.file;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.arx.Credentials;
import org.arx.MessageType;
import org.arx.util.ResponseMessage;
import org.arx.util.SimpleResource;
import org.arx.util.StringCredentials;
import org.arx.util.Utils;
import org.junit.Test;

public class TestUnsubscribeAllRequest {
	@Test
	public void testUnsubscribeAll() throws InterruptedException {
		String[] res1 = {"a/b/c", "+/b/c", "+/+/c"};
		Credentials credentials = new StringCredentials("# crud");
		Utils.QueingObserver observer1 = new Utils.QueingObserver();
		SubscriptionObserver[] s1 = new SubscriptionObserver[res1.length];
		Subscriptions subs = new Subscriptions();
		for ( int i = 0; i < res1.length; ++i) {
			s1[i] = new SubscriptionObserver(credentials, observer1, new SimpleResource(res1[i]), false);
			subs.subscribe(s1[i]);
		}
		String[] res2 = {"a/b/+", "+/b/+", "+/+/#"};
		Utils.QueingObserver observer2 = new Utils.QueingObserver();
		SubscriptionObserver[] s2 = new SubscriptionObserver[res1.length];
		for ( int i = 0; i < res2.length; ++i) {
			s2[i] = new SubscriptionObserver(credentials, observer2, new SimpleResource(res2[i]), false);
			subs.subscribe(s2[i]);
		}
		UnsubscribeAllRequest request = new UnsubscribeAllRequest(observer1, subs);
		request.run();
		ResponseMessage message = observer1.take();
		assertEquals(MessageType.SUCCESS,message.getResponse());
		assertEquals(MessageType.UNSUBSCRIBE_ALL,message.getRequest());
		assertNull(message.getResource());
		assertNull(message.getReason());
		assertNull(message.getData());
		assertNull(message.getAffectedResource());
		message = observer1.take();
		assertEquals(MessageType.SUCCESS,message.getResponse());
		assertEquals(MessageType.SUBSCRIBE,message.getRequest());
		message = observer1.take();
		assertEquals(MessageType.SUCCESS,message.getResponse());
		assertEquals(MessageType.SUBSCRIBE,message.getRequest());
		message = observer1.take();
		assertEquals(MessageType.SUCCESS,message.getResponse());
		assertEquals(MessageType.SUBSCRIBE,message.getRequest());
		// No further messages from observer1
		message = observer1.poll(100, TimeUnit.MILLISECONDS);
		assertNull(message);
		// No message from observer2
		message = observer2.poll(100, TimeUnit.MILLISECONDS);
		assertNull(message);
	}
}
