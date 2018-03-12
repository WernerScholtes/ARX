package org.arx.util;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.arx.backend.BackendFactory;
import org.arx.protocol.ProtocolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The singleton class Configuration reads and caches the configuration of ARX.
 * Changes of the configuration files $ARX_HOME/conf/arx.conf and
 * $ARX_HOME/conf/mime.types are being watched and the internal cache will be
 * updated automatically whenever one of the files is modified.
 * <p>
 * In order to watch for changes of the configuration files the singleton
 * instance of this class must be executed after creation as shown in the
 * following example:
 * 
 * <pre>
 * {
 * 	&#64;code
 * 	Executor executor = anExecutor;
 * 	Configuration configuration = Configuration.createInstance("");
 * 	executor.execute(configuration);
 * }
 * </pre>
 */
public class Configuration implements Runnable {
	/**
	 * Name of the environment variable that specifies the home directory of
	 * ARX.
	 */
	public static final String ARX_HOME = "ARX_HOME";

	private static final String BACKEND_FACTORY_KEY = "org.arx.backend.BackendFactory";
	private static final String PROTOCOL_FACTORY_KEY = "org.arx.protocol.ProtocolFactory";
	private static final String DEFAULT_HOME_PATH = "./";
	private static final String CONF_PATH = "conf";
	private static final String ARX_CONF = "arx.conf";
	private static final String MIME_TYPES = "mime.types";
	private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);
	private static Configuration instance;
	private Path home;
	private Path confDir;
	private WatchService watcher;
	private Map<String, String> mimeTypes;
	private Map<String, String> parameters;

	/**
	 * Constructs the singleton configuration object. The configuration object
	 * caches the entries from the configuration file $ARX_HOME/conf/arx.conf
	 * and the mime types stored in file $ARX_HOME/conf/mime.types. All changes
	 * to these files are being watched. If any changes occur the corresponding
	 * files are read and their entries are cached.
	 * 
	 * @param homePath
	 *            home path of ARX ($ARX_HOME)
	 */
	private Configuration(String homePath) {
		if (homePath == null || homePath.isEmpty()) {
			homePath = System.getenv().get(ARX_HOME);
		}
		if (homePath == null || homePath.isEmpty()) {
			homePath = DEFAULT_HOME_PATH;
		}
		this.home = FileSystems.getDefault().getPath(homePath).toAbsolutePath().normalize();
		this.confDir = home.resolve(CONF_PATH).normalize();
		readConfiguration();
		readMimeTypes();
		try {
			watcher = confDir.getFileSystem().newWatchService();
			confDir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		} catch (IOException e) {
			LOGGER.error("Error watching configuration directory " + confDir.toString(), e);
		}
	}

	/**
	 * Returns the home path of ARX.
	 * 
	 * @return home path of ARX ($ARX_HOME)
	 */
	public Path getHome() {
		return home;
	}

	/**
	 * Returns the configuration directory path of ARX.
	 * 
	 * @return the configuration directory path of ARX
	 */
	public Path getConfDir() {
		return confDir;
	}

	/**
	 * Retrieves the mime type for the specified file extension.
	 * 
	 * @param extension
	 *            the file extension whose associated mime type is to be
	 *            returned
	 * @return the mime type associated to the file extension or null, if file
	 *         extension is unknown
	 */
	public String getMimeType(String extension) {
		return mimeTypes.get(extension);
	}

	/**
	 * Retrieves the parameter value for the specified parameter key. The search
	 * for parameters is case sensitive.
	 * 
	 * @param key
	 *            the parameter key to be searched for
	 * @return the parameter value associated to key or null if no value is
	 *         associated to key
	 */
	public String getParameter(String key) {
		return parameters.get(key);
	}

	/**
	 * Creates a factory for backend-specific objects. The class to be used for
	 * the creation is specified with the parameter named "backendFactory" in
	 * $ARX_HOME/conf/arx.conf.
	 * 
	 * @return factory object for backend-specific objects or null, if no
	 *         factory object can be created.
	 * @throws ClassNotFoundException
	 *             if the class cannot be found
	 * @throws InstantiationException
	 *             if the class or its default constructor is not accessible.
	 * @throws IllegalAccessException
	 *             if the class represents an abstract class, an interface, an
	 *             array class, a primitive type, or void;
	 */
	public BackendFactory createBackendFactory()
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String factoryName = getParameter(BACKEND_FACTORY_KEY);
		if (factoryName == null) {
			return null;
		}
		Class<?> cl = Class.forName(factoryName);
		Object obj = cl.newInstance();
		if (obj instanceof BackendFactory) {
			return (BackendFactory) obj;
		}
		return null;
	}

	/**
	 * Creates a factory for protocol-specific objects. The class to be used for
	 * the creation is specified with the parameter named "protocolFactory" in
	 * $ARX_HOME/conf/arx.conf.
	 * 
	 * @return factory object for protocol-specific objects or null, if no
	 *         factory object can be created.
	 * @throws ClassNotFoundException
	 *             if the class cannot be found
	 * @throws InstantiationException
	 *             if the class or its default constructor is not accessible.
	 * @throws IllegalAccessException
	 *             if the class represents an abstract class, an interface, an
	 *             array class, a primitive type, or void;
	 */
	public ProtocolFactory createProtocolFactory()
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String factoryName = getParameter(PROTOCOL_FACTORY_KEY);
		if (factoryName == null) {
			return null;
		}
		Class<?> cl = Class.forName(factoryName);
		Object obj = cl.newInstance();
		if (obj instanceof ProtocolFactory) {
			return (ProtocolFactory) obj;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		WatchKey key;
		try {
			while ((key = watcher.take()) != null) {
				Path directory = (Path) key.watchable();
				List<WatchEvent<?>> events = key.pollEvents();
				for (WatchEvent<?> event : events) {
					if (event.kind() == OVERFLOW) {
						// Synchronize with file system
						LOGGER.warn("Overflow while watching configuration directory " + directory.toString());
						readConfiguration();
						readMimeTypes();
					} else {
						Path file = (Path) event.context();
						Path path = directory.resolve(file);
						if (Files.isRegularFile(path)) {
							String fileName = file.toString();
							if (fileName.equals(ARX_CONF)) {
								readConfiguration();
							} else if (fileName.equals(MIME_TYPES)) {
								readMimeTypes();
							}
						}
					}
				}
				key.reset();
			}
		} catch (InterruptedException e) {
			// Do nothing
		}
		try {
			watcher.close();
		} catch (IOException e) {
			LOGGER.error("Error while closing WatchService", e);
		}

	}

	/**
	 * Retrieve the singleton instance of the configuration. If the singleton
	 * instance has not yet been constructed constructed it will be created with
	 * default home path which is read from the environment variable $ARX_HOME.
	 * If this environment variable is not set, the current working directory
	 * will be used as home path.
	 * 
	 * @return singleton instance of the configuration
	 */
	public static Configuration getInstance() {
		if (instance == null) {
			synchronized (Configuration.class) {
				instance = new Configuration("");
			}
		}
		return instance;
	}

	/**
	 * Creates and returns a (new) singleton instance of the configuration. The
	 * homePath can be specified. If the variable homePath is null or empty, the
	 * environment variable $ARX_HOME will be read. If this environment variable
	 * is not set, the current working directory will be used as home path.
	 * 
	 * @param homePath
	 *            home path to be used
	 * @return singleton instance of the configuration
	 */
	public static Configuration createInstance(String homePath) {
		synchronized (Configuration.class) {
			instance = new Configuration(homePath);
		}
		return instance;
	}

	private void readMimeTypes() {
		Map<String, String> newMimeTypes = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
		Path mt = confDir.resolve(MIME_TYPES).normalize();
		if (Files.exists(mt)) {
			try {
				LOGGER.debug("Start reading mime types from " + mt.toString());
				List<String> lines = Files.readAllLines(mt);
				for (String line : lines) {
					line = line.trim();
					if (!line.startsWith("#")) {
						String[] words = line.trim().split("[ \t]+");
						for (int i = 1; i < words.length; ++i) {
							newMimeTypes.put(words[i], words[0]);
						}
					}
				}
				LOGGER.debug("Successfully finished reading mime types from " + mt.toString());
			} catch (IOException e) {
				LOGGER.error("Error reading mime types from " + mt.toString(), e);
			}
		}
		this.mimeTypes = newMimeTypes;
	}

	private void readConfiguration() {
		Map<String, String> newParameters = new HashMap<String, String>();
		Path conf = confDir.resolve(ARX_CONF).normalize();
		if (Files.exists(conf)) {
			try {
				LOGGER.debug("Start reading configuration file " + conf.toString());
				List<String> lines = Files.readAllLines(conf);
				int lineNum = 0;
				for (String line : lines) {
					++lineNum;
					line = line.trim();
					if ( ! line.isEmpty() && ! line.startsWith("#")) {
						String[] words = line.trim().split("[ \t]+", 2);
						if (words.length > 1) {
							newParameters.put(words[0], words[1]);
						} else {
							LOGGER.error("Illegal line " + line + " in line " + lineNum + " of configuration file "
									+ conf.toString());
						}
					}
				}
				LOGGER.debug("Successfully finished reading configuration file " + conf.toString());
			} catch (IOException e) {
				LOGGER.error("Error reading configuration file " + conf.toString(), e);
			}
		}
		parameters = newParameters;
	}

}
