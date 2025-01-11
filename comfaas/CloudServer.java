// CloudServer.java

package comfaas;

import comfaas.Logger.LogLevel;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;



// ------------------------------------------
// * The Server (or CloudServer) class extends CoreOperations and handles
// * inbound socket operations for the Cloud. It listens for client connections
// * (which might be EdgeClient or any other client) and manages requests.
// ------------------------------------------
public class CloudServer extends CoreOperations {
    // Logging
    private static final Logger logger = new Logger(Main.LogFile);

    // A static flag to indicate that we are shutting down
    private static volatile boolean shuttingDown = false;

    // The thread pool. We'll create it in run(), store it in a static so we can shut it down if needed.
    private static ExecutorService executor = null;
    private static int shutdownTimeoutSec = Main.shutdownTimeout > 0 ? Main.shutdownTimeout : 30;


    // ------------------------------------------
    // Default constructor, if needed.
    // ------------------------------------------
    public CloudServer() {
        logger.logEvent(LogLevel.INFO, "CloudServer", "Initialization", 
            "CloudServer instance created (no client yet).", 0, -1);
    }
    

    // ------------------------------------------
    // Constructor that accepts client socket and initializes dis/dos.
    // ------------------------------------------
public CloudServer(Socket clientSocket) throws IOException {
    logger.newline();
    logger.network("CloudServer", "Connection", "Handling an incoming client socket...");
    this.socket = clientSocket;
    this.dis = new DataInputStream(clientSocket.getInputStream());
    this.dos = new DataOutputStream(clientSocket.getOutputStream());
    logger.logEvent(LogLevel.SUCCESS, "CloudServer", "Connection", 
    "Cloud Connection established with client.", 0, -1);
}

    // ------------------------------------------
    // Starts a server on the Cloud side to accept connections from a "real" client.
    // ------------------------------------------
    public static void run(int port) throws IOException, InterruptedException {


        int maxThreads = Main.maxThreads > 0 ? Main.maxThreads : 10;

        logger.logEvent(LogLevel.INFO, "CloudServer", "Startup",
            "Listening on port " + port + " with maxThreads=" + maxThreads, 0, -1);

        // Create a thread pool (store in static var so we can shut it down later)
        executor = Executors.newFixedThreadPool(maxThreads);

        int acceptTimeoutMs = 2000; // poll every 2 seconds
        try (ServerSocket cloudServerSocket = new ServerSocket(port)) {
            cloudServerSocket.setSoTimeout(acceptTimeoutMs);

            // Keep accepting while not shutting down
            while (!shuttingDown) {
                try {
                    // accept will block up to acceptTimeoutMs
                    Socket clientSocket = cloudServerSocket.accept();

                    logger.logEvent(LogLevel.INFO, "CloudServer", "Connection",
                        "Accepted client connection.", 0, -1);

                    // Build the new cloudServer object from that socket
                    CloudServer newServer = new CloudServer(clientSocket);

                    // Handle the client request in a separate thread
                    executor.submit(() -> {
                        try {
                            newServer.manageRequests();
                            logger.logEvent(LogLevel.SUCCESS, "CloudServer", "RequestHandling",
                                "Request handled successfully.", 0, -1);
                        } catch (IOException | InterruptedException e) {
                            // ignore
   
                        } finally {
                            try {
                                newServer.close(true);
                            } catch (IOException e) {
                                logger.logEvent(LogLevel.ERROR, "CloudServer", "close()",
                                    "Error closing resources: " + e.getMessage(), 0, -1);
                            }
                        }
                    });

                } catch (SocketTimeoutException ste) {
                    // ignore
                } catch (IOException e) {
                    // If we're shutting down, we might ignore
                    if (!shuttingDown) {
                        logger.logEvent(LogLevel.ERROR, "CloudServer", "accept()",
                            "Error accepting client connection: " + e.getMessage(), 0, -1);
                    }
                }
            }
        } catch (IOException e) {
            logger.logEvent(LogLevel.ERROR, "CloudServer", "Initialization",
                "Error starting Cloud server: " + e.getMessage(), 0, -1);
        } finally {
            // Once we exit the while loop, we want to gracefully shut down the thread pool
            logger.logEvent(LogLevel.INFO, "CloudServer", "Shutdown", 
                "Shutting down thread pool...", 0, -1);

            executor.shutdown();
            if (!executor.awaitTermination(shutdownTimeoutSec, TimeUnit.SECONDS)) {
                logger.logEvent(LogLevel.WARNING, "CloudServer", "Shutdown",
                    "Forcing shutdownNow after waiting " + shutdownTimeoutSec + "s", 0, -1);
                executor.shutdownNow();

            }

            logger.logEvent(LogLevel.SUCCESS, "CloudServer", "Shutdown",
                "Cloud server has shut down gracefully.", 0, -1);
        }
    }

    // Called by manageRequests() if we see "shutdownServer"
    public static void setShutdownFlag() {
        shuttingDown = true;
        logger.logEvent(LogLevel.INFO, "CloudServer", "ShutdownFlag",
            "Set shuttingDown=true; server will close after finishing tasks.", 0, -1);
    }
}