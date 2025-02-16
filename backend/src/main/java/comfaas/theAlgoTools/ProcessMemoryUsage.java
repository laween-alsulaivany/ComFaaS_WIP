package comfaas.theAlgoTools;

public class ProcessMemoryUsage {
    /**
     * Returns the memory currently used by the JVM (in MB).
     */
    public static double getUsedMemoryMB() {
        long usedBytes = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        return usedBytes / (1024.0 * 1024.0);
    }
}
