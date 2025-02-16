package comfaas;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class TheAlgoInstanceConnections {
    // Map that holds IP addresses as keys and their corresponding Socket connections.
    private final Map<String, Socket> connections;

    public TheAlgoInstanceConnections() {
        this.connections = new HashMap<>();
    }

    /**
     * Creates a new connection to the specified IP on port 12463 and stores it in the map.
     *
     * @param ip the IP address to connect to.
     * @throws IOException if an I/O error occurs when creating the socket.
     */
    public void newIP(String ip) throws IOException {
        // Check if a connection already exists for this IP.
        if (connections.containsKey(ip)) {
            System.out.println("Already connected to " + ip);
            return;
        }
        Socket socket = new Socket(ip, 12463);
        connections.put(ip, socket);
        System.out.println("Connected to " + ip + " on port 12463. Total connections: " + connections.size());
    }

    /**
     * Closes the connection to the specified IP and removes it from the map.
     *
     * @param ip the IP address whose connection should be closed.
     */
    public void closeSocket(String ip) {
        Socket socket = connections.get(ip);
        if (socket != null) {
            try {
                socket.close();
                System.out.println("Closed connection to " + ip);
            } catch (IOException e) {
                System.err.println("Error closing connection to " + ip + ": " + e.getMessage());
            } finally {
                connections.remove(ip);
            }
        } else {
            System.out.println("No connection found for " + ip);
        }
    }

    /**
     * Closes all socket connections and clears the map.
     */
    public void closeAllSockets() {
        for (Map.Entry<String, Socket> entry : connections.entrySet()) {
            String ip = entry.getKey();
            Socket socket = entry.getValue();
            try {
                socket.close();
                System.out.println("Closed connection to " + ip);
            } catch (IOException e) {
                System.err.println("Error closing connection to " + ip + ": " + e.getMessage());
            }
        }
        connections.clear();
        System.out.println("All connections closed.");
    }

    // Example usage:
    public static void main(String[] args) {
        TheAlgoInstanceConnections manager = new TheAlgoInstanceConnections();

        try {
            // Create connections to two example IPs.
            manager.newIP("192.168.1.100");
            manager.newIP("192.168.1.101");
        } catch (IOException e) {
            System.err.println("Error creating connection: " + e.getMessage());
        }

        // Close a specific connection.
        manager.closeSocket("192.168.1.100");

        // Close all remaining connections.
        manager.closeAllSockets();
    }
}
