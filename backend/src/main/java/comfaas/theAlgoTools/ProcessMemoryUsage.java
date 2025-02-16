package comfaas.theAlgoTools;

public class ProcessMemoryUsage {
    /**
     * Returns the memory currently used by the JVM (in MB).
     */
    public static double getUsedMemoryMB() {
        long usedBytes = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        return usedBytes / (1024.0 * 1024.0);
    }

    /**
     * Samples the memory usage every sampleIntervalMillis over durationMillis and
     * returns the maximum memory used.
     * 
     * @param durationMillis       Total duration over which to sample (in
     *                             milliseconds)
     * @param sampleIntervalMillis Interval between samples (in milliseconds)
     * @return Maximum memory used (in MB) observed during the sampling period
     * @throws InterruptedException if the thread sleep is interrupted
     */
    public static double maxMemoryUsageDuringPeriod(long durationMillis, long sampleIntervalMillis)
            throws InterruptedException {
        int samples = (int) (durationMillis / sampleIntervalMillis);
        double maxMem = 0.0;
        for (int i = 0; i < samples; i++) {
            double current = getUsedMemoryMB();
            if (current > maxMem) {
                maxMem = current;
            }
            Thread.sleep(sampleIntervalMillis);
        }
        return maxMem;
    }
}
