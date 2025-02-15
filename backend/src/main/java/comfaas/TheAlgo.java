package comfaas;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

public class TheAlgo extends AbstractAlgo {
    // Executor service for any threads this class might use.
    private ExecutorService executor;
    // A dummy socket field to simulate an open socket.
    // private Socket socket;
    // Random instance for selecting a random IP.
    private Random random;

    /**
     * Constructor.
     *
     * @param IPs  An array of IP addresses.
     * @param node Either "cloud" or "edge".
     */
    public TheAlgo(String node) {
        super(node);
        this.random = new Random();
        // For demonstration, we initialize an executor with a fixed thread pool.
        this.executor = Executors.newFixedThreadPool(2);
        // Initialize socket as null (or create one if needed).
        // this.socket = null;
    }

    /**
     * Reads "ip.json" from $SERVER_DIR/Output and updates ipDictionary
     * with any new IP addresses.
     */
    @Override
    protected void ipUpdateImpl(String IP) {
        String serverDir = System.getenv("SERVER_DIR");
        if (serverDir == null) {
            System.err.println("SERVER_DIR environment variable not set.");
            return;
        }

        if (!ipDictionary.containsKey(IP)) {
            ipDictionary.put(IP, IP);
            System.out.println("IP dictionary updated with new entry: " + IP);
        } else {
            System.out.println("IP address already exists in the dictionary: " + IP);
        }
    }

    /**
     * Reads "bench.json" from $SERVER_DIR/Output and updates faasDictionary
     * with any new FaaS entries.
     */
    @Override
    public void faasUpdate(String fileName, double CPU, double RAM, double fileSize) {
        String serverDir = Main.rootDir.resolve("server").toString();
        if (serverDir == null) {
            System.err.println("SERVER_DIR environment variable not set.");
            return;
        }
        String benchFilePath = serverDir + "/Output/bench.json";
        try {
            String content = new String(Files.readAllBytes(Paths.get(benchFilePath)), StandardCharsets.UTF_8);
            // Assume bench.json is a JSON object where keys are FaaS file names.
            JSONObject jsonObject = new JSONObject(content);
            for (String key : jsonObject.keySet()) {
                if (!faasDictionary.containsKey(key)) {
                    // Here, we simply store the associated value as a String.
                    faasDictionary.put(key, jsonObject.getString(key));
                }
            }
            System.out.println("FaaS dictionary updated with new entries from bench.json.");
        } catch (IOException e) {
            System.err.println("Error reading bench.json: " + e.getMessage());
        } catch (JSONException e) {
            System.err.println("Error parsing bench.json: " + e.getMessage());
        }
    }

    public void faasUpdate(String fileName, double CPU, double RAM, double fileSize, String[] additonalArgs) {
        faasUpdate( fileName, CPU, RAM, fileSize) ;
    }


    /**
     * Looks up the given FaaS file name in the FaaS dictionary, validates that it
     * exists,
     * and returns a random IP address from the IP dictionary.
     *
     * @param faasFileName The file name used for lookup in the FaaS dictionary.
     * @return A random IP address from ipDictionary.
     * @throws IllegalArgumentException if the faasFileName is not in the
     *                                  faasDictionary.
     * @throws IllegalStateException    if the ipDictionary is empty.
     */
    @Override
    public String get(String faasFileName) {
        if (!faasDictionary.containsKey(faasFileName)) {
            throw new IllegalArgumentException("FaaS file name not found: " + faasFileName);
        }
        if (ipDictionary.isEmpty()) {
            throw new IllegalStateException("No IP addresses available.");
        }
        // Retrieve the IP addresses from the dictionary keys.
        List<String> ipList = new ArrayList<>(ipDictionary.keySet());
        int randomIndex = random.nextInt(ipList.size());
        String selectedIP = ipList.get(randomIndex);
        System.out.println("Returning IP: " + selectedIP + " for FaaS file: " + faasFileName);
        return selectedIP;
    }

    /**
     * Closes any internal threads and sockets used by this class.
     */
    @Override
    public void close() {
        // Shutdown the executor service.
        if (executor != null) {
            executor.shutdownNow();
        }
        // Close the socket if it was opened./
        // if (socket != null) {/
        //     try {
        //         socket.close();
        //     } catch (IOException e) {
        //         System.err.println("Error closing socket: " + e.getMessage());
        //     }
        // }
        System.out.println("TheAlgo closed successfully.");
    }
}
