package org.arx.util;

import static org.junit.Assert.*;

import java.util.Map;

import org.arx.Resource;
import org.arx.util.SimpleResource;
import org.arx.util.StringCredentials;
import org.junit.Test;

public class TestStringCredentials {

	@Test
	public void testParseNormal() {
		String cr = "# r, internal -, public crud";
		StringCredentials sc = new StringCredentials();
		sc.parseAuthorization(cr,0L);
		Map<Resource,String> entries = sc.getEntries();
		assertEquals(3,entries.size());
		assertEquals("r",entries.get(new SimpleResource("#")));
		assertEquals("-",entries.get(new SimpleResource("internal")));
		assertEquals("crud",entries.get(new SimpleResource("public")));
	}

	@Test
	public void testParseWhitespace() {
		String cr = "dir with whitespaces	 crud";
		StringCredentials sc = new StringCredentials();
		sc.parseAuthorization(cr,0L);
		Map<Resource,String> entries = sc.getEntries();
		assertEquals(1,entries.size());
		assertEquals("crud",entries.get(new SimpleResource("dir with whitespaces")));
	}
	
	@Test
	public void testSerialize() {
		String cr = "# r, internal -, public crud";
		StringCredentials sc = new StringCredentials(cr);
		assertEquals(cr,sc.serialize());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testIncomplete() {
		String cr = "# crud,error";
		StringCredentials sc = new StringCredentials();
		sc.parseAuthorization(cr,0L);
	}

}
