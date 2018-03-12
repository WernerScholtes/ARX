package org.arx.backend.file;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.arx.Resource;
import org.arx.util.ResourceVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A file system based resource walker. Only regular files will be visited.
 */
public class FileSystemWalker {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemWalker.class);
	private static final String WALK_ERROR = "Error while walking file tree";
	private boolean onlyExistingFiles;

	/**
	 * Creates a file system walker that only visits existing files.
	 */
	public FileSystemWalker() {
		this(true);
	}

	/**
	 * Creates a file system walker that implements the specified visiting
	 * strategy.
	 * 
	 * @param onlyExistingFiles
	 *            if false, existing and non-existing files will be visited.
	 *            Otherwise only existing files will be visited
	 */
	public FileSystemWalker(boolean onlyExistingFiles) {
		this.onlyExistingFiles = onlyExistingFiles;
	}

	/**
	 * Starts walking the specified resources.
	 * 
	 * @param resource
	 *            the resources to be inspected
	 * @param visitor
	 *            the visitor that is used for inspection of resources
	 * @throws IOException
	 *             if an IO error occurs during the resource walk
	 */
	public void walkResource(Resource resource, ResourceVisitor visitor) throws IOException {
		walkResources(resource, visitor, 0);
	}

	private void walkResources(Resource resource, final ResourceVisitor visitor, int part) throws IOException {
		Resource parent = resource.subresource(0, part);
		Path parentPath = FileSystemFactory.getPath(parent);
		if (part + 1 == resource.getLevels().length) {
			// Last part of resource name
			switch (resource.getLevels()[part]) {
			case "#":
				// Retrieve all files in parent directory and recursively in all
				// subdirectories
				try {
					if (Files.exists(parentPath) && Files.isDirectory(parentPath)) {
						Files.walkFileTree(FileSystemFactory.getPath(parent), new SimpleFileVisitor<Path>() {
							@Override
							public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
								Resource res = FileSystemFactory.createResource(file);
								visitor.visitResource(res);
								return FileVisitResult.CONTINUE;
							}
						});
					}
				} catch (IOException e) {
					LOGGER.error(WALK_ERROR, e);
				}
				break;
			case "+":
				// Retrieve all files in parent directory
				if (Files.exists(parentPath) && Files.isDirectory(parentPath)) {
					String[] directories = parentPath.toFile().list(new FilenameFilter() {
						@Override
						public boolean accept(File current, String name) {
							return !(new File(current, name).isDirectory());
						}
					});
					for (String directory : directories) {
						Resource res = parent.resolve(directory);
						visitor.visitResource(res);
					}
				}
				break;
			default:
				// Retrieve file for current resource
				Path path = FileSystemFactory.getPath(resource);
				if (!onlyExistingFiles || (Files.isRegularFile(path) && Files.exists(path))) {
					visitor.visitResource(resource);
				}
				break;
			}
		} else {
			// Intermediate part of resource name
			switch (resource.getLevels()[part]) {
			case "+":
				// Retrieve all subdirectories for parent directory an walk
				// resources recursively
				if (Files.exists(parentPath) && Files.isDirectory(parentPath)) {
					String[] directories = parentPath.toFile().list(new FilenameFilter() {
						@Override
						public boolean accept(File current, String name) {
							return new File(current, name).isDirectory();
						}
					});
					for (String directory : directories) {
						Resource res = resource.replaceLevel(part, directory);
						walkResources(res, visitor, part + 1);
					}
				}
				break;
			default:
				// Walk resources for next subdirectory level
				walkResources(resource, visitor, part + 1);
				break;
			}
		}
	}

}
