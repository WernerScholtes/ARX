package org.arx.backend.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.arx.Credentials;
import org.arx.Data;
import org.arx.MessageType;
import org.arx.Reason;
import org.arx.Resource;
import org.arx.util.ByteArrayData;
import org.arx.util.Configuration;
import org.arx.util.ResponseMessage;
import org.arx.util.SimpleResource;
import org.arx.util.StringCredentials;
import org.arx.util.Utils;
import org.junit.Before;
import org.junit.Test;

public class TestFileSystemBackend {
	@Before
	public void init() {
		Configuration.createInstance(Utils.HOME);
	}
	
	@Test
	public void testPing() throws IOException, InterruptedException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			FileSystemBackend backend = new FileSystemBackend(executor);
			Credentials credentials = new StringCredentials("# crud");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			backend.ping(credentials, observer);
			ResponseMessage message = observer.take();
			assertEquals(MessageType.SUCCESS, message.getResponse());
			assertEquals(MessageType.PING, message.getRequest());
			assertNull(message.getResource());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResources());
		} finally {
			executor.shutdown();
			Utils.cleanup("htdocs");
		}
		
	}

	@Test
	public void testCreate() throws IOException, InterruptedException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			// Make sure the resource test does not exist
			Utils.deleteIfExists("htdocs/test");
			FileSystemBackend backend = new FileSystemBackend(executor);
			Credentials credentials = new StringCredentials("# crud");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Data data = new ByteArrayData(null, "DATA".getBytes());
			Resource resource = new SimpleResource("test");
			backend.create(credentials, resource, data, observer);
			ResponseMessage message = observer.take();
			assertEquals(MessageType.SUCCESS, message.getResponse());
			assertEquals(MessageType.CREATE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertEquals("test", message.getAffectedResource().getName());
			assertEquals("DATA", Utils.readString("htdocs/test"));
			// Try again, expect ALREADY_EXISTS
			backend.create(credentials, resource, data, observer);
			message = observer.take();
			assertEquals(MessageType.ALREADY_EXISTS, message.getResponse());
			assertEquals(MessageType.CREATE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
			// Try with resource pattern, expect BAD_REQUEST
			Resource resource2 = new SimpleResource("+/test");
			backend.create(credentials, resource2, data, observer);
			message = observer.take();
			assertEquals(MessageType.BAD_REQUEST, message.getResponse());
			assertEquals(MessageType.CREATE, message.getRequest());
			assertEquals("+/test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
			// Try without access rights, expect FORBIDDEM
			credentials = new StringCredentials("# r");
			backend.create(credentials, resource, data, observer);
			message = observer.take();
			assertEquals(MessageType.FORBIDDEN, message.getResponse());
			assertEquals(MessageType.CREATE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
		} finally {
			executor.shutdown();
			Utils.cleanup("htdocs");
		}
	}

	@Test
	public void testUpdate() throws IOException, InterruptedException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			// Make sure the resource test does exist
			Utils.write("htdocs/test", "DATA".getBytes());
			FileSystemBackend backend = new FileSystemBackend(executor);
			Credentials credentials = new StringCredentials("# crud");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Data data = new ByteArrayData(null, "DUTU".getBytes());
			Resource resource = new SimpleResource("test");
			backend.update(credentials, resource, data, observer);
			ResponseMessage message = observer.take();
			assertEquals(MessageType.SUCCESS, message.getResponse());
			assertEquals(MessageType.UPDATE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertEquals("test", message.getAffectedResource().getName());
			assertEquals("DUTU", Utils.readString("htdocs/test"));
			// Make sure the resource test does not exist, expect NOT_FOUND
			Utils.deleteIfExists("htdocs/test");
			backend.update(credentials, resource, data, observer);
			message = observer.take();
			assertEquals(MessageType.NOT_FOUND, message.getResponse());
			assertEquals(MessageType.UPDATE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
			// Try without access rights, expect FORBIDDEM
			credentials = new StringCredentials("# r");
			backend.update(credentials, resource, data, observer);
			message = observer.take();
			assertEquals(MessageType.FORBIDDEN, message.getResponse());
			assertEquals(MessageType.UPDATE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
		} finally {
			executor.shutdown();
			Utils.cleanup("htdocs");
		}
	}

	@Test
	public void testSave() throws IOException, InterruptedException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			// Make sure the resource test does not exist
			Utils.deleteIfExists("htdocs/test");
			FileSystemBackend backend = new FileSystemBackend(executor);
			Credentials credentials = new StringCredentials("# crud");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Data data = new ByteArrayData(null, "DATA".getBytes());
			Resource resource = new SimpleResource("test");
			backend.save(credentials, resource, data, observer);
			ResponseMessage message = observer.take();
			assertEquals(MessageType.SUCCESS, message.getResponse());
			assertEquals(MessageType.SAVE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertEquals("test", message.getAffectedResource().getName());
			assertEquals("DATA", Utils.readString("htdocs/test"));
			// Try again, expect SUCCESS
			data = new ByteArrayData(null, "DUTU".getBytes());
			backend.save(credentials, resource, data, observer);
			message = observer.take();
			assertEquals(MessageType.SUCCESS, message.getResponse());
			assertEquals(MessageType.SAVE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertEquals("test", message.getAffectedResource().getName());
			assertEquals("DUTU", Utils.readString("htdocs/test"));
			// Try without access rights, expect FORBIDDEM
			credentials = new StringCredentials("# r");
			backend.save(credentials, resource, data, observer);
			message = observer.take();
			assertEquals(MessageType.FORBIDDEN, message.getResponse());
			assertEquals(MessageType.SAVE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
		} finally {
			executor.shutdown();
			Utils.cleanup("htdocs");
		}
	}

	@Test
	public void testMultipleSave() throws IOException, InterruptedException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			// Make sure some resources do exist
			Utils.write("htdocs/a/b/c/test", "DATA".getBytes());
			Utils.write("htdocs/a/x/test", "DATA".getBytes());
			FileSystemBackend backend = new FileSystemBackend(executor);
			Credentials credentials = new StringCredentials("# crud");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Data data = new ByteArrayData(null, "DUTU".getBytes());
			Resource resource = new SimpleResource("a/+/c/test");
			backend.save(credentials, resource, data, observer);
			ResponseMessage message = observer.take();
			assertEquals(MessageType.SUCCESS, message.getResponse());
			assertEquals(MessageType.SAVE, message.getRequest());
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
			executor.shutdown();
			Utils.cleanup("htdocs");
		}
	}
	
	@Test
	public void testDelete() throws IOException, InterruptedException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			// Make sure the resource test does exist
			Utils.write("htdocs/test", "DATA".getBytes());
			FileSystemBackend backend = new FileSystemBackend(executor);
			Credentials credentials = new StringCredentials("# crud");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Resource resource = new SimpleResource("test");
			backend.delete(credentials, resource, observer);
			ResponseMessage message = observer.take();
			assertEquals(MessageType.SUCCESS, message.getResponse());
			assertEquals(MessageType.DELETE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertEquals("test", message.getAffectedResource().getName());
			assertEquals(false, Utils.fileExists("htdocs/test"));
			// Make sure the resource test does not exist, expect NOT_FOUND
			Utils.deleteIfExists("htdocs/test");
			backend.delete(credentials, resource, observer);
			message = observer.take();
			assertEquals(MessageType.NOT_FOUND, message.getResponse());
			assertEquals(MessageType.DELETE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
			// Try without access rights, expect FORBIDDEM
			credentials = new StringCredentials("# r");
			backend.delete(credentials, resource, observer);
			message = observer.take();
			assertEquals(MessageType.FORBIDDEN, message.getResponse());
			assertEquals(MessageType.DELETE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
		} finally {
			executor.shutdown();
			Utils.cleanup("htdocs");
		}
	}
	
	@Test
	public void testRead() throws IOException, InterruptedException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			// Make sure the resource test does exist
			Utils.write("htdocs/test", "DATA".getBytes());
			FileSystemBackend backend = new FileSystemBackend(executor);
			Credentials credentials = new StringCredentials("# crud");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Resource resource = new SimpleResource("test");
			backend.read(credentials, resource, observer);
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
			// Make sure the resource test does not exist, expect NOT_FOUND
			Utils.deleteIfExists("htdocs/test");
			backend.read(credentials, resource, observer);
			message = observer.take();
			assertEquals(MessageType.NOT_FOUND, message.getResponse());
			assertEquals(MessageType.READ, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
			// Try without access rights, expect FORBIDDEM
			credentials = new StringCredentials("# -");
			backend.read(credentials, resource, observer);
			message = observer.take();
			assertEquals(MessageType.FORBIDDEN, message.getResponse());
			assertEquals(MessageType.READ, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
		} finally {
			executor.shutdown();
			Utils.cleanup("htdocs");
		}
	}
	
	@Test
	public void testSubscriptions() throws IOException, InterruptedException {
		ExecutorService executor = Executors.newCachedThreadPool();
		try {
			// Make sure the resource test does exist
			Utils.write("htdocs/test", "DATA".getBytes());
			FileSystemBackend backend = new FileSystemBackend(executor);
			executor.execute(backend);
			Credentials credentials = new StringCredentials("# crud");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			backend.subscribe(credentials, new SimpleResource("tast"), observer);
			Resource resource = new SimpleResource("test");
			backend.subscribeStatus(credentials, resource, observer);
			ResponseMessage message = observer.take();
			assertEquals(MessageType.DATA, message.getResponse());
			assertEquals(MessageType.SUBSCRIBE_STATUS, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertEquals(Reason.INITIAL,message.getReason());
			assertEquals("DATA",new String(message.getData().getContent()));
			assertEquals("test", message.getAffectedResource().getName());
			// Change resource
			Utils.write("htdocs/test", "DUTU".getBytes());
			message = observer.take();
			assertEquals(MessageType.DATA, message.getResponse());
			assertEquals(MessageType.SUBSCRIBE_STATUS, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertEquals(Reason.UPDATED,message.getReason());
			assertEquals("DUTU",new String(message.getData().getContent()));
			assertEquals("test", message.getAffectedResource().getName());
			// Remove double MODIFY_ENTRY 
			message = observer.poll(1,TimeUnit.SECONDS);
			// Make sure the resource test does not exist
			Utils.deleteIfExists("htdocs/test");
			message = observer.take();
			assertEquals(MessageType.DATA, message.getResponse());
			assertEquals(MessageType.SUBSCRIBE_STATUS, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertEquals(Reason.DELETED,message.getReason());
			assertNull(message.getData());
			assertEquals("test", message.getAffectedResource().getName());
			// Unsubscribe
			backend.unsubscribe(credentials, resource, observer);
			message = observer.take();
			assertEquals(MessageType.SUCCESS, message.getResponse());
			assertEquals(MessageType.UNSUBSCRIBE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResources());
			message = observer.take();
			assertEquals(MessageType.SUCCESS, message.getResponse());
			assertEquals(MessageType.SUBSCRIBE_STATUS, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResources());
			// Unsubscribe all
			backend.unsubscribeAll(credentials, observer);
			message = observer.take();
			assertEquals(MessageType.SUCCESS, message.getResponse());
			assertEquals(MessageType.UNSUBSCRIBE_ALL, message.getRequest());
			assertNull(message.getResource());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResources());
			message = observer.take();
			assertEquals(MessageType.SUCCESS, message.getResponse());
			assertEquals(MessageType.SUBSCRIBE, message.getRequest());
			assertEquals("tast", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResources());
		} finally {
			executor.shutdown();
			Utils.cleanup("htdocs");
		}
	}
}
