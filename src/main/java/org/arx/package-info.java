/**
 * Contains interfaces and enumeration classes used to implement applications
 * that asynchronously exchange resources.
 * <p>
 * ARX (acronym for Asynchronous Resource eXchange) is a programming paradigm
 * that determines how resources can be exchanged and altered asynchronously. It
 * does not prescribe or require a specific communication protocol to be used
 * between ARX clients and ARX servers.
 * <p>
 * The following diagram shows the architecture of an ARX client and an ARX
 * server:
 * 
 * <pre>
 *        ARX client                              ARX server
 * +------------------------+          +---------------------------------+
 * | ARX client application |          |      ARX resource backend       |     /-----------\
 * |                        |          |         implementation          |     |           |
 * |       +----------------+          +----------------+                |&lt;---&gt;| resources |
 * |       |     Observer   |          |    Endpoint    |                |     |           |
 * |       | implementation |          | implementation |                |     \-----------/
 * +-------+----------------+          +----------------+----------------+
 *     |              ^                        ^                |
 *     |              |                        |                |
 *     v              |                        |                v
 * +----------------+-------+          +----------------+----------------+
 * |     Endpoint   |       |          |      Server    |     Observer   |
 * | implementation |       |          | implementation | implementation |
 * +----------------+       | request  +----------------+----------------+
 * |   ARX client protocol  |---------&gt;|       ARX server protocol       |
 * |     implementation     |&lt;---------|          implementation         |
 * +------------------------+ response +---------------------------------+
 * </pre>
 * 
 * An ARX client application must implement the {@link org.arx.Observer}
 * interface and uses the {@link org.arx.Endpoint} interface, which is implement
 * by a protocol specific ARX client protocol implementation.
 * 
 * <p>
 * The ARX client protocol implementation converts the calls to the methods of
 * interface Endpoint to requests which are sent to an ARX server by using a
 * specific communication protocol. It also receives response messages sent from
 * the ARX server and converts them into method calls of the Observer interface,
 * which is implemented by the ARX client application.
 * 
 * <p>
 * AN ARX server consists of two major parts:
 * 
 * <p>
 * The first part is the ARX server protocol implementation. It must implement
 * the {@link org.arx.protocol.Server} and the Observer interfaces. The ARX
 * server protocol implementation receives request messages from an ARX client
 * and converts them into method calls to the {@link org.arx.Endpoint}
 * interface. It also converts method calls to the Observer implementation into
 * response messages and sends them to the ARX client.
 * 
 * <p>
 * The second part of the ARX server is the ARX resource backend implementation,
 * which implements the Endpoint interface to receive requests and uses the
 * Observer interface to respond to these requests. It is completely protocol
 * agnostic. Its only task is to manage resources.
 * 
 * <p>
 * ARX comes with one predefined protocol implementation for TCP/IP
 * communication (see {@link org.arx.protocol.tcp.TcpClient},
 * {@link org.arx.protocol.tcp.TcpServer} and
 * {@link org.arx.protocol.tcp.TcpSession}) and one predefined Endpoint
 * implementation for file system resources (files are used as resources, see
 * {@link org.arx.backend.file.FileSystemBackend}).
 * 
 * <p>
 * Any protocol implementation must implement the Endpoint, Server and Observer
 * interfaces. Every Endpoint implementation must implement the Endpoint
 * interface.
 * 
 * <p>
 * An ARX server can be implemented by using the
 * {@link org.arx.backend.BackendFactory} and
 * {@link org.arx.protocol.ProtocolFactory} implementations specified in the ARX
 * configuration. the easiest implementation of a ServerExample class looks like:
 * 
 * <pre>
 * {@code
 * package org.arx.examples;
 * 
 * import java.util.concurrent.ExecutorService;
 * import java.util.concurrent.Executors;
 * 
 * import org.arx.Endpoint;
 * import org.arx.backend.BackendFactory;
 * import org.arx.protocol.Server;
 * import org.arx.protocol.ProtocolFactory;
 * import org.arx.util.Configuration;
 * 
 * public class ServerExample {
 * 	public static void main(String[] args) throws Exception {
 * 		// Create a cache pool for asynchronous execution of tasks
 * 		ExecutorService executor = Executors.newCachedThreadPool();
 * 		// Create the configuration singleton. $ARX_HOME must be set. 
 * 		Configuration configuration = Configuration.createInstance("");
 *		// Start watching for changes of the configuration files.
 * 		executor.execute(configuration);
 * 		// Create the backend factory specified in $ARX_HOME/conf/arx.conf
 * 		BackendFactory backendFactory = configuration.createBackendFactory();
 * 		// Create a backend object
 * 		Endpoint backend = backendFactory.createBackend(executor,null);
 * 		// Start the backend object
 * 		executor.execute(backend);
 * 		// Create the protocol factory specified in $ARX_HOME/conf/arx.conf
 * 		ProtocolFactory protocolFactory = configuration.createProtocolFactory();
 * 		// Creates a server object
 * 		Server server = protocolFactory.createServer(executor,null);
 * 		// Sets the backend to be used by the server object
 * 		server.setBackend(backend);
 *		// Starts the server
 * 		executor.execute(server);
 * 	}
 * }
 * }
 * </pre>
 * 
 * If the configuration file $ARX_HOME/conf/arx.conf contains the following
 * lines:
 * 
 * <pre>
 * org.arx.backend.BackendFactory org.arx.backend.file.FileSystemFactory
 * org.arx.protocol.ProtocolFactory org.arx.protocol.tcp.TcpFactory
 * org.arx.protocol.tcp.TcpServer.port 6789
 * </pre>
 * 
 * an ARX server with a file system backend and a TCP server (listening to port
 * 6789) will be created and started.
 */
package org.arx;