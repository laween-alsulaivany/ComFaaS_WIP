package comfaas.theAlgoTools;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class ProcessCpuUsage {
    /**
     * Returns the CPU load of the current process as a percentage.
     * The value is between 0 and 100.
     */
    public static double getProcessCpuLoadPercentage() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double load = osBean.getProcessCpuLoad(); // value between 0.0 and 1.0, or -1 if not available
        if (load < 0) {
            return 0;
        }
        return load * 100;
    }
}
