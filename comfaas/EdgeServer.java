// EdgeServer.java

package comfaas;



import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

    // ------------------------------------------
    // * The EdgeServer class represents an edge node acting as a server
    // * for local clients (or any external client), receiving requests and
    // * handling them with CoreOperations (file transfers, etc.).
    // ------------------------------------------
public class EdgeServer extends CoreOperations {
    // Logging
    private static final Logger server_logger = new Logger("edgeServer_logs.csv");

    // ------------------------------------------
    // Default constructor, if needed.
    // ------------------------------------------
    public EdgeServer() {
        server_logger.logInfo("EdgeServer instance created (no client yet).");
    }

    // ------------------------------------------
    // Constructor that accepts a ServerSocket, accepts one client connection,
    // and initializes the dis/dos streams.
    // ------------------------------------------
    public EdgeServer(ServerSocket tmp) throws IOException {
        server_logger.logNewline("--------------------");
        server_logger.logInfo("EdgeServer: Awaiting an incoming client connection...");
        Socket clientSocket = tmp.accept();
        this.socket = clientSocket;
        this.dis = new DataInputStream(clientSocket.getInputStream());
        this.dos = new DataOutputStream(clientSocket.getOutputStream());
        server_logger.logSuccess("Edge Connection established with client");
    }


    // ------------------------------------------
    // Starts a server on the Edge side to accept connections from a "real" client.
    // ------------------------------------------
    public static void run(int port) throws IOException, InterruptedException {
        server_logger.log("INFO", "Edge server starting on port " + port, "EdgeServer", "Initialization", -1);
        server_logger.logNetwork("Edge server listening on port " + port);
        
        try {
            ServerSocket edgeServerSocket = new ServerSocket(port);


            // Accept incoming connections in a loop
            while (true) {
                EdgeServer server = null;
                try {
                    // Create a new EdgeServer instance for each new client
                    server = new EdgeServer(edgeServerSocket);
                    server_logger.log("INFO", "Accepted client connection", "EdgeServer", "Connection", -1);
                    server_logger.logSuccess( "Accepted client connection on edge server");

                    // handle all incoming requests
                    server.manageRequests(); // from CoreOperations

                    server_logger.logSuccess("Request handled successfully");
                    server_logger.log("INFO", "Request handled successfully", "EdgeServer", "Server", -1);

                } catch (IOException | InterruptedException e) {
                    server_logger.logError("Error accepting client connection: " + e.getMessage(), "EdgeServer", "Connection");
                } finally {
                    // clean up
                    try {
                        server.close();
                    } catch (IOException e) {
                        server_logger.logError("Error closing edge server resources: " + e.getMessage(), "CloudServer", "Closing");
                    }
                }
            }
        } catch (IOException e) {
            server_logger.logError("Error starting Edge server: " + e.getMessage(), "CloudServer", "Initialization");
        }
    }

}





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
