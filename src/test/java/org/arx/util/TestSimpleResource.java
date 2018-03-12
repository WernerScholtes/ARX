package org.arx.util;

import static org.junit.Assert.*;

import org.arx.Resource;
import org.junit.Test;

public class TestSimpleResource {

	@Test
	public void testEmptyConstructors() {
		Resource r1 = new SimpleResource();
		Resource r2 = new SimpleResource("");
		Resource r3 = new SimpleResource(new String[0]);
		assertEquals(r1,r2);
		assertEquals(r1,r3);
		assertEquals(r2,r3);
	}
	
	@Test
	public void testConstructors() {
		Resource r1 = new SimpleResource("/a/b/c");
		Resource r2 = new SimpleResource("a/b/c");
		String[] levels = {"a","b","c"};
		Resource r3 = new SimpleResource(levels);
		assertEquals(r1,r2);
		assertEquals(r1,r3);
		assertEquals(r2,r3);
	}
	
	@Test
	public void testWhitespace() {
		Resource r1 = new SimpleResource("a a/b\tb/c");
		assertEquals("a a/b\tb/c",r1.getName());
	}
	
	@Test
	public void testGetters() {
		Resource r1 = new SimpleResource("/a/b/c");
		assertEquals("a/b/c",r1.getName());
		String[] expected = {"a","b","c"};
		assertArrayEquals(expected,r1.getLevels());
		assertTrue(r1.isSimple());
		assertFalse(r1.isPattern());
		Resource r2 = new SimpleResource("/a/+/c");
		assertTrue(r2.isPattern());
		assertFalse(r2.isSimple());
	}
	
	@Test
	public void testResolve() {
		Resource r = new SimpleResource("a/b");
		assertEquals("a/b/c/d",r.resolve("c/d").getName());
		assertEquals("a/b", r.getName());
	}
	
	@Test
	public void testReplaceLevel() {
		Resource r = new SimpleResource("a/b/c/d");
		assertEquals("a/b/e/d", r.replaceLevel(2, "e").getName());
		assertEquals("a/b/c/d", r.getName());
	}

	@Test
	public void testSubresource() {
		Resource r = new SimpleResource("a/b/c/d");
		assertEquals("b/c", r.subresource(1, 3).getName());
		assertEquals("a/b/c/d", r.getName());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testEmptyLevel() {
		new SimpleResource("a//b");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalSingleWildcard() {
		new SimpleResource("a/b+/c");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalMultiWildcard() {
		new SimpleResource("a/b/c#");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMultiNotLast() {
		new SimpleResource("a/#/c");
	}
}
