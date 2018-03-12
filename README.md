ARX (acronym for Asynchronous Resource eXchange) is a programming paradigm that determines how resources can be exchanged and altered asynchronously. It does not prescribe or require a specific communication protocol to be used between ARX clients and ARX servers.

An ARX client application must implement the Observer interface and uses the Endpoint interface, which is implement by a protocol specific ARX client protocol implementation.
The ARX client protocol implementation converts the calls to the methods of interface Endpoint to requests which are sent to an ARX server by using a specific communication protocol. It also receives response messages sent from the ARX server and converts them into method calls of the Observer interface, which is implemented by the ARX client application.

AN ARX server consists of two major parts:

The first part is the ARX server protocol implementation. It must implement the Server and the Observer interfaces. The ARX server protocol implementation receives request messages from an ARX client and converts them into method calls to the Endpoint interface. It also converts method calls to the Observer implementation into response messages and sends them to the ARX client.

The second part of the ARX server is the ARX resource backend implementation, which implements the Endpoint interface to receive requests and uses the Observer interface to respond to these requests. It is completely protocol agnostic. Its only task is to manage resources.

ARX comes with one predefined protocol implementation for TCP/IP communication (see TcpClient, TcpServer and TcpSession) and one predefined Endpoint implementation for file system resources (files are used as resources, see FileSystemBackend).

Any protocol implementation must implement the Endpoint, Server and Observer interfaces. Every Endpoint implementation must implement the Endpoint interface.

An ARX server can be implemented by using the BackendFactory and ProtocolFactory implementations specified in the ARX configuration. 
