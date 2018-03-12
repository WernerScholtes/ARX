package org.arx.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.arx.Data;
import org.arx.MessageType;
import org.arx.Observer;
import org.arx.Reason;
import org.arx.Resource;

public class Utils {
	public static final String HOME = "src/test/resources";
	public static final Path HOME_PATH = FileSystems.getDefault().getPath(HOME).toAbsolutePath().normalize();

	public static void copyFile(String source, String dest) throws IOException {
		Path sourcePath = HOME_PATH.resolve(source);
		Path destPath = HOME_PATH.resolve(dest);
		Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
	}

	public static void moveFile(String source, String dest) throws IOException {
		Path sourcePath = HOME_PATH.resolve(source);
		Path destPath = HOME_PATH.resolve(dest);
		Files.move(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
	}

	public static void deleteIfExists(String file) throws IOException {
		Path path = HOME_PATH.resolve(file);
		Files.deleteIfExists(path);
	}

	public static Object readString(String file) throws IOException {
		Path path = HOME_PATH.resolve(file);
		return new String(Files.readAllBytes(path));
	}

	public static Object fileExists(String file) {
		Path path = HOME_PATH.resolve(file);
		return Files.exists(path);
	}

	public static void write(String file, byte[] bytes) throws IOException {
		Path path = HOME_PATH.resolve(file);
		Path parent = path.getParent();
		if (!Files.exists(parent)) {
			parent.toFile().mkdirs();
		}
		Files.write(path, bytes);
	}
	
	public static void cleanup(String root) throws IOException {
		Path path = HOME_PATH.resolve(root);
		cleanup(path.toFile(),false);
	}

	public static void cleanup(File file,boolean deleteDir) {
	    File[] allContents = file.listFiles();
	    if (allContents != null) {
	        for (File sub : allContents) {
	        	cleanup(sub,true);
	        }
	    }
	    if ( deleteDir ) {
	    	file.delete();
	    }
	}

	public static class QueingObserver implements Observer {
		private static int maxId = 0;
		private BlockingQueue<ResponseMessage> queue;
		private int id;

		public QueingObserver() {
			queue = new LinkedBlockingQueue<ResponseMessage>();
			id = ++maxId;
		}

		@Override
		public void onSuccess(MessageType request, Resource resource, Resource... affectedResources)
				throws IOException {
			queue.add(new ResponseMessage(null, MessageType.SUCCESS, request, resource, null, null, affectedResources));
		}

		@Override
		public void onData(MessageType request, Resource resource, Reason reason, Resource affectedResource, Data data)
				throws IOException {
			queue.add(new ResponseMessage(null, MessageType.DATA, request, resource, reason, data, affectedResource));
		}

		@Override
		public void onError(MessageType request, Resource resource, MessageType status) throws IOException {
			queue.add(new ResponseMessage(null, status, request, resource, null, null));
		}

		public ResponseMessage take() throws InterruptedException {
			return queue.take();
		}

		public ResponseMessage poll(long timeout, TimeUnit unit) throws InterruptedException {
			return queue.poll(timeout, unit);
		}
		
		@Override
		public String toString() {
			return Integer.toString(id);
		}
	}

}
