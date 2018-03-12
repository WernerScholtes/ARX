package org.arx.util;

import java.util.LinkedHashMap;
import java.util.Map;

import org.arx.Credentials;
import org.arx.Resource;

/**
 * A string based implementation of the Credentials interface. Credentials are
 * specified as comma separated list of single resource access rights. Single
 * resource access rights are specified by a resource pattern and access rights
 * separated by whitespace. access rights are either a string containing the '-'
 * character or a string containing at most one of the following characters:
 * 
 * <pre>
 * c		access right CREATE is granted to the resource pattern
 * r		access right READ is granted to the resource pattern
 * u		access right UPDATE is granted to the resource pattern
 * d		access right DELETE is granted to the resource pattern
 * </pre>
 * 
 * If the access rights consists of the character '-' no access is granted to
 * the resource pattern Example 1:
 * 
 * <pre>
 * {@code
 * 	StringCredentials credentials = new StringCredentials();
 * 	credentials.parseAuthorization("# r");
 * }
 * </pre>
 * 
 * This credentials specify the access right READ to the root directory, the
 * file entries within the root directory and recursively to all sub-directories
 * of the root directory and their file entries. Example 2:
 * 
 * <pre>
 * {@code
 * 	StringCredentials credentials = new StringCredentials();
 * 	credentials.parseAuthorization("# r, internal/# -");
 * }
 * </pre>
 * 
 * This credentials specify the access right READ to the root directory, the
 * file entries within the root directory and recursively to all sub-directories
 * of the root directory and their file entries except the sub-directory
 * internal. All access rights are revoked from the directory internal its file
 * entries and recursively from all sub-directories of the directory internal
 * and their file entries.
 */
public class StringCredentials implements Credentials {
	private ResourceTree<AccessRights> credentials;

	/**
	 * Creates an empty credentials object
	 */
	public StringCredentials() {
		this.credentials = new ResourceTree<AccessRights>();
	}

	/**
	 * Creates credentials for the specified authorization string without
	 * setting an expiration time (see {@link #parseAuthorization(String, long)}
	 * ).
	 * 
	 * @param authorization
	 *            the authorization string to be parsed
	 * @throws IllegalArgumentException
	 *             if the specified authorization string is not valid
	 */
	public StringCredentials(String authorization) throws IllegalArgumentException {
		this();
		parseAuthorization(authorization, 0L);
	}

	/**
	 * Creates credentials for the specified authorization string with a
	 * specified expiration time (see {@link #parseAuthorization(String, long)}
	 * ).
	 * 
	 * @param authorization
	 *            the authorization string to be parsed
	 * @param expirationTime
	 *            expiration time in milliseconds since midnight, January 1 1970
	 *            UTC or 0L, if no expiration time shall be applied.
	 * @throws IllegalArgumentException
	 *             if the specified string is not a valid authorization string
	 */
	public StringCredentials(String authorization, long expirationTime) throws IllegalArgumentException {
		this();
		parseAuthorization(authorization, expirationTime);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Credentials#canCreate(org.arx.Resource)
	 */
	@Override
	public boolean canCreate(Resource resourcePattern) {
		AccessRights accessRights = get(resourcePattern);
		if (accessRights != null) {
			return accessRights.canCreate();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Credentials#canUpdate(org.arx.Resource)
	 */
	@Override
	public boolean canUpdate(Resource resourcePattern) {
		AccessRights accessRights = get(resourcePattern);
		if (accessRights != null) {
			return accessRights.canUpdate();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Credentials#canDelete(org.arx.Resource)
	 */
	@Override
	public boolean canDelete(Resource resourcePattern) {
		AccessRights accessRights = get(resourcePattern);
		if (accessRights != null) {
			return accessRights.canDelete();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Credentials#canRead(org.arx.Resource)
	 */
	@Override
	public boolean canRead(Resource resourcePattern) {
		AccessRights accessRights = get(resourcePattern);
		if (accessRights != null) {
			return accessRights.canRead();
		}
		return false;
	}

	/**
	 * Associates the specified access rights with the specified resource.
	 * 
	 * @param resource
	 *            the resource with which the specified access rights are to be
	 *            associated
	 * @param accessRights
	 *            the access rights to be associated with the specified resource
	 */
	public void put(Resource resource, AccessRights accessRights) {
		credentials.put(resource, accessRights);
	}

	/**
	 * Parses the specified authorization string without setting an expiration
	 * time (see {@link #parseAuthorization(String, long)})
	 * 
	 * @param authorization
	 *            the authorization string to be parsed
	 * @throws IllegalArgumentException
	 *             if the specified string is not a valid authorization string
	 */
	public void parseAuthorization(String authorization) throws IllegalArgumentException {
		parseAuthorization(authorization, 0L);
	}

	/**
	 * Parses the specified authorization string with a specified expiration
	 * time (an infinite expiration time is indicated by 0L). An authorization
	 * string is a possibly empty list of comma-separated pairs of resource
	 * patterns and access rights. A pair of resource name and access rights is
	 * whitespace-separated and describes the access privileges for a specific
	 * resource pattern.
	 * 
	 * @param authorization
	 *            the authorization string to be parsed
	 * @param expirationTime
	 *            expiration time in milliseconds since midnight, January 1 1970
	 *            UTC or 0L, if no expiration time shall be applied.
	 * @throws IllegalArgumentException
	 *             if the specified string is not a valid authorization string
	 */
	public void parseAuthorization(String authorization, long expirationTime) throws IllegalArgumentException {
		if (authorization != null && (expirationTime == 0L || System.currentTimeMillis() < expirationTime)) {
			authorization = authorization.trim();
			String[] pairs = authorization.split(",");
			for (String pair : pairs) {
				pair = pair.trim();
				if (!pair.isEmpty()) {
					// Split pair by last occurrence of whitespace
					int accessStart = pair.lastIndexOf(' ');
					accessStart = Math.max(accessStart, pair.lastIndexOf('\t'));
					if (accessStart < 0) {
						throw new IllegalArgumentException();
					}
					++accessStart;
					int resourceEnd = accessStart - 1;
					while (resourceEnd >= 0 && (pair.charAt(resourceEnd) == ' ' || pair.charAt(resourceEnd) == '\t')) {
						--resourceEnd;
					}
					++resourceEnd;
					String resource = pair.substring(0, resourceEnd);
					String access = pair.substring(accessStart);
					put(new SimpleResource(resource), new AccessRights(access, expirationTime));
				}

			}
		}
	}

	/**
	 * Returns the entries of this credentials object as map.
	 * 
	 * @return map with access rights associated to resources.
	 */
	public Map<Resource, String> getEntries() {
		Map<Resource, AccessRights> entries = credentials.getEntries();
		Map<Resource, String> result = new LinkedHashMap<Resource, String>();
		for (Map.Entry<Resource, AccessRights> entry : entries.entrySet()) {
			result.put(entry.getKey(), entry.getValue().toString());
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		Map<Resource, AccessRights> entries = credentials.getEntries();
		String result = "";
		for (Map.Entry<Resource, AccessRights> entry : entries.entrySet()) {
			if (!result.isEmpty()) {
				result += ", ";
			}
			result += entry.getKey().getName();
			result += " " + entry.getValue();
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.arx.Credentials#serialize()
	 */
	@Override
	public String serialize() {
		return toString();
	}

	private AccessRights get(Resource resourcePattern) {
		AccessRights accessRights = null;
		while ((accessRights = credentials.get(resourcePattern)) != null) {
			if (accessRights.isExpired()) {
				credentials.remove(resourcePattern);
			} else {
				break;
			}
		}
		return accessRights;
	}

	/**
	 * A access rights object encapsulates access privileges for CREATE, READ,
	 * UPDATE and DELETE access.
	 */
	private static class AccessRights {
		private static final String REVOKE_ALL = "-";
		private static final char CREATE_CHAR = 'c';
		private static final char READ_CHAR = 'r';
		private static final char UPDATE_CHAR = 'u';
		private static final char DELETE_CHAR = 'd';
		private static final int CREATE_BIT = 0;
		private static final int READ_BIT = 1;
		private static final int UPDATE_BIT = 2;
		private static final int DELETE_BIT = 3;
		private byte rights;
		private long expirationTime;

		/**
		 * Creates and initializes an access rights object with access
		 * privileges with the specified expiration time
		 * 
		 * @param accessRights
		 *            the string with access privileges (see {@link #set(String)
		 *            set(accessRights)})
		 * @param expirationTime
		 *            the expiration time in milliseconds since midnight,
		 *            January 1, 1970 UTC or 0L, if no expiration time shall be
		 *            applied.
		 * @throws IllegalArgumentException
		 *             if the specified string is not a valid access rights
		 *             string
		 */
		public AccessRights(String accessRights, long expirationTime) throws IllegalArgumentException {
			set(accessRights);
			this.expirationTime = expirationTime;
		}

		/**
		 * Sets the access privileges for this access rights object. The
		 * specified String with access rights must be empty, equal to "-" or
		 * consists of an arbitrary combination of the characters 'c', 'r', 'u'
		 * or 'd'. The string "" or "-" revokes all access privileges. If the
		 * string contains the characters 'c', 'r', 'u' or 'd', the following
		 * access privileges are granted: 'c': CREATE, 'r': READ, 'u': UPDATE,
		 * 'd': DELETE.
		 * 
		 * @param accessRights
		 *            string specifying the access privileges
		 * @throws IllegalArgumentException
		 *             if the specified string is not a valid access rights
		 *             string
		 */
		public void set(String accessRights) throws IllegalArgumentException {
			rights = 0;
			if (!accessRights.equals(REVOKE_ALL)) {
				for (int i = 0; i < accessRights.length(); i++) {
					char ch = accessRights.charAt(i);
					switch (Character.toLowerCase(ch)) {
					case CREATE_CHAR:
						rights |= (1 << CREATE_BIT);
						break;
					case READ_CHAR:
						rights |= (1 << READ_BIT);
						break;
					case UPDATE_CHAR:
						rights |= (1 << UPDATE_BIT);
						break;
					case DELETE_CHAR:
						rights |= (1 << DELETE_BIT);
						break;
					default:
						throw new IllegalArgumentException("Illegal character " + ch + " in access rights string");
					}
				}
			}
		}

		/**
		 * Returns true, if this access rights object is expired.
		 * 
		 * @return true, if this access rights object is expired and false
		 *         otherwise.
		 */
		public boolean isExpired() {
			if (expirationTime == 0L) {
				return false;
			}
			return System.currentTimeMillis() > expirationTime;
		}

		/**
		 * Returns true, if the CREATE access privilege is granted or false
		 * otherwise
		 * 
		 * @return true, if the CREATE access privilege is granted or false
		 *         otherwise
		 */
		public boolean canCreate() {
			if (isExpired()) {
				return false;
			}
			return (rights & (1 << CREATE_BIT)) != 0;
		}

		/**
		 * Returns true, if the READ access privilege is granted or false
		 * otherwise
		 * 
		 * @return true, if the READ access privilege is granted or false
		 *         otherwise
		 */
		public boolean canRead() {
			if (isExpired()) {
				return false;
			}
			return (rights & (1 << READ_BIT)) != 0;
		}

		/**
		 * Returns true, if the UPDATE access privilege is granted or false
		 * otherwise
		 * 
		 * @return true, if the UPDATE access privilege is granted or false
		 *         otherwise
		 */
		public boolean canUpdate() {
			if (isExpired()) {
				return false;
			}
			return (rights & (1 << UPDATE_BIT)) != 0;
		}

		/**
		 * Returns true, if the DELETE access privilege is granted or false
		 * otherwise
		 * 
		 * @return true, if the DELETE access privilege is granted or false
		 *         otherwise
		 */
		public boolean canDelete() {
			if (isExpired()) {
				return false;
			}
			return (rights & (1 << DELETE_BIT)) != 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			String result = "";
			if (rights == 0) {
				result = REVOKE_ALL;
			} else {
				if (canCreate()) {
					result += CREATE_CHAR;
				}
				if (canRead()) {
					result += READ_CHAR;
				}
				if (canUpdate()) {
					result += UPDATE_CHAR;
				}
				if (canDelete()) {
					result += DELETE_CHAR;
				}
			}
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof AccessRights) {
				AccessRights other = (AccessRights) obj;
				return this.rights == other.rights;
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return rights;
		}
	}

}
