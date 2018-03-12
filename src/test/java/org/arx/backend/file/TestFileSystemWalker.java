package org.arx.backend.file;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.arx.Resource;
import org.arx.util.Configuration;
import org.arx.util.ResourceVisitor;
import org.arx.util.SimpleResource;
import org.arx.util.Utils;
import org.junit.Before;
import org.junit.Test;

public class TestFileSystemWalker {
	@Before
	public void init() {
		Configuration.createInstance(Utils.HOME);
	}

	@Test
	public void testExistingOnly() throws IOException {
		try {
			// Make sure some resources do exist
			Utils.write("htdocs/a/b/c/test", "DATA".getBytes());
			Utils.write("htdocs/a/x/test", "DATA".getBytes());
			Resource resource = new SimpleResource("a/+/c/test");
			Set<String> visited = new HashSet<String>();
			FileSystemWalker walker = new FileSystemWalker();
			walker.walkResource(resource, new ResourceVisitor() {
				@Override
				public void visitResource(Resource res) throws IOException {
					visited.add(res.getName());
				}

			});
			assertEquals(1,visited.size());
			assertEquals(true,visited.contains("a/b/c/test"));
		} finally {
			Utils.cleanup("htdocs");
		}
	}

	@Test
	public void testNotOnlyExisting() throws IOException {
		try {
			// Make sure some resources do exist
			Utils.write("htdocs/a/b/c/test", "DATA".getBytes());
			Utils.write("htdocs/a/x/test", "DATA".getBytes());
			Resource resource = new SimpleResource("a/+/c/test");
			Set<String> visited = new HashSet<String>();
			FileSystemWalker walker = new FileSystemWalker(false);
			walker.walkResource(resource, new ResourceVisitor() {
				@Override
				public void visitResource(Resource res) throws IOException {
					visited.add(res.getName());
				}

			});
			assertEquals(2,visited.size());
			assertEquals(true,visited.contains("a/b/c/test"));
			assertEquals(true,visited.contains("a/x/c/test"));
		} finally {
			Utils.cleanup("htdocs");
		}
	}
}
