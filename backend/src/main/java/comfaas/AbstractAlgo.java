package comfaas ;

import java.util.Map;
import java.util.HashMap;

public abstract class AbstractAlgo {
    // Instance dictionaries for IP addresses and FaaS entries.
    protected Map<String, String> ipDictionary;
    
    protected Map<String, double[]> faasDictionary;
    
    // Node type: should be either "cloud" or "edge".
    protected String node;

    /**
     * This needs to be called before connections are established. 
     * 
     * @param node Either "cloud" or "edge".
     */
    public AbstractAlgo(String node) {
        this.node = node;
        this.ipDictionary = new HashMap<>();
        // Initialize the FaaS dictionary.
        this.faasDictionary = new HashMap<>();
    }

    /**
     * Reads "ip.json" from $SERVER_DIR/Output and updates ipDictionary
     * with any new IP addresses.
     */
    public final synchronized void ipUpdate() {
        ipUpdateImpl() ;
    }
    // implement the core logic of ipUpdate
    protected abstract void ipUpdateImpl() ;


    /**
     * Reads "bench.json" from $SERVER_DIR/Output and updates faasDictionary
     * with any new FaaS entries.
     */
    public abstract void faasUpdate();

    /**
     * Looks up the provided file name in the FaaS dictionary, validates its existence,
     * and returns a random IP address from ipDictionary.
     *
     * @param faasFileName The file name used to look up in the FaaS dictionary.
     * c
     * @return A random IP address from the IP dictionary.
     */
    public abstract String get(String faasFileName, int np);

    /**
     * Closes any threads and sockets that may be running.
     */
    public abstract void close();
}
