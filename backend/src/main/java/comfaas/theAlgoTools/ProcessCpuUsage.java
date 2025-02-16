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
}
