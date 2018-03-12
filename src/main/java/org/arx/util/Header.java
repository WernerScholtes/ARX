package org.arx.util;

import java.util.TreeMap;

/**
 * A header contains key-value pairs where keys and values are strings. The
 * comparison of keys is case insensitive.
 */
public class Header extends TreeMap<String, String> {
	private static final long serialVersionUID = -7093277828203588214L;

	/**
	 * Constructs an empty header
	 */
	public Header() {
		super(String.CASE_INSENSITIVE_ORDER);
	}

}
