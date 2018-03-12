package org.arx.backend.file;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import org.arx.Endpoint;
import org.arx.MessageType;
import org.arx.Credentials;
import org.arx.Data;
import org.arx.Reason;
import org.arx.Resource;
import org.arx.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A file system backend is an implementation of a resource backend where
 * resources are files in a file system. A resource backend is an implementation
 * of the Endpoint interface for a specific type of resources.
 * <p>
 * The file system backend watches the entire file system below the root
 * directory for changes. Thus, changes to files can be communicated to
 * subscribers. In order to watch the file changes in background the file system
 * backend must be executed after creation.
 */
public class FileSystemBackend implements Endpoint {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemBackend.class);
	private static final String REGISTER_ERROR_FORMAT = "Error during registration of directory %1$s";
	private static final String CLOSE_ERROR = "Error while closing watch service";
	private Executor executor;
	private Subscriptions subscriptions;
	private WatchService watcher;
	private Map<Path, WatchKey> watchKeys;

	/**
	 * Creates a file system backend. After a successful creation of the file
	 * system backend it must be executed by an
	 * {@link java.util.concurrent.Executor} in order to watch changes to the
	 * file system.
	 * 
	 * @param executor
	 *            the executor that shall be used to execute asynchronous tasks.
	 * @throws IOException
	 *             if an IO error occurred during initialization
	 */
	public FileSystemBackend(Executor executor) throws IOException {
		this.executor = executor;
		this.subscriptions = new Subscriptions();
		this.watcher = FileSystems.getDefault().newWatchService();
		this.watchKeys = new HashMap<Path, WatchKey>();
		Path root = FileSystemFactory.getRoot();
		register(root);
		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				register(dir);
				return FileVisitResult.CONTINUE;
			}
		    @Override
		    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		    	return FileVisitResult.CONTINUE;
		    }
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Endpoint#ping(org.arx.Credentials, org.arx.Observer)
	 */
	@Override
	public void ping(Credentials credentials, Observer observer) {
		try {
			observer.onSuccess(MessageType.PING, null);
		} catch (IOException e) {
			LOGGER.error("Cannot send PING response", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Endpoint#create(org.arx.Credentials, org.arx.Resource,
	 * org.arx.Data, org.arx.Observer)
	 */
	@Override
	public void create(Credentials credentials, Resource resource, Data data, Observer observer) {
		Runnable request = new CreateRequest(credentials, resource, data, observer);
		executor.execute(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Endpoint#update(org.arx.Credentials, org.arx.Resource,
	 * org.arx.Data, org.arx.Observer)
	 */
	@Override
	public void update(Credentials credentials, Resource resourcePattern, Data data, Observer observer) {
		Runnable request = new UpdateRequest(credentials, resourcePattern, data, observer);
		executor.execute(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Endpoint#save(org.arx.Credentials, org.arx.Resource,
	 * org.arx.Data, org.arx.Observer)
	 */
	@Override
	public void save(Credentials credentials, Resource resourcePattern, Data data, Observer observer) {
		Runnable request = new SaveRequest(credentials, resourcePattern, data, observer);
		executor.execute(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Endpoint#delete(org.arx.Credentials, org.arx.Resource,
	 * org.arx.Observer)
	 */
	@Override
	public void delete(Credentials credentials, Resource resourcePattern, Observer observer) {
		Runnable request = new DeleteRequest(credentials, resourcePattern, observer);
		executor.execute(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Endpoint#read(org.arx.Credentials, org.arx.Resource,
	 * org.arx.Observer)
	 */
	@Override
	public void read(Credentials credentials, Resource resourcePattern, Observer observer) {
		Runnable request = new ReadRequest(credentials, resourcePattern, observer);
		executor.execute(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Endpoint#subscribe(org.arx.Credentials, org.arx.Resource,
	 * org.arx.Observer)
	 */
	@Override
	public void subscribe(Credentials credentials, Resource resourcePattern, Observer observer) {
		Runnable request = new SubscribeRequest(credentials, false, resourcePattern, observer, subscriptions);
		executor.execute(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Endpoint#subscribeStatus(org.arx.Credentials,
	 * org.arx.Resource, org.arx.Observer)
	 */
	@Override
	public void subscribeStatus(Credentials credentials, Resource resourcePattern, Observer observer) {
		Runnable request = new SubscribeRequest(credentials, true, resourcePattern, observer, subscriptions);
		executor.execute(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Endpoint#unsubscribe(org.arx.Credentials, org.arx.Resource,
	 * org.arx.Observer)
	 */
	@Override
	public void unsubscribe(Credentials credentials, Resource resourcePattern, Observer observer) {
		Runnable request = new UnsubscribeRequest(resourcePattern, observer, subscriptions);
		executor.execute(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Endpoint#unsubscribeAll(org.arx.Credentials,
	 * org.arx.Observer)
	 */
	@Override
	public void unsubscribeAll(Credentials credentials, Observer observer) {
		Runnable request = new UnsubscribeAllRequest(observer, subscriptions);
		executor.execute(request);
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
						Path path = (Path) event.context();
						Path fullPath = directory.resolve(path);
						Resource resource = FileSystemFactory.createResource(fullPath);
						LOGGER.error("Overflow: " + resource);
						Set<SubscriptionObserver> subs = subscriptions.match(resource);
						for (SubscriptionObserver sub : subs) {
							executor.execute(new SubscriptionOutOfSyncResponse(subscriptions, sub));
						}
					} else if (event.kind() == ENTRY_CREATE) {
						Path path = (Path) event.context();
						Path fullPath = directory.resolve(path);
						Resource resource = FileSystemFactory.createResource(fullPath);
						LOGGER.debug("Created: " + resource);
						if (Files.isDirectory(fullPath)) {
							register(fullPath);
						} else {
							Set<SubscriptionObserver> subs = subscriptions.match(resource);
							for (SubscriptionObserver sub : subs) {
								if (sub.getCredentials().canRead(sub.getResourcePattern())) {
									subscriptions.unsubscribe(sub);
								}
								executor.execute(new SubscriptionDataResponse(sub,resource,Reason.CREATED));
							}
						}
					} else if (event.kind() == ENTRY_MODIFY) {
						Path path = (Path) event.context();
						Path fullPath = directory.resolve(path);
						Resource resource = FileSystemFactory.createResource(fullPath);
						LOGGER.debug("Modified: " + resource);
						if (!Files.isDirectory(fullPath)) {
							Set<SubscriptionObserver> subs = subscriptions.match(resource);
							for (SubscriptionObserver sub : subs) {
								executor.execute(new SubscriptionDataResponse(sub,resource,Reason.UPDATED));
							}
						}
					} else if (event.kind() == ENTRY_DELETE) {
						Path path = (Path) event.context();
						Path fullPath = directory.resolve(path);
						Resource resource = FileSystemFactory.createResource(fullPath);
						LOGGER.debug("Deleted: " + resource);
						if (Files.isDirectory(fullPath)) {
							unregister(fullPath);
						} else {
							Set<SubscriptionObserver> subs = subscriptions.match(resource);
							for (SubscriptionObserver sub : subs) {
								executor.execute(
										new SubscriptionDataDeletedResponse(sub, resource));
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
			LOGGER.error(CLOSE_ERROR, e);
		}

	}

	private void register(Path path) {
		try {
			WatchKey key = path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
			watchKeys.put(path.normalize(), key);
		} catch (IOException e) {
			LOGGER.error(String.format(REGISTER_ERROR_FORMAT, path), e);
		}
	}

	private void unregister(Path path) {
		WatchKey key = watchKeys.get(path.normalize());
		if (key != null) {
			key.cancel();
			watchKeys.remove(path.normalize());
		}
	}

}
