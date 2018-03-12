package org.arx.backend.file;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.arx.Credentials;
import org.arx.Data;
import org.arx.MessageType;
import org.arx.Reason;
import org.arx.Resource;
import org.arx.util.ByteArrayData;
import org.arx.util.ResponseMessage;
import org.arx.util.SimpleResource;
import org.arx.util.StringCredentials;
import org.arx.util.Utils;
import org.junit.Test;

public class TestSubscriptionObserver {
	@Test
	public void testGetters() {
		Credentials credentials = new StringCredentials("# crud");
		Utils.QueingObserver observer = new Utils.QueingObserver();
		Resource resource = new SimpleResource("test");
		SubscriptionObserver subscription = new SubscriptionObserver(credentials,observer,resource,false);
		assertEquals(observer,subscription.getObserver());
		assertEquals(MessageType.SUBSCRIBE,subscription.getRequest());
	}

	@Test
	public void testOnData() throws IOException, InterruptedException {
		Credentials credentials = new StringCredentials("# crud");
		Utils.QueingObserver observer = new Utils.QueingObserver();
		Resource resource = new SimpleResource("test");
		SubscriptionObserver subscription = new SubscriptionObserver(credentials,observer,resource,false);
		Data data = new ByteArrayData(null, "DATA".getBytes());
		subscription.onData(MessageType.SUBSCRIBE, resource, Reason.CREATED, resource, data);
		ResponseMessage message = observer.take();
		assertEquals(MessageType.DATA,message.getResponse());
		assertEquals(MessageType.SUBSCRIBE,message.getRequest());
		assertEquals("test",message.getResource().getName());
		assertEquals(Reason.CREATED,message.getReason());
		assertEquals("DATA",new String(message.getData().getContent()));
		assertEquals("test",message.getAffectedResource().getName());
	}

	@Test
	public void testOnSuccess() throws IOException, InterruptedException {
		Credentials credentials = new StringCredentials("# crud");
		Utils.QueingObserver observer = new Utils.QueingObserver();
		Resource resource = new SimpleResource("test");
		SubscriptionObserver subscription = new SubscriptionObserver(credentials,observer,resource,false);
		subscription.onSuccess(MessageType.SUBSCRIBE, resource);
		ResponseMessage message = observer.take();
		assertEquals(MessageType.SUCCESS,message.getResponse());
		assertEquals(MessageType.SUBSCRIBE,message.getRequest());
		assertEquals("test",message.getResource().getName());
		assertNull(message.getData());
		assertNull(message.getAffectedResources());
	}

	@Test
	public void testOnError() throws IOException, InterruptedException {
		Credentials credentials = new StringCredentials("# crud");
		Utils.QueingObserver observer = new Utils.QueingObserver();
		Resource resource = new SimpleResource("test");
		SubscriptionObserver subscription = new SubscriptionObserver(credentials,observer,resource,false);
		subscription.onError(MessageType.SUBSCRIBE, resource, MessageType.FORBIDDEN);
		ResponseMessage message = observer.take();
		assertEquals(MessageType.FORBIDDEN,message.getResponse());
		assertEquals(MessageType.SUBSCRIBE,message.getRequest());
		assertEquals("test",message.getResource().getName());
		assertNull(message.getData());
		assertNull(message.getAffectedResources());
	}

	@Test
	public void testQueuing() throws IOException, InterruptedException {
		Credentials credentials = new StringCredentials("# crud");
		Utils.QueingObserver observer = new Utils.QueingObserver();
		Resource resource = new SimpleResource("test");
		SubscriptionObserver subscription = new SubscriptionObserver(credentials,observer,resource,true);
		Data data = new ByteArrayData(null, "DATA".getBytes());
		subscription.startBuffering();
		subscription.onData(MessageType.SUBSCRIBE_STATUS, resource, Reason.INITIAL, resource, data);
		ResponseMessage message = observer.take();
		assertEquals(MessageType.DATA,message.getResponse());
		assertEquals(MessageType.SUBSCRIBE_STATUS,message.getRequest());
		assertEquals("test",message.getResource().getName());
		assertEquals(Reason.INITIAL,message.getReason());
		assertEquals("DATA",new String(message.getData().getContent()));
		assertEquals("test",message.getAffectedResource().getName());
		subscription.onData(MessageType.SUBSCRIBE_STATUS, resource, Reason.UPDATED, resource, data);
		message = observer.poll(100, TimeUnit.MILLISECONDS);
		assertNull(message);
		subscription.stopBuffering();
		message = observer.take();
		assertEquals(MessageType.DATA,message.getResponse());
		assertEquals(MessageType.SUBSCRIBE_STATUS,message.getRequest());
		assertEquals("test",message.getResource().getName());
		assertEquals(Reason.UPDATED,message.getReason());
		assertEquals("DATA",new String(message.getData().getContent()));
		assertEquals("test",message.getAffectedResource().getName());
	}

	@Test
	public void testQueuingError() throws IOException, InterruptedException {
		Credentials credentials = new StringCredentials("# crud");
		Utils.QueingObserver observer = new Utils.QueingObserver();
		Resource resource = new SimpleResource("test");
		SubscriptionObserver subscription = new SubscriptionObserver(credentials,observer,resource,true);
		Data data = new ByteArrayData(null, "DATA".getBytes());
		subscription.startBuffering();
		subscription.onData(MessageType.SUBSCRIBE_STATUS, resource, Reason.INITIAL, resource, data);
		ResponseMessage message = observer.take();
		assertEquals(MessageType.DATA,message.getResponse());
		assertEquals(MessageType.SUBSCRIBE_STATUS,message.getRequest());
		assertEquals("test",message.getResource().getName());
		assertEquals(Reason.INITIAL,message.getReason());
		assertEquals("DATA",new String(message.getData().getContent()));
		assertEquals("test",message.getAffectedResource().getName());
		subscription.onData(MessageType.SUBSCRIBE_STATUS, resource, Reason.UPDATED, resource, data);
		message = observer.poll(100, TimeUnit.MILLISECONDS);
		assertNull(message);
		// Implicit stopBuffering() call
		subscription.onError(MessageType.SUBSCRIBE_STATUS, resource, MessageType.OUT_OF_SYNC);
		message = observer.take();
		assertEquals(MessageType.DATA,message.getResponse());
		assertEquals(MessageType.SUBSCRIBE_STATUS,message.getRequest());
		assertEquals("test",message.getResource().getName());
		assertEquals(Reason.UPDATED,message.getReason());
		assertEquals("DATA",new String(message.getData().getContent()));
		assertEquals("test",message.getAffectedResource().getName());
		message = observer.take();
		assertEquals(MessageType.OUT_OF_SYNC,message.getResponse());
		assertEquals(MessageType.SUBSCRIBE_STATUS,message.getRequest());
		assertEquals("test",message.getResource().getName());
		assertNull(message.getReason());
		assertNull(message.getData());
		assertNull(message.getAffectedResources());
		// No further message
		message = observer.poll(100, TimeUnit.MILLISECONDS);
		assertNull(message);
	}

}
