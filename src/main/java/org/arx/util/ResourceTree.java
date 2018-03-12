package org.arx.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.arx.Resource;

/**
 * An object that maps resources to values. When retrieving mapped values, the
 * characters + and # are treated as wildcards (+ is a single level wildcard, #
 * is a multi-level-wildcard).
 * 
 * @param <T>
 *            the type of a value
 */
public class ResourceTree<T> {
	private Map<Resource, ResourceTree<T>> resourceTree;
	private Map<Resource, T> treeEntries;

	/**
	 * Constructs an empty resource tree.
	 */
	public ResourceTree() {
		resourceTree = new HashMap<Resource, ResourceTree<T>>();
		treeEntries = new LinkedHashMap<Resource, T>();
	}

	/**
	 * Retrieves the most specific match for the specified resource or null, if
	 * no matching resource an be found.
	 * 
	 * @param resource
	 *            the resource whose associated value is to be returned
	 * @return the most specific match for the specified resource or null, if no
	 *         matching resource an be found.
	 */
	public T get(Resource resource) {
		return get(resource, 0);
	}

	/**
	 * Associates the specified value with the specified resource. If this
	 * resource tree previously contained a mapping for the resource, the old
	 * value is replaced by the specified value.
	 * 
	 * @param resource
	 *            the resource with which the specified value is to be
	 *            associated
	 * @param value
	 *            the value to be associated with the specified key
	 * @return the previous value associated with resource, or null if there was
	 *         no mapping for the resource.
	 */
	public T put(Resource resource, T value) {
		return put(resource, value, 0);
	}

	/**
	 * Removes the mapping for a resource from this resource tree if it is
	 * present.
	 * 
	 * @param resource
	 *            the resource whose mapping is to be removed from the map
	 * @return the previous value associated with resource, or null if there was
	 *         no mapping for the resource.
	 */
	public T remove(Resource resource) {
		return remove(resource, 0);
	}

	/**
	 * Returns all entries of this resource tree as a map.
	 * 
	 * @return all entries of this resource tree as a map.
	 */
	public Map<Resource, T> getEntries() {
		Map<Resource, T> result = new LinkedHashMap<Resource, T>();
		result.putAll(treeEntries);
		for (ResourceTree<T> tree : resourceTree.values()) {
			result.putAll(tree.getEntries());
		}
		return result;
	}

	private T get(Resource resource, int part) {
		T entry = treeEntries.get(resource);
		if (entry != null) {
			return entry;
		}
		Resource partial = resource.subresource(0, part);
		int num = resource.getLevels().length;
		if (part + 1 == num) {
			Resource search = partial.resolve(Resource.SINGLE_LEVEL_WILDCARD);
			entry = treeEntries.get(search);
			if (entry != null) {
				return entry;
			}
		} else {
			Resource search = resource.subresource(0, part + 1);
			ResourceTree<T> tree = resourceTree.get(search);
			if (tree != null) {
				entry = tree.get(resource, part + 1);
				if (entry != null) {
					return entry;
				}
			}
			search = partial.resolve(Resource.SINGLE_LEVEL_WILDCARD);
			tree = resourceTree.get(search);
			if (tree != null) {
				Resource resourcePattern = resource.replaceLevel(part, Resource.SINGLE_LEVEL_WILDCARD);
				entry = tree.get(resourcePattern, part + 1);
				if (entry != null) {
					return entry;
				}
			}
		}
		Resource search = partial.resolve(Resource.MULTI_LEVEL_WILDCARD);
		entry = treeEntries.get(search);
		if (entry != null) {
			return entry;
		}
		return null;
	}

	private T put(Resource resource, T entry, int part) {
		int num = resource.getLevels().length;
		if (part + 1 == num) {
			return treeEntries.put(resource, entry);
		}
		Resource name = resource.subresource(0, part + 1);
		ResourceTree<T> tree = resourceTree.get(name);
		if (tree == null) {
			tree = new ResourceTree<T>();
			resourceTree.put(name, tree);
		}
		return tree.put(resource, entry, part + 1);
	}

	private T remove(Resource resource, int part) {
		int num = resource.getLevels().length;
		if (part + 1 == num) {
			return treeEntries.remove(resource);
		}
		Resource name = resource.subresource(0, part + 1);
		ResourceTree<T> tree = resourceTree.get(name);
		if (tree != null) {
			return tree.remove(resource, part + 1);
		}
		return null;
	}

}
