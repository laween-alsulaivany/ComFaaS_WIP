// EdgeHeartbeat.java

package comfaas;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import comfaas.Logger.LogLevel;

/**
 * A simple thread that, if role=EDGE, periodically connects to the Cloud
 * and sends "heartbeat" JSON, then closes the connection.
 */
public class EdgeHeartbeat implements Runnable {

    private static final Logger logger = new Logger(Main.getLogFile());
    private final String cloudIP;
    private final int cloudPort;
    private final int localPort; // the port this edge server is using
    private int edgeId = 1; // the ID of this edge server
    private volatile boolean running = true;

    public EdgeHeartbeat(String cloudIP, int cloudPort, int edgeId, int localPort) {
        this.cloudIP = cloudIP;
        this.cloudPort = cloudPort;
        this.edgeId += edgeId;
        this.localPort = localPort;
    }

    @Override
    public void run() {
        Socket socket = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;
        try {
            // Open a persistent socket connection
            socket = new Socket(cloudIP, cloudPort);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            logger.logEvent(LogLevel.INFO, "EdgeHeartbeat", "run",
                    "Persistent heartbeat connection established to " + cloudIP + ":" + cloudPort, 0, -1);

            // Keep sending heartbeats periodically over the same connection.
            // while (running && !Server.isShuttingDown()) {
            // Send heartbeat command (if your protocol requires it)
            dos.writeUTF("heartbeat");

            // Send node type and local IP
            String nodeType = "edge";
            String localIp = socket.getLocalAddress().getHostAddress();
            dos.writeUTF(nodeType);
            dos.writeUTF(localIp);
            System.out.println("Sent heartbeat: " + nodeType + " with IP " + localIp);
            dos.flush();
            logger.logEvent(LogLevel.INFO, "EdgeHeartbeat", "run",
                    "Sent heartbeat: " + nodeType + " with IP " + localIp, 0, -1);

            // Optionally, read a response from the cloud (if needed)
            String response;
            try {
                response = dis.readUTF();
                logger.logEvent(LogLevel.INFO, "EdgeHeartbeat", "run",
                        "Received response: " + response, 0, -1);
            } catch (IOException eof) {
                // if the server temporarily doesn’t respond, you can choose to log or ignore it
                logger.logEvent(LogLevel.ERROR, "EdgeHeartbeat", "run",
                        "Error reading response: " + eof.getMessage(), 0, -1);
            }

            // Wait before sending the next heartbeat
            // Thread.sleep(5000);
            // }
        } catch (IOException e) {
            logger.logEvent(LogLevel.ERROR, "EdgeHeartbeat", "run",
                    "Error in persistent heartbeat: " + e.getMessage(), 0, -1);
        } finally {
            try {
                if (dos != null)
                    dos.close();
            } catch (IOException ignored) {
            }
            try {
                if (dis != null)
                    dis.close();
            } catch (IOException ignored) {
            }
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException ignored) {
            }
            logger.logEvent(LogLevel.INFO, "EdgeHeartbeat", "run",
                    "Persistent heartbeat connection closed.", 0, -1);
        }
    }

    public void stopHeartbeat() {
        running = false;
    }
}
