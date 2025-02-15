// Server.java

package comfaas;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import comfaas.Logger.LogLevel;

// ------------------------------------------
// * The Server class extends CoreOperations and handles
// * inbound socket operations for the Cloud. It listens for client connections
// * and manages requests.
// ------------------------------------------
public class Server extends CoreOperations {
    // Logging
    private static final Logger logger = new Logger(Main.LogFile);

    // A static flag to indicate that we are shutting down
    private static volatile boolean shuttingDown = false;

    // Registry of edges using composite key: "ip:port"
    private static final ConcurrentHashMap<String, EdgeInfo> edgeRegistry = new ConcurrentHashMap<>();
    private static int nextEdgeId = 1;

    // The thread pool. We'll create it in run() and store it here so we can shut it
    // down if needed.
    private static ExecutorService executor = null;
    private static int shutdownTimeoutSec = Main.shutdownTimeout > 0 ? Main.shutdownTimeout : 30;

    private AbstractAlgo algo;

    // ------------------------------------------
    // Default constructor, if needed.
    // ------------------------------------------
    public Server() {
        logger.logEvent(LogLevel.INFO, "Server", "Initialization",
                "Server instance created (no client yet).", 0, -1);
    }

    // ------------------------------------------
    // Constructor that accepts client socket and initializes dis/dos.
    // ------------------------------------------
    public Server(Socket clientSocket) throws IOException {
        logger.newline();
        logger.network("Server", "Connection", "Handling an incoming client socket...");
        this.socket = clientSocket;
        this.dis = new DataInputStream(clientSocket.getInputStream());
        this.dos = new DataOutputStream(clientSocket.getOutputStream());
        logger.logEvent(LogLevel.SUCCESS, "Server", "Connection",
                "Server Connection established with client.", 0, -1);
        String[] initialIPs = getUniqueIPs();
        System.err.println("Initial IPs: " + Arrays.toString(initialIPs));
        this.algo = new TheAlgo(initialIPs, Main.serverType);
        System.err.println("Algo: " + algo);
        this.algo.ipUpdate();
        System.err.println("IpUpdate has been called");
        this.algo.faasUpdate();
        System.err.println("FaasUpdate has been called");
    }

    /**
     * Updated storeEdgeInfo: Uses composite key "ip:port" to ensure uniqueness.
     * If an entry for the same IP and port exists, updates it (preserving the edge
     * ID);
     * otherwise assigns a new edge ID.
     */
    public static synchronized void storeEdgeInfo(EdgeInfo info) {
        String key = info.getIp() + ":" + info.getPort();
        if (edgeRegistry.containsKey(key)) {
            EdgeInfo existing = edgeRegistry.get(key);
            // Preserve the existing edge ID and update the record with the latest
            // heartbeat.
            info.setEdgeId(existing.getEdgeId());
            edgeRegistry.put(key, info);
        } else {
            info.setEdgeId(nextEdgeId++);
            edgeRegistry.put(key, info);
        }
    }

    // ------------------------------------------
    // Starts a server on the Cloud side to accept connections from a remote client.
    // ------------------------------------------
    public static void run(int port) throws IOException, InterruptedException {

        int maxThreads = Main.maxThreads > 0 ? Main.maxThreads : 10;

        logger.logEvent(LogLevel.INFO, "Server", "Startup",
                "Listening on port " + port + " with maxThreads=" + maxThreads, 0, -1);

        // Create a thread pool (store in static var so we can shut it down later)
        executor = Executors.newFixedThreadPool(maxThreads);

        // +++ Only if we are a cloud server, start discovering edges +++
        if ("cloud".equalsIgnoreCase(Main.serverType)) {
            logger.logEvent(LogLevel.INFO, "Server", "Startup",
                    "Server type is CLOUD; starting EdgeDiscovery thread...", 0, -1);

            // EdgeDiscovery discovery = new EdgeDiscovery();
            // Thread discoveryThread = new Thread(discovery, "EdgeDiscoveryThread");
            // discoveryThread.start();
        } else if ("edge".equalsIgnoreCase(Main.serverType)) {
            // Edge = Start EdgeResponder
            logger.logEvent(LogLevel.INFO, "Server", "Startup",
                    "Server type is EDGE; starting EdgeResponder thread...", 0, -1);

            // Suppose we read from arguments or default:
            String cloudIP = "127.0.0.1"; // fallback
            int cloudPort = 12353; // fallback

            // find them in `args` if you want e.g. -cloudIP <ip> -cloudPort <port>
            // (You can parse them just like you do -p <port>)
            for (int i = 2; i < Main.cliArgs.length; i++) {
                switch (Main.cliArgs[i]) {
                    case "-cloudIP" -> {
                        if (i + 1 < Main.cliArgs.length) {
                            cloudIP = Main.cliArgs[++i];
                        }
                    }
                    case "-cloudPort" -> {
                        if (i + 1 < Main.cliArgs.length) {
                            cloudPort = Integer.parseInt(Main.cliArgs[++i]);
                        }
                    }
                }
            }

            // Create a unique ID for this edge, or read from config
            // String edgeId = "edge-" + port; // a simple unique ID
            // int edgeId = 22; // a simple unique ID
            EdgeHeartbeat hb = new EdgeHeartbeat(cloudIP, cloudPort, nextEdgeId, port);
            Thread hbThread = new Thread(hb, "EdgeHeartbeatThread");
            hbThread.start();

            // old way
            // EdgeResponder responder = new EdgeResponder();
            // Thread responderThread = new Thread(responder, "EdgeResponderThread");
            // responderThread.start();
        }

        int acceptTimeoutMs = 2000; // poll every 2 seconds
        try (ServerSocket ServerSocket = new ServerSocket(port)) {
            ServerSocket.setSoTimeout(acceptTimeoutMs);

            // Keep accepting while not shutting down
            while (!shuttingDown) {
                try {
                    // accept will block up to acceptTimeoutMs
                    Socket clientSocket = ServerSocket.accept();

                    logger.logEvent(LogLevel.INFO, "Server", "Connection",
                            "Accepted client connection.", 0, -1);

                    // Build the new cloudServer object from that socket
                    Server newServer = new Server(clientSocket);

                    // Handle the client request in a separate thread
                    executor.submit(() -> {
                        try {
                            newServer.manageRequests();
                            logger.logEvent(LogLevel.SUCCESS, "Server", "RequestHandling",
                                    "Request handled successfully.", 0, -1);
                        } catch (IOException | InterruptedException e) {
                            // ignore

                        } finally {
                            try {
                                newServer.close(true);
                            } catch (IOException e) {
                                logger.logEvent(LogLevel.ERROR, "Server", "close()",
                                        "Error closing resources: " + e.getMessage(), 0, -1);
                            }
                        }
                    });

                } catch (SocketTimeoutException ste) {
                    // ignore
                } catch (IOException e) {
                    // If we're shutting down, we might ignore
                    if (!shuttingDown) {
                        logger.logEvent(LogLevel.ERROR, "Server", "accept()",
                                "Error accepting client connection: " + e.getMessage(), 0, -1);
                    }
                }
            }
        } catch (IOException e) {
            logger.logEvent(LogLevel.ERROR, "Server", "Initialization",
                    "Error starting server: " + e.getMessage(), 0, -1);
        } finally {
            // Once we exit the while loop, we want to gracefully shut down the thread pool
            logger.logEvent(LogLevel.INFO, "Server", "Shutdown",
                    "Shutting down thread pool...", 0, -1);

            executor.shutdown();
            if (!executor.awaitTermination(shutdownTimeoutSec, TimeUnit.SECONDS)) {
                logger.logEvent(LogLevel.WARNING, "Server", "Shutdown",
                        "Forcing shutdownNow after waiting " + shutdownTimeoutSec + "s", 0, -1);
                executor.shutdownNow();

            }

            logger.logEvent(LogLevel.SUCCESS, "Server", "Shutdown",
                    "Server has shut down gracefully.", 0, -1);
        }
    }

    // +++ Optionally you can add a cleaner method to remove old edges +++
    public static void removeStaleEdges(long maxAgeMs) {
        long now = System.currentTimeMillis();
        edgeRegistry.forEach((edgeId, edgeInfo) -> {
            if ((now - edgeInfo.getLastHeartbeatTimestamp()) > maxAgeMs) {
                edgeRegistry.remove(edgeId);
            }
        });
    }

    // TODO: Implement this later
    // Called by manageRequests() if we see "shutdownServer"
    public static void setShutdownFlag() {
        shuttingDown = true;
        logger.logEvent(LogLevel.INFO, "Server", "ShutdownFlag",
                "Set shuttingDown=true; server will close after finishing tasks.", 0, -1);
    }

    // TODO: Implement this later
    public static boolean isShuttingDown() {
        return shuttingDown;

        // Implement the logic to determine if the server is shutting down
        // return false; // Placeholder implementation
    }

    public static String[] getUniqueIPs() {
        Set<String> uniqueIPs = new HashSet<>();

        // Iterate over the edgeRegistry and collect unique IP addresses
        for (EdgeInfo edge : edgeRegistry.values()) {
            uniqueIPs.add(edge.getIp());
        }

        // Convert to String array
        return uniqueIPs.toArray(new String[0]);
    }
}