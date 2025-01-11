// EdgeServer.java

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
// * The EdgeServer class represents an edge node acting as a server
// * for local clients (or any external client), receiving requests and
// * handling them with CoreOperations (file transfers, etc.).
// ------------------------------------------
public class EdgeServer extends CoreOperations {
    // Logging
    private static final Logger logger = new Logger(Main.LogFile);

    // A static flag to indicate that we are shutting down
    private static volatile boolean shuttingDown = false;

    // The thread pool. We'll create it in run(), store it in a static so we can shut it down if needed.
    private static ExecutorService executor = null;

    // ------------------------------------------
    // Default constructor, if needed.
    // ------------------------------------------
public EdgeServer() {
    logger.logEvent(LogLevel.INFO, "EdgeServer", "Initialization", 
        "EdgeServer instance created (no client yet).", 0, -1);
}


    // ------------------------------------------
    // Constructor that accepts client socket and initializes dis/dos.
    // ------------------------------------------
    public EdgeServer(Socket clientSocket) throws IOException {
        logger.newline();
        logger.logEvent(LogLevel.NETWORK, "EdgeServer", "Connection", 
            "Handling an incoming client socket...", 0, -1);
        this.socket = clientSocket;
        this.dis = new DataInputStream(clientSocket.getInputStream());
        this.dos = new DataOutputStream(clientSocket.getOutputStream());
        logger.logEvent(LogLevel.SUCCESS, "EdgeServer", "Connection", 
            "Edge Connection established with client.", 0, -1);
    }
    
    


    // ------------------------------------------
    // Starts a server on the Edge side to accept connections from a "real" client.
    // ------------------------------------------
    public static void run(int port) throws IOException, InterruptedException {
        logger.logEvent(LogLevel.INFO, "EdgeServer", "Startup",
            "Listening on port " + port, 0, -1);

        int maxThreads = Main.maxThreads > 0 ? Main.maxThreads : 10;
        executor = Executors.newFixedThreadPool(maxThreads);

        try (ServerSocket edgeSocket = new ServerSocket(port)) {
            edgeSocket.setSoTimeout(2000);

            while (!shuttingDown) {
                try {
                    Socket clientSocket = edgeSocket.accept();
                    EdgeServer newServer = new EdgeServer(clientSocket);

                    executor.submit(() -> {
                        try {
                            newServer.manageRequests();
                            logger.logEvent(LogLevel.SUCCESS, "EdgeServer", "RequestHandling",
                                "Request handled successfully.", 0, -1);
                        } catch (IOException | InterruptedException e) {
                            // ignore
                        } finally {
                            try {
                                newServer.close(true);
                            } catch (IOException e) {
                                logger.logEvent(LogLevel.ERROR, "EdgeServer", "close()",
                                    "Error closing resources: " + e.getMessage(), 0, -1);
                            }
                        }
                    });
                } catch (SocketTimeoutException ignored) {
                } catch (IOException e) {
                    if (!shuttingDown) {
                        logger.logEvent(LogLevel.ERROR, "EdgeServer", "accept()",
                            "Error accepting client: " + e.getMessage(), 0, -1);
                    }
                }
            }
        } finally {
            // Graceful shutdown
            executor.shutdown();
            int timeoutSec = Main.shutdownTimeout > 0 ? Main.shutdownTimeout : 30;
            if (!executor.awaitTermination(timeoutSec, TimeUnit.SECONDS)) {
                logger.logEvent(LogLevel.WARNING, "EdgeServer", "Shutdown",
                    "Forcing shutdownNow after waiting " + timeoutSec + "s", 0, -1);
                executor.shutdownNow();
            }

            logger.logEvent(LogLevel.SUCCESS, "EdgeServer", "Shutdown",
                "Edge server shut down gracefully.", 0, -1);
        }
    }

    public static void setShutdownFlag() {
        shuttingDown = true;
        logger.logEvent(LogLevel.INFO, "EdgeServer", "ShutdownFlag",
            "Set shuttingDown=true; server will close after finishing tasks.", 0, -1);
    }
}