package org.arx.util;

import static org.junit.Assert.*;

import java.util.Map;

import org.arx.Resource;
import org.junit.Test;

public class TestResourceTree {
	@Test
	public void testGetMostSpecific() {
		String[] res = {"a/b/c", "a/b/+", "a/b/#", "a/+/c", "a/+/+", "a/#", 
				"+/b/c", "+/b/+", "+/+/c", "+/+/+", "+/#", "#"};
		ResourceTree<String> rt = new ResourceTree<String>();
		for ( String re : res ) {
			rt.put(new SimpleResource(re), re);
		}
		assertEquals("a/b/c",rt.get(new SimpleResource("a/b/c")));
		assertEquals("a/b/+",rt.get(new SimpleResource("a/b/d")));
		assertEquals("a/b/#",rt.get(new SimpleResource("a/b/d/e")));
		assertEquals("a/+/c",rt.get(new SimpleResource("a/d/c")));
		assertEquals("a/+/+",rt.get(new SimpleResource("a/d/e")));
		assertEquals("a/#",rt.get(new SimpleResource("a/d")));
		assertEquals("a/#",rt.get(new SimpleResource("a/d/e/f")));
		assertEquals("+/b/c",rt.get(new SimpleResource("d/b/c")));
		assertEquals("+/b/+",rt.get(new SimpleResource("d/b/e")));
		assertEquals("+/+/c",rt.get(new SimpleResource("d/e/c")));
		assertEquals("+/+/+",rt.get(new SimpleResource("d/e/f")));
		assertEquals("+/#",rt.get(new SimpleResource("c/d/e/f")));
		assertEquals("+/#",rt.get(new SimpleResource("c/d")));
		assertEquals("#",rt.get(new SimpleResource("c")));
	}
	
	@Test
	public void testRemove() {
		String[] res = {"a/b/c", "a/b/+", "a/b/#", "a/+/c", "a/+/+", "a/#", 
				"+/b/c", "+/b/+", "+/+/c", "+/+/+", "+/#", "#"};
		ResourceTree<String> rt = new ResourceTree<String>();
		for ( String re : res ) {
			rt.put(new SimpleResource(re), re);
		}
		assertEquals("a/b/c",rt.get(new SimpleResource("a/b/c")));
		assertEquals("a/b/c",rt.remove(new SimpleResource("a/b/c")));
		assertEquals("a/b/+",rt.get(new SimpleResource("a/b/c")));
		assertNull(rt.remove(new SimpleResource("a/b/c")));
	}

	@Test
	public void testPut() {
		String[] res = {"a/b/c", "a/b/+", "a/b/#", "a/+/c", "a/+/+", "a/#", 
				"+/b/c", "+/b/+", "+/+/c", "+/+/+", "+/#", "#"};
		ResourceTree<String> rt = new ResourceTree<String>();
		for ( String re : res ) {
			rt.put(new SimpleResource(re), re);
		}
		assertEquals("a/b/c",rt.get(new SimpleResource("a/b/c")));
		assertEquals("a/b/c",rt.put(new SimpleResource("a/b/c"),"test"));
		assertEquals("test",rt.get(new SimpleResource("a/b/c")));
		assertNull(rt.put(new SimpleResource("a/b/c/d"), "a/b/c/d"));
	}

	@Test
	public void testGetEntries() {
		String[] res = {"a/b/c", "a/b/+", "a/b/#", "a/+/c", "a/+/+", "a/#", 
				"+/b/c", "+/b/+", "+/+/c", "+/+/+", "+/#", "#"};
		ResourceTree<String> rt = new ResourceTree<String>();
		for ( String re : res ) {
			rt.put(new SimpleResource(re), re);
		}
		Map<Resource,String> entries = rt.getEntries();
		assertEquals(res.length,entries.size());
		for ( String re : res ) {
			assertEquals(re,entries.get(new SimpleResource(re)));
		}
	}
}
