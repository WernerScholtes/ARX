package org.arx.protocol.tcp;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.arx.Credentials;
import org.arx.Data;
import org.arx.Endpoint;
import org.arx.MessageType;
import org.arx.Reason;
import org.arx.Resource;
import org.arx.backend.file.FileSystemBackend;
import org.arx.util.ByteArrayData;
import org.arx.util.Configuration;
import org.arx.util.ResponseMessage;
import org.arx.util.SimpleResource;
import org.arx.util.Utils;
import org.arx.util.jwt.JWTDecodeException;
import org.arx.util.jwt.JwtCredentials;
import org.arx.util.jwt.SignatureVerificationException;
import org.arx.util.jwt.TokenExpiredException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestTcpProtocol {
	private static ExecutorService executor;
	private static TcpClient client;
	@BeforeClass
	public static void startup() throws IOException, IllegalArgumentException, SignatureVerificationException, TokenExpiredException, JWTDecodeException, NoSuchAlgorithmException {
		// Remove default credentials
		Utils.copyFile("conf/arx2.conf", "conf/arx.conf");
		executor = Executors.newCachedThreadPool();
		Configuration.createInstance(Utils.HOME);
		Endpoint backend = new FileSystemBackend(executor);
		executor.execute(backend);
		TcpServer server = new TcpServer(executor,6789);
		server.setBackend(backend);
		executor.execute(server);
		client = new TcpClient("localhost",6789);
		executor.execute(client);
	}
	
	@AfterClass
	public static void shutdown() throws IOException {
		executor.shutdown();
		Utils.copyFile("conf/arx-ori.conf", "conf/arx.conf");			
	}
	
	@Test
	public void testPing() throws UnknownHostException, IOException, IllegalArgumentException, SignatureVerificationException, TokenExpiredException, JWTDecodeException, NoSuchAlgorithmException, InterruptedException {
		Utils.QueingObserver observer = new Utils.QueingObserver();
		Credentials credentials = new JwtCredentials("eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJyZ3MiOiIjIGNydWQifQ.slKq7ISDZiLrtWoubpTftmR_6JhyX3vGoIQfLl9TMTAPFG5NSK0jXY0oLAZbDIQBU3KACTxUb6612PJkvQtCm04OFd8i2jpTsIeMVTdvMMt0QqVKvrG_vVwL6ah3sq9vXIste_bXmTgCHfIUCKjW9sixsd7VXZymWpfEEFQw1Do");
		client.ping(credentials, observer);
		ResponseMessage message = observer.take();
		assertEquals(MessageType.SUCCESS,message.getResponse());
		assertEquals(MessageType.PING,message.getRequest());
		assertNull(message.getResource());
		assertNull(message.getReason());
		assertNull(message.getData());
		assertNull(message.getAffectedResources());
	}

	@Test
	public void testCreate() throws IOException, InterruptedException, IllegalArgumentException, SignatureVerificationException, TokenExpiredException, JWTDecodeException, NoSuchAlgorithmException {
		try {
			// Make sure the resource test does not exist
			Utils.deleteIfExists("htdocs/test");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Credentials credentials = new JwtCredentials("eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJyZ3MiOiIjIGNydWQifQ.slKq7ISDZiLrtWoubpTftmR_6JhyX3vGoIQfLl9TMTAPFG5NSK0jXY0oLAZbDIQBU3KACTxUb6612PJkvQtCm04OFd8i2jpTsIeMVTdvMMt0QqVKvrG_vVwL6ah3sq9vXIste_bXmTgCHfIUCKjW9sixsd7VXZymWpfEEFQw1Do");
			Data data = new ByteArrayData(null, "DATA".getBytes());
			Resource resource = new SimpleResource("test");
			client.create(credentials, resource, data, observer);
			ResponseMessage message = observer.take();
			assertEquals(MessageType.SUCCESS, message.getResponse());
			assertEquals(MessageType.CREATE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertEquals("test", message.getAffectedResource().getName());
			assertEquals("DATA", Utils.readString("htdocs/test"));
			// Try again, expect ALREADY_EXISTS
			client.create(credentials, resource, data, observer);
			message = observer.take();
			assertEquals(MessageType.ALREADY_EXISTS, message.getResponse());
			assertEquals(MessageType.CREATE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
			// Try with resource pattern, expect BAD_REQUEST
			Resource resource2 = new SimpleResource("+/test");
			client.create(credentials, resource2, data, observer);
			message = observer.take();
			assertEquals(MessageType.BAD_REQUEST, message.getResponse());
			assertEquals(MessageType.CREATE, message.getRequest());
			assertEquals("+/test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
			// Try without access rights, expect FORBIDDEM
			client.create(null, resource, data, observer);
			message = observer.take();
			assertEquals(MessageType.FORBIDDEN, message.getResponse());
			assertEquals(MessageType.CREATE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
		} finally {
			Utils.cleanup("htdocs");
		}
	}

	@Test
	public void testUpdate() throws IOException, InterruptedException, IllegalArgumentException, SignatureVerificationException, TokenExpiredException, JWTDecodeException, NoSuchAlgorithmException {
		try {
			// Make sure the resource test does exist
			Utils.write("htdocs/test", "DATA".getBytes());
			Credentials credentials = new JwtCredentials("eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJyZ3MiOiIjIGNydWQifQ.slKq7ISDZiLrtWoubpTftmR_6JhyX3vGoIQfLl9TMTAPFG5NSK0jXY0oLAZbDIQBU3KACTxUb6612PJkvQtCm04OFd8i2jpTsIeMVTdvMMt0QqVKvrG_vVwL6ah3sq9vXIste_bXmTgCHfIUCKjW9sixsd7VXZymWpfEEFQw1Do");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Data data = new ByteArrayData(null, "DUTU".getBytes());
			Resource resource = new SimpleResource("test");
			client.update(credentials, resource, data, observer);
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
			client.update(credentials, resource, data, observer);
			message = observer.take();
			assertEquals(MessageType.NOT_FOUND, message.getResponse());
			assertEquals(MessageType.UPDATE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
			// Try without access rights, expect FORBIDDEM
			client.update(null, resource, data, observer);
			message = observer.take();
			assertEquals(MessageType.FORBIDDEN, message.getResponse());
			assertEquals(MessageType.UPDATE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
		} finally {
			Utils.cleanup("htdocs");
		}
	}

	@Test
	public void testSave() throws IOException, InterruptedException, IllegalArgumentException, SignatureVerificationException, TokenExpiredException, JWTDecodeException, NoSuchAlgorithmException {
		try {
			// Make sure the resource test does not exist
			Utils.deleteIfExists("htdocs/test");
			Credentials credentials = new JwtCredentials("eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJyZ3MiOiIjIGNydWQifQ.slKq7ISDZiLrtWoubpTftmR_6JhyX3vGoIQfLl9TMTAPFG5NSK0jXY0oLAZbDIQBU3KACTxUb6612PJkvQtCm04OFd8i2jpTsIeMVTdvMMt0QqVKvrG_vVwL6ah3sq9vXIste_bXmTgCHfIUCKjW9sixsd7VXZymWpfEEFQw1Do");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Data data = new ByteArrayData(null, "DATA".getBytes());
			Resource resource = new SimpleResource("test");
			client.save(credentials, resource, data, observer);
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
			client.save(credentials, resource, data, observer);
			message = observer.take();
			assertEquals(MessageType.SUCCESS, message.getResponse());
			assertEquals(MessageType.SAVE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertEquals("test", message.getAffectedResource().getName());
			assertEquals("DUTU", Utils.readString("htdocs/test"));
			// Try without access rights, expect FORBIDDEM
			client.save(null, resource, data, observer);
			message = observer.take();
			assertEquals(MessageType.FORBIDDEN, message.getResponse());
			assertEquals(MessageType.SAVE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
		} finally {
			Utils.cleanup("htdocs");
		}
	}

	@Test
	public void testMultipleSave() throws IOException, InterruptedException, IllegalArgumentException, SignatureVerificationException, TokenExpiredException, JWTDecodeException, NoSuchAlgorithmException {
		try {
			// Make sure some resources do exist
			Utils.write("htdocs/a/b/c/test", "DATA".getBytes());
			Utils.write("htdocs/a/x/test", "DATA".getBytes());
			Credentials credentials = new JwtCredentials("eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJyZ3MiOiIjIGNydWQifQ.slKq7ISDZiLrtWoubpTftmR_6JhyX3vGoIQfLl9TMTAPFG5NSK0jXY0oLAZbDIQBU3KACTxUb6612PJkvQtCm04OFd8i2jpTsIeMVTdvMMt0QqVKvrG_vVwL6ah3sq9vXIste_bXmTgCHfIUCKjW9sixsd7VXZymWpfEEFQw1Do");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Data data = new ByteArrayData(null, "DUTU".getBytes());
			Resource resource = new SimpleResource("a/+/c/test");
			client.save(credentials, resource, data, observer);
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
			Utils.cleanup("htdocs");
		}
	}
	
	@Test
	public void testDelete() throws IOException, InterruptedException, IllegalArgumentException, SignatureVerificationException, TokenExpiredException, JWTDecodeException, NoSuchAlgorithmException {
		try {
			// Make sure the resource test does exist
			Utils.write("htdocs/test", "DATA".getBytes());
			Credentials credentials = new JwtCredentials("eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJyZ3MiOiIjIGNydWQifQ.slKq7ISDZiLrtWoubpTftmR_6JhyX3vGoIQfLl9TMTAPFG5NSK0jXY0oLAZbDIQBU3KACTxUb6612PJkvQtCm04OFd8i2jpTsIeMVTdvMMt0QqVKvrG_vVwL6ah3sq9vXIste_bXmTgCHfIUCKjW9sixsd7VXZymWpfEEFQw1Do");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Resource resource = new SimpleResource("test");
			client.delete(credentials, resource, observer);
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
			client.delete(credentials, resource, observer);
			message = observer.take();
			assertEquals(MessageType.NOT_FOUND, message.getResponse());
			assertEquals(MessageType.DELETE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
			// Try without access rights, expect FORBIDDEM
			client.delete(null, resource, observer);
			message = observer.take();
			assertEquals(MessageType.FORBIDDEN, message.getResponse());
			assertEquals(MessageType.DELETE, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
		} finally {
			Utils.cleanup("htdocs");
		}
	}
	
	@Test
	public void testRead() throws IOException, InterruptedException, IllegalArgumentException, SignatureVerificationException, TokenExpiredException, JWTDecodeException, NoSuchAlgorithmException {
		try {
			// Make sure the resource test does exist
			Utils.write("htdocs/test", "DATA".getBytes());
			Credentials credentials = new JwtCredentials("eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJyZ3MiOiIjIGNydWQifQ.slKq7ISDZiLrtWoubpTftmR_6JhyX3vGoIQfLl9TMTAPFG5NSK0jXY0oLAZbDIQBU3KACTxUb6612PJkvQtCm04OFd8i2jpTsIeMVTdvMMt0QqVKvrG_vVwL6ah3sq9vXIste_bXmTgCHfIUCKjW9sixsd7VXZymWpfEEFQw1Do");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			Resource resource = new SimpleResource("test");
			client.read(credentials, resource, observer);
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
			client.read(credentials, resource, observer);
			message = observer.take();
			assertEquals(MessageType.NOT_FOUND, message.getResponse());
			assertEquals(MessageType.READ, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
			// Try without access rights, expect FORBIDDEM
			client.read(null, resource, observer);
			message = observer.take();
			assertEquals(MessageType.FORBIDDEN, message.getResponse());
			assertEquals(MessageType.READ, message.getRequest());
			assertEquals("test", message.getResource().getName());
			assertNull(message.getReason());
			assertNull(message.getData());
			assertNull(message.getAffectedResource());
		} finally {
			Utils.cleanup("htdocs");
		}
	}
	
	@Test
	public void testSubscriptions() throws IOException, InterruptedException, IllegalArgumentException, SignatureVerificationException, TokenExpiredException, JWTDecodeException, NoSuchAlgorithmException {
		try {
			// Make sure the resource test does exist
			Utils.write("htdocs/test", "DATA".getBytes());
			Credentials credentials = new JwtCredentials("eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJyZ3MiOiIjIGNydWQifQ.slKq7ISDZiLrtWoubpTftmR_6JhyX3vGoIQfLl9TMTAPFG5NSK0jXY0oLAZbDIQBU3KACTxUb6612PJkvQtCm04OFd8i2jpTsIeMVTdvMMt0QqVKvrG_vVwL6ah3sq9vXIste_bXmTgCHfIUCKjW9sixsd7VXZymWpfEEFQw1Do");
			Utils.QueingObserver observer = new Utils.QueingObserver();
			client.subscribe(credentials, new SimpleResource("tast"), observer);
			Resource resource = new SimpleResource("test");
			client.subscribeStatus(credentials, resource, observer);
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
			do {
				message = observer.poll(100,TimeUnit.MILLISECONDS);
			} while (message != null);
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
			client.unsubscribe(credentials, resource, observer);
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
			client.unsubscribeAll(credentials, observer);
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
			Utils.cleanup("htdocs");
		}
	}
}
