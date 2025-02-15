// EdgeHeartbeat.java

package comfaas;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import comfaas.Logger.LogLevel;

/**
 * A simple thread that, if role=EDGE, periodically connects to the Cloud
 * and sends "heartbeat" JSON, then closes the connection.
 */
public class EdgeHeartbeat implements Runnable {

    private static final Logger logger = new Logger(Main.getLogFile());
    private final String cloudIP;
    private final int cloudPort;
    public int edgeId;
    private final int localPort; // the port this edge server is using
    // int counter = 0;

    private volatile boolean running = true;

    public EdgeHeartbeat(String cloudIP, int cloudPort, int edgeId, int localPort) {
        this.cloudIP = cloudIP;
        this.cloudPort = cloudPort;
        this.edgeId = edgeId;
        this.localPort = localPort;
    }

    @Override
    public void run() {
        while (running && !Server.isShuttingDown()) {
            Socket socket = null;
            DataOutputStream dos = null;
            DataInputStream dis = null;
            try {
                // Open a new socket for each heartbeat iteration
                socket = new Socket(cloudIP, cloudPort);
                dos = new DataOutputStream(socket.getOutputStream());
                dis = new DataInputStream(socket.getInputStream());

                // Send "heartbeat" command
                dos.writeUTF("heartbeat");

                // Build JSON
                String currentTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
                String localIp = socket.getLocalAddress().getHostAddress();

                String nodeType = "edge";
                String jsonString = """
                        {
                          "node": "%s",
                          "edge_id": "%d",
                          "ip": "%s",
                          "port": %d,
                          "last_heartbeat": "%s"
                        }
                        """.formatted(nodeType, edgeId, localIp, localPort, currentTime);
                byte[] jsonBytes = jsonString.getBytes();
                dos.writeInt(jsonBytes.length);
                dos.write(jsonBytes);
                dos.flush();

                // Read response from the server (expecting "OK")
                String response;
                try {
                    response = dis.readUTF();
                } catch (java.io.EOFException eof) {
                    // Server closed the connection after response; treat as "OK"
                    response = "OK";
                }
                logger.logEvent(LogLevel.INFO, "EdgeHeartbeat", "run",
                        "Sent heartbeat, got response: " + response, 0, -1);
            } catch (IOException e) {
                logger.logEvent(LogLevel.ERROR, "EdgeHeartbeat", "run",
                        "Error in long-lived heartbeat: " + e.toString(), 0, -1);
            } finally {
                if (dos != null) {
                    try {
                        dos.close();
                    } catch (IOException ignored) {
                    }
                }
                if (dis != null) {
                    try {
                        dis.close();
                    } catch (IOException ignored) {
                    }
                }
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException ignored) {
                    }
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                running = false;
            }
        }
        logger.logEvent(LogLevel.INFO, "EdgeHeartbeat", "run",
                "EdgeHeartbeat (long-lived) thread exiting.", 0, -1);
    }

    public void stopHeartbeat() {
        running = false;
    }
}
