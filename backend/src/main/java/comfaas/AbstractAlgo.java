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
    public AbstractAlgo(String node) {
        this.node = node;
        this.ipDictionary = new HashMap<>();
        // for (String ip : IPs) {
        //     // Storing each IP in the dictionary. Adjust the key/value as needed.
        //     ipDictionary.put(ip, ip);
        // }
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
    public final synchronized void ipUpdate(String IP) {
        ipUpdateImpl(IP) ;
    }
    // implement the core logic of ipUpdate
    protected abstract void ipUpdateImpl(String IP) ;


    /**
     * Reads "bench.json" from $SERVER_DIR/Output and updates faasDictionary
     * with any new FaaS entries.
     */
    public abstract void faasUpdate(String fileName, double CPU, double RAM, double fileSize);

    /**
     * This is a future proof method to incorperate more information that comfaas can provide. 
     * @param fileName
     * @param CPU
     * @param RAM
     * @param fileSize
     * @param additonalArgs
     */
    public abstract void faasUpdate(String fileName, double CPU, double RAM, double fileSize, String[] additonalArgs);

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
