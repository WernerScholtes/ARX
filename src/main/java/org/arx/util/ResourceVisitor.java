package org.arx.util;

import java.io.IOException;

import org.arx.Resource;

/**
 * A visitor of resources
 */
public interface ResourceVisitor {
	/**
	 * Invoked for an existing resource
	 * 
	 * @param resource
	 *            the resource that is being visited
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	void visitResource(Resource resource) throws IOException;
}
