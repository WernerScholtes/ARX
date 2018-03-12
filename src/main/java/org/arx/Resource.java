package org.arx;

/**
 * Every resource is represented by a name. Resources can represent completely
 * different things. For example, they can be represented by files in a file
 * system, topics in an MQTT message broker or cached data in a distributed
 * cache.
 * <p>
 * A resource name is a UTF-8 string, which is used to access resources. It
 * consists of one or more levels where the levels are separated by a forward
 * slash (level separator). Each resource name must have at least 1 character to
 * be valid and it can also contain spaces. Also a resource name is
 * case-sensitive, which makes traffic/data and Traffic/Data two individual
 * resource names. It is allowed to use a leading forward slash in a resource
 * name, for example /traffic/info. But that should be avoided, because it
 * does not provide any benefit and often leads to confusion.
 * <p>
 * When referencing resources the exact resource names can be used or a resource
 * pattern that matches several resources can be used. A resource pattern uses
 * wildcards to reference zero, one or many resources. There are two different
 * kinds of wildcards: single level and multi level wildcards.
 * <p>
 * <strong>Single level wildcard: +</strong>
 * <p>
 * As the name already suggests, a single level wildcard is a substitute for one
 * level. The plus symbol (+) represents a single level wildcard in the resource
 * name. Any resource name matches to a resource pattern including the single
 * level wildcard if it contains an arbitrary string instead of the wildcard.
 * For example a resource pattern of
 * 
 * <pre>
 * traffic/+/velocity
 * </pre>
 * 
 * would <strong>match</strong> the following resource names:
 * 
 * <pre>
 * traffic/data/velocity
 * traffic/info/velocity
 * </pre>
 * 
 * but it would <strong>not match</strong> the following resource names:
 * 
 * <pre>
 * traffic/velocity
 * traffic/data/id123/velocity
 * </pre>
 * <p>
 * <strong>Multi level wildcard: #</strong>
 * <p>
 * While the single level wildcard only covers one level, the multi level
 * wildcard (#) covers an arbitrary number of levels. In order to determine the
 * matching resource names it is required that the multi level wildcard is
 * always the last character in the resoure name and it is preceded by a forward
 * slash. For example a resource pattern of
 * 
 * <pre>
 * traffic/#
 * </pre>
 * 
 * would <strong>match</strong> the following resource names:
 * 
 * <pre>
 * traffic/value
 * traffic/info/velocity
 * traffic/data/id123/velocity
 * </pre>
 * 
 * but it would <strong>not match</strong> the following resource names:
 * 
 * <pre>
 * environment / temperature / id123
 * </pre>
 * 
 * A resource pattern with a multi level wildcard matches all resource names,
 * which start with the pattern before the wildcard character, no matter how
 * long or deep the resource names will get. If you only specify the multilevel
 * wildcard as a resource pattern (#), it means that the resource pattern will
 * match all resources. In most cases this is considered an anti-pattern. It is
 * only useful for granting default access rights for all resources.
 */
public interface Resource extends Comparable<Resource> {
	/**
	 * The single level wildcard character
	 */
	String SINGLE_LEVEL_WILDCARD = "+";
	/**
	 * The multi level wildcard character
	 */
	String MULTI_LEVEL_WILDCARD = "#";
	/**
	 * The line separator character
	 */
	String LEVEL_SEPARATOR = "/";

	/**
	 * Returns the name of this resource or resource pattern.
	 * 
	 * @return the name of this resource or resource pattern
	 */
	String getName();

	/**
	 * Return the resource name as a String array with one entry for every
	 * level.
	 * 
	 * @return the levels of this resource or resource pattern as a String array
	 */
	String[] getLevels();

	/**
	 * Returns a new resource object starting with the levels of this resource
	 * object concatenated by the specified name.
	 * 
	 * @param name
	 *            partial resource name to be concatenated with this resource
	 *            name.
	 * @return a new resource as concatenation of this resource name and the
	 *         specified name
	 * @throws IllegalArgumentException
	 *             if the resulting string is not a valid resource name
	 */
	Resource resolve(String name) throws IllegalArgumentException;

	/**
	 * Returns a new resource object whose name is a part of this resource name.
	 * The partial resource name begins at the specified beginIndex and extends
	 * to the resource level at index endIndex - 1. Thus the number of levels of
	 * the subresource is endIndex-beginIndex.
	 * 
	 * @param beginIndex
	 *            the beginning index, inclusive.
	 * @param endIndex
	 *            the ending index, exclusive.
	 * @return the specified subresource
	 * @throws IllegalArgumentException
	 *             if the beginIndex is negative, or endIndex is larger than the
	 *             number of levels of this resource object, or beginIndex is
	 *             larger than endIndex.
	 */
	Resource subresource(int beginIndex, int endIndex) throws IllegalArgumentException;

	/**
	 * Returns a new resource object whose name is a copy of this resource name
	 * and where the specified level index is replaced by the given lavel
	 * string.
	 * 
	 * @param index
	 *            index of level to be replaced
	 * @param level
	 *            new level string
	 * @return the resource with the specified replacement
	 * @throws IllegalArgumentException
	 *             if the index is less than 0 or greater or equal to the number
	 *             of levels.
	 */
	Resource replaceLevel(int index, String level) throws IllegalArgumentException;

	/**
	 * Returns true, if this resource represents a single resource, e.g. does
	 * not contain any wildcard.
	 * 
	 * @return true, if this resource represents a single resource or false
	 *         otherwise
	 */
	boolean isSimple();

	/**
	 * Returns true, if this resource represents a resource pattern, e.g.
	 * contains at least one wildcard
	 * 
	 * @return true, if this resource represents a resource pattern or false
	 *         otherwise
	 */
	boolean isPattern();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	default int compareTo(Resource other) {
		return this.getName().compareTo(other.getName());
	}
}
