package org.arx.util;

import static org.junit.Assert.*;

import org.arx.MessageType;
import org.junit.Test;

public class TestRequestResource {
	@Test
	public void testGetters() {
		RequestResource rr = new RequestResource(MessageType.CREATE, new SimpleResource("test"));
		assertEquals(MessageType.CREATE,rr.getRequest());
		assertEquals("test", rr.getResource().getName());
	}

	@Test
	public void testNullResource() {
		RequestResource rr = new RequestResource(MessageType.CREATE, null);
		assertEquals(MessageType.CREATE,rr.getRequest());
		assertEquals("", rr.getResource().getName());
	}
}
