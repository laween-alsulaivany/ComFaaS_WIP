package comfaas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TheAlgoInstanceServer {
    private int port;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;

    public TheAlgoInstanceServer(int port) {
        this.port = port;
        // Using a cached thread pool to handle an arbitrary number of clients.
        this.threadPool = Executors.newCachedThreadPool();
    }

    /**
     * Starts the server and listens for client connections.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            // Main server loop: accept connections until the server socket is closed.
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getRemoteSocketAddress());
                // Handle the client in its own thread.
                threadPool.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            if (serverSocket != null && serverSocket.isClosed()) {
                System.out.println("Server socket closed.");
            } else {
                e.printStackTrace();
            }
        } finally {
            stop();
        }
    }

    /**
     * Stops the server by closing the server socket and shutting down the thread pool.
     */
    public void stop() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (threadPool != null && !threadPool.isShutdown()) {
                threadPool.shutdownNow();
            }
            System.out.println("Server stopped.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ClientHandler handles the client connection in a separate thread.
     */
    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()))) {
                String line;
                // Read from the client until the stream is closed (client disconnects).
                while ((line = in.readLine()) != null) {
                    System.out.println("Received from " + socket.getRemoteSocketAddress() + ": " + line);
                }
                System.out.println("Client " + socket.getRemoteSocketAddress() + " disconnected.");
            } catch (IOException e) {
                System.out.println("Error handling client " + socket.getRemoteSocketAddress());
                e.printStackTrace();
            } finally {
                // Ensure the socket is closed.
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignore exception during socket close.
                }
            }
        }
    }

    // Example usage: start the server on port 12345.
    public static void main(String[] args) {
        int port = 12463;
        TheAlgoInstanceServer server = new TheAlgoInstanceServer(port);

        // Add a shutdown hook to stop the server gracefully when the program is interrupted.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop()));

        server.start();
    }
}
