package org.arx.backend.file;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;

import org.arx.util.ByteArrayData;
import org.arx.util.Configuration;
import org.arx.util.SimpleResource;
import org.arx.util.Utils;
import org.junit.Before;
import org.junit.Test;

public class TestFileSystemFactory {
	@Before
	public void init() {
		Configuration.createInstance(Utils.HOME);
	}
	
	@Test
	public void testFactory() throws IOException {
		FileSystemFactory factory = new FileSystemFactory();
		assertEquals(FileSystemBackend.class,factory.createBackend(null, null).getClass());
		assertEquals(SimpleResource.class,factory.createResource("test").getClass());
		assertEquals(ByteArrayData.class,factory.createData(null,null).getClass());
	}
	
	@Test
	public void testStaticMethods() {
		Path root = FileSystemFactory.getRoot();
		assertEquals(Utils.HOME_PATH.resolve("htdocs"),root);
		Path path = FileSystemFactory.getPath(new SimpleResource("a"));
		assertEquals(root.resolve("a"),path);
		path = path.resolve("b/c");
		assertEquals("a/b/c", FileSystemFactory.createResource(path).getName());
		assertEquals("application/xml",FileSystemFactory.getMimeType(new SimpleResource("a.xml")));
		assertNull(FileSystemFactory.getMimeType(new SimpleResource("a")));
		assertNull(FileSystemFactory.getMimeType(new SimpleResource("a.hfjdkslfh")));
	}
}
