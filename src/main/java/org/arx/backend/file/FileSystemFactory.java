package org.arx.backend.file;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Executor;

import org.arx.Endpoint;
import org.arx.Data;
import org.arx.Resource;
import org.arx.backend.BackendFactory;
import org.arx.util.ByteArrayData;
import org.arx.util.Configuration;
import org.arx.util.SimpleResource;

/**
 * A file system factory is an implementation of the interface BackendFactory
 * for a file system backed implementation of a resource backend.
 */
public class FileSystemFactory implements BackendFactory {
	private static final String ROOT_PATH = "htdocs";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.backend.BackendFactory#createBackend(java.util.concurrent.
	 * Executor, java.util.Map)
	 */
	@Override
	public Endpoint createBackend(Executor executor, Map<String, String> parameters) throws IOException {
		return new FileSystemBackend(executor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.backend.BackendFactory#createResource(java.lang.String)
	 */
	@Override
	public Resource createResource(String name) {
		return new SimpleResource(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.backend.BackendFactory#createData(java.lang.String, byte[])
	 */
	@Override
	public Data createData(String mimeType, byte[] data) {
		return new ByteArrayData(mimeType, data);
	}

	/**
	 * Creates a resource from a Path object
	 * 
	 * @param fullPath
	 *            full path name to be converted to a resource
	 * @return the resource that represents the specified path
	 */
	public static Resource createResource(Path fullPath) {
		Path root = FileSystemFactory.getRoot();
		Path path = root.relativize(fullPath);
		String[] parts = new String[path.getNameCount()];
		for (int i = 0; i < parts.length; ++i) {
			parts[i] = path.getName(i).toString();
		}
		return new SimpleResource(parts);
	}

	/**
	 * Converts a resource into a Path object.
	 * 
	 * @param resource
	 *            the resource to be converted to a path
	 * @return the path that implements the specified resource
	 */
	public static Path getPath(Resource resource) {
		String name = resource.getName();
		if (name.startsWith(Resource.LEVEL_SEPARATOR)) {
			name = name.substring(1);
		}
		Path root = getRoot();
		if (name.isEmpty()) {
			return root;
		}
		return root.resolve(name).normalize();
	}

	/**
	 * Returns the root directory that is used by the file system backend
	 * 
	 * @return the root directory that is used by the file system backend
	 */
	public static Path getRoot() {
		Path home = Configuration.getInstance().getHome();
		return home.resolve(ROOT_PATH).normalize();
	}

	/**
	 * Returns the mime type for the specified resource
	 * 
	 * @param resource
	 *            the resource for which the mime type shall be retrieved
	 * @return the mime type for the specified resource or null if the mime-type
	 *         cannot be found
	 */
	public static String getMimeType(Resource resource) {
		String[] parts = resource.getLevels();
		if (parts.length > 0) {
			String fileName = parts[parts.length - 1];
			int pos = fileName.lastIndexOf('.');
			if (pos >= 0) {
				String extension = fileName.substring(pos + 1);
				return Configuration.getInstance().getMimeType(extension);
			}
		}
		return null;
	}

}
