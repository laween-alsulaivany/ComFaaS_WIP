package comfaas.theAlgoTools;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class CpuAvailability {

    /**
     * Returns an approximation of available CPU "units" by calculating the idle time fraction.
     * It measures the system CPU load between two ticks (separated by a short delay)
     * and then computes the available CPU units as: 
     *   availableCpu = cores * (1 - cpuLoad)
     *
     * @return available CPU units.
     */
    public static double getCpuAvailability() {
        SystemInfo systemInfo = new SystemInfo();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        int cores = processor.getLogicalProcessorCount();

        // Capture CPU ticks at the first time point.
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        
        // Wait a short period to compute an average load.
        try {
            Thread.sleep(1000); // 1 second delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        }
        
        // Capture CPU ticks at the second time point.
        long[] ticks = processor.getSystemCpuLoadTicks();
        // Calculate CPU load between ticks. This returns a value between 0.0 and 1.0.
        double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks);
        
        // Calculate available CPU units.
        double availableCpu = cores * (1 - cpuLoad);
        return availableCpu;
    }

    public static void main(String[] args) {
        double availableCpu = getCpuAvailability();
        if (availableCpu >= 0) {
            System.out.printf("Available CPU units: %.2f%n", availableCpu);
        } else {
            System.out.println("Could not determine CPU availability.");
        }
    }
}
