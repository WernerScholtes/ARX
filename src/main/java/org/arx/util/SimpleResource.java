package org.arx.util;

import org.arx.Resource;

/**
 * A simple implementation of the Resource interface with no other functionality
 * than the resource name.
 */
public class SimpleResource implements Resource {
	protected String[] levels;

	/**
	 * Constructs an empty resource
	 */
	public SimpleResource() {
		this("");
	}

	/**
	 * Constructs a resource with the specified name
	 * 
	 * @param name
	 *            the name of the resource
	 * @throws IllegalArgumentException
	 *             if the name is not a valid resource name
	 */
	public SimpleResource(String name) throws IllegalArgumentException {
		if (name == null || name.isEmpty()) {
			levels = new String[0];
		} else {
			if (name.startsWith(LEVEL_SEPARATOR)) {
				name = name.substring(1);
			}
			levels = name.split(LEVEL_SEPARATOR);
			assertValid();
		}
	}

	/**
	 * Constructs a resource for the specified levels
	 * 
	 * @param levels
	 *            the levels of the resource name
	 * @throws IllegalArgumentException
	 *             if the resulting resource is not valid
	 */
	public SimpleResource(String[] levels) throws IllegalArgumentException {
		if (levels == null) {
			levels = new String[0];
		}
		this.levels = levels;
		assertValid();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Resource#getName()
	 */
	@Override
	public String getName() {
		String result = "";
		for (String level : levels) {
			if (!result.isEmpty()) {
				result += LEVEL_SEPARATOR;
			}
			result += level;
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Resource#getLevels()
	 */
	@Override
	public String[] getLevels() {
		return levels;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Resource#resolve(java.lang.String)
	 */
	@Override
	public Resource resolve(String name) throws IllegalArgumentException {
		String[] newLevels;
		if (name == null || name.isEmpty()) {
			newLevels = new String[levels.length];
			System.arraycopy(levels, 0, newLevels, 0, levels.length);
		} else {
			if (name.startsWith(LEVEL_SEPARATOR)) {
				name = name.substring(1);
			}
			String[] addLevels = name.split(LEVEL_SEPARATOR);
			newLevels = new String[levels.length + addLevels.length];
			System.arraycopy(levels, 0, newLevels, 0, levels.length);
			System.arraycopy(addLevels, 0, newLevels, levels.length, addLevels.length);
		}
		return new SimpleResource(newLevels);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Resource#subresource(int, int)
	 */
	@Override
	public Resource subresource(int beginIndex, int endIndex) throws IllegalArgumentException {
		if (beginIndex < 0 || beginIndex >= levels.length) {
			throw new IllegalArgumentException();
		}
		if (endIndex < beginIndex || endIndex > levels.length) {
			throw new IllegalArgumentException();
		}
		String[] newParts = new String[endIndex - beginIndex];
		System.arraycopy(levels, beginIndex, newParts, 0, newParts.length);
		return new SimpleResource(newParts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Resource#replaceLevel(int, java.lang.String)
	 */
	@Override
	public Resource replaceLevel(int levelNum, String level) throws IllegalArgumentException {
		if (levelNum < 0 || levelNum >= levels.length) {
			throw new IllegalArgumentException();
		}
		String[] newParts = new String[levels.length];
		System.arraycopy(levels, 0, newParts, 0, levels.length);
		newParts[levelNum] = level;
		return new SimpleResource(newParts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Resource#isSimple()
	 */
	@Override
	public boolean isSimple() {
		String[] levels = getLevels();
		for (String level : levels) {
			if (level.equals(SINGLE_LEVEL_WILDCARD) || level.equals(MULTI_LEVEL_WILDCARD)) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Resource#isPattern()
	 */
	@Override
	public boolean isPattern() {
		String[] levels = getLevels();
		for (String level : levels) {
			if (level.equals(SINGLE_LEVEL_WILDCARD) || level.equals(MULTI_LEVEL_WILDCARD)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Resource) {
			Resource other = (Resource) obj;
			return this.getName().equals(other.getName());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Resource#compareTo(org.arx.Resource)
	 */
	@Override
	public int compareTo(Resource other) {
		return this.getName().compareTo(other.getName());
	}

	protected void assertValid() throws IllegalArgumentException {
		String[] levels = getLevels();
		for (int i = 0; i < levels.length; ++i) {
			if (levels[i].isEmpty()) {
				throw new IllegalArgumentException("A resource must not contain empty levels");
			}
			if (levels[i].contains("+") && !levels[i].equals("+")) {
				throw new IllegalArgumentException(
						"A level of a resource containing a \"+\"-sign must be equal to \"+\"");
			}
			if (levels[i].contains("#") && !levels[i].equals("#")) {
				throw new IllegalArgumentException(
						"A level of a resource containing a \"#\"-sign must be equal to \"#\"");
			}
			if (levels[i].equals("#") && i + 1 != levels.length) {
				throw new IllegalArgumentException("Only the last level of a resource may be equal to \"#\"");
			}
		}
	}

}
