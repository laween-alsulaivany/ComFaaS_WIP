// CloudServer.java

package comfaas;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


// ------------------------------------------
// * The Server (or CloudServer) class extends CoreOperations and handles
// * inbound socket operations for the Cloud. It listens for client connections
// * (which might be EdgeClient or any other client) and manages requests.
// ------------------------------------------
public class CloudServer extends CoreOperations {
    // Logging
    private static final Logger server_logger = new Logger("cloudServer_logs.csv");

    // ------------------------------------------
    // Default constructor, if needed.
    // ------------------------------------------
 public CloudServer() {
     server_logger.logInfo("CloudServer instance created (no client yet).");

 }

    // ------------------------------------------
    // Constructor that accepts a ServerSocket, accepts one client connection,
    // and initializes the dis/dos streams.
    // ------------------------------------------
    public CloudServer(ServerSocket tmp) throws IOException {
        server_logger.logNewline("--------------------");
        server_logger.logInfo("CloudServer: Awaiting an incoming client connection...");
        Socket clientSocket = tmp.accept();
        this.socket = clientSocket;
        this.dis = new DataInputStream(clientSocket.getInputStream());
        this.dos = new DataOutputStream(clientSocket.getOutputStream());
        server_logger.logSuccess("Cloud Connection established with client");
    }

    // ------------------------------------------
    // Starts a server on the Cloud side to accept connections from a "real" client.
    // ------------------------------------------
    public static void run(int port) throws IOException, InterruptedException {
        server_logger.log("INFO", "Server started and listening on port " + port, "CloudServer", "Initialization", -1);
        server_logger.logNetwork("Cloud server listening on port " + port);

        try {
            ServerSocket cloudServerSocket = new ServerSocket(port);
            // CloudServer server = new CloudServer(cloudServerSocket);

            // Accept incoming connections in a loop
            while (true) {
                CloudServer server = null;
                try {
                server = new CloudServer(cloudServerSocket);
                server_logger.log("INFO", "Accepted client connection", "CloudServer", "Connection", -1);
                server_logger.logSuccess( "Accepted client connection on cloud server");
                
                // handle all incoming requests
                server.manageRequests();

                server_logger.logSuccess("Request handled successfully");
                server_logger.log("INFO", "Request handled successfully", "CloudServer", "Server", -1);
            
            } catch (IOException | InterruptedException e) {
                    server_logger.logError("Error handling client request: " + e.getMessage(), "CloudServer", "Connection");
            } finally {
                    // clean up
                    try {
                        server.close();
                    } catch (IOException e) {
                        // System.err.println("Error closing cloud server resources: " + e.getMessage());
                        server_logger.logError("Error closing cloud server resources: " + e.getMessage(), "CloudServer", "Closing");
                    }
                }
            }
        } 
        catch (IOException e) {
            server_logger.logError("Error starting Cloud server: " + e.getMessage(), "CloudServer", "Initialization");
        }

    }

}

// //
// // CloudServer.java

// package comfaas;

// import java.io.DataInputStream;
// import java.io.DataOutputStream;
// import java.io.EOFException;
// import java.io.IOException;
// import java.net.ServerSocket;
// import java.net.Socket;

// // ------------------------------------------
// // * The Server (or CloudServer) class extends CoreOperations and handles
// // * inbound socket operations for the Cloud. It listens for client connections
// // * (which might be EdgeClient or any other client) and manages requests.
// // ------------------------------------------
// public class CloudServer extends CoreOperations {

//     // Logging
//     private static final Logger server_logger = new Logger("server_logs.csv");

//     // ------------------------------------------
//     // Default constructor, if needed.
//     // ------------------------------------------
//     public CloudServer() {
//         System.out.println("CloudServer instance created (no client yet).");
//     }

//     // ------------------------------------------
//     // Constructs a CloudServer by accepting one incoming connection
//     // on the provided ServerSocket, then sets up dis/dos streams.
//     // ------------------------------------------
//     public CloudServer(ServerSocket tmp) throws IOException {
//         System.out.println("CloudServer: Awaiting an incoming client connection...");
//         Socket clientSocket = tmp.accept();
//         this.socket = clientSocket;
//         this.dis = new DataInputStream(clientSocket.getInputStream());
//         this.dos = new DataOutputStream(clientSocket.getOutputStream());
//         System.out.println("Cloud Connection established with client");
//     }

//     // ------------------------------------------
//     // Runs the server loop, continuously accepting new connections
//     // and calling manageRequests() from CoreOperations.
//     // ------------------------------------------
//     public static void run(int port) throws IOException, InterruptedException {
//         server_logger.log("INFO", "Server started and listening on port " + port, "CloudServer", "Initialization", -1);
//         System.out.println("Cloud server listening on port " + port);

//         try (ServerSocket cloudServerSocket = new ServerSocket(port)) {
//             while (true) {
//                 CloudServer server = null;
//                 try {
//                     // Only accept and construct CloudServer once per client
//                     server = new CloudServer(cloudServerSocket);
//                     System.out.println("Accepted client connection on cloud server");
//                     server_logger.log("INFO", "Accepted client connection", "CloudServer", "Connection", -1);

//                     // handle all incoming requests
//                     server.manageRequests();

//                     System.out.println("Finished handling client request(s)");
//                     server_logger.log("INFO", "Request handled successfully", "CloudServer", "Server", -1);

//                 } catch (IOException | InterruptedException e) {
//                     System.err.println("Error handling client request: " + e.getMessage());
//                     server_logger.logError("Error handling client request: " + e.getMessage(), "CloudServer", "Connection");
//                 } finally {
//                     // clean up
//                     try {
//                         server.close();
//                     } catch (IOException e) {
//                         System.err.println("Error closing cloud server resources: " + e.getMessage());
//                         server_logger.logError("Error closing cloud server resources: " + e.getMessage(), "CloudServer", "Closing");
//                     }
//                 }
//             }
//         } catch (IOException e) {
//             System.err.println("Error starting Cloud server: " + e.getMessage());
//             server_logger.logError("Error starting Cloud server: " + e.getMessage(), "CloudServer", "Initialization");
//         }
//     }

//     @Override
//     public void manageRequests() throws IOException, InterruptedException {
//         String command;
//         while (true) {
//             command = null;
//             try {
//                 command = this.dis.readUTF();
//             } catch (EOFException e) {
//                 System.out.println("Client closed connection unexpectedly.");
//                 break;
//             }
//             if (command == null || "done".equals(command)) {
//                 System.out.println("No more commands or client finished. Exiting.");
//                 break;
//             }
//             // ...existing code...
//         }
//         // ...existing code...
//     }
// }


