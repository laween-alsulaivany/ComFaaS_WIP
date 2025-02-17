package comfaas.theAlgoTools;

import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

public class ProcessCpuUsage {
    /**
     * Returns the CPU load of the current process as a percentage.
     */
    public static double getProcessCpuLoadPercentage() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double load = osBean.getProcessCpuLoad();
        return (load < 0) ? 0 : load * 100;
    }

    /**
     * Returns the total CPU time used by the current process in nanoseconds.
     */
    public static long getProcessCpuTimeNanos() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        return osBean.getProcessCpuTime();
    }

    /**
     * Method 1: Calculates average CPU usage using the difference between CPU time
     * and real time.
     * 
     * @param startCpuTimeNanos Process CPU time at start (in nanoseconds)
     * @param endCpuTimeNanos   Process CPU time at end (in nanoseconds)
     * @param elapsedTimeNanos  Elapsed wall-clock time in nanoseconds
     * @param numCores          Number of logical cores available
     * @return Average CPU usage percentage over the period
     */
    public static double averageCpuUsageUsingTime(long startCpuTimeNanos, long endCpuTimeNanos, long elapsedTimeNanos,
            int numCores) {
        long cpuTimeUsed = endCpuTimeNanos - startCpuTimeNanos;
        double avgCpu = (cpuTimeUsed / (double) (elapsedTimeNanos * numCores)) * 100;
        return avgCpu;
    }

    /**
     * Method 2: Samples the instantaneous CPU load every sampleIntervalMillis over
     * durationMillis,
     * then returns the average.
     * 
     * @param durationMillis       Total duration over which to sample (in
     *                             milliseconds)
     * @param sampleIntervalMillis Interval between samples (in milliseconds)
     * @return Average CPU usage percentage over the sampling period
     * @throws InterruptedException if the thread sleep is interrupted
     */
    public static double averageCpuUsageBySampling(long durationMillis, long sampleIntervalMillis)
            throws InterruptedException {
        int samples = (int) (durationMillis / sampleIntervalMillis);
        double sum = 0.0;
        for (int i = 0; i < samples; i++) {
            double sample = getProcessCpuLoadPercentage();
            sum += sample;
            Thread.sleep(sampleIntervalMillis);
        }
        return sum / samples;
    }
}
