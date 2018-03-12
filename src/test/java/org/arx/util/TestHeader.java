package org.arx.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestHeader {

	@Test
	public void testHeader() {
		Header header = new Header();
		header.put("test", "value");
		assertEquals("value",header.get("test"));
		assertEquals("value",header.get("TEST"));
		assertEquals(1,header.size());
		header.put("Test", "New value");
		assertEquals("New value",header.get("Test"));
		assertEquals("New value",header.get("test"));
		assertEquals(1,header.size());
		header.remove("tEST");
		assertEquals(0,header.size());
		assertNull(header.get("test"));
	}
}
