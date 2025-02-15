package comfaas;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractAlgo {
    // Instance dictionaries for IP addresses and FaaS entries.
    protected Map<String, String> ipDictionary;
    protected Map<String, String> faasDictionary;

    // Node type: should be either "cloud" or "edge".
    protected String node;

    /**
     * Constructor.
     * 
     * @param IPs  An array of IP addresses to initialize the IP dictionary.
     * @param node Either "cloud" or "edge".
     */
    public AbstractAlgo(String[] IPs, String node) {
        this.node = node;
        this.ipDictionary = new HashMap<>();
        for (String ip : IPs) {
            // Storing each IP in the dictionary. Adjust the key/value as needed.
            ipDictionary.put(ip, ip);
        }
        // Initialize the FaaS dictionary.
        this.faasDictionary = new HashMap<>();
        System.out.println("AbstractAlgo constructor: " + node);
        System.out.println("IPs: " + ipDictionary);
        System.out.println("FaaS: " + faasDictionary);
    }

    /**
     * Reads "ip.json" from $SERVER_DIR/Output and updates ipDictionary
     * with any new IP addresses.
     */
    public abstract void ipUpdate();

    /**
     * Reads "bench.json" from $SERVER_DIR/Output and updates faasDictionary
     * with any new FaaS entries.
     */
    public abstract void faasUpdate();

    /**
     * Looks up the provided file name in the FaaS dictionary, validates its
     * existence,
     * and returns a random IP address from ipDictionary.
     *
     * @param faasFileName The file name used to look up in the FaaS dictionary.
     * @return A random IP address from the IP dictionary.
     */
    public abstract String get(String faasFileName);

    /**
     * Closes any threads and sockets that may be running.
     */
    public abstract void close();
}
