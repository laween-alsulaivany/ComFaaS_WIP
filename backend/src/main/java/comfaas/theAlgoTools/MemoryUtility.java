package comfaas.theAlgoTools;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;

public class MemoryUtility {

    /**
     * Returns the amount of free (available) physical memory in megabytes.
     *
     * @return free memory in MB.
     */
    public static double getFreeMemoryInMB() {
        SystemInfo systemInfo = new SystemInfo();
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        long freeBytes = memory.getAvailable();
        return freeBytes / (1024.0 * 1024.0);
    }

    public static void main(String[] args) {
        double freeMemoryMB = getFreeMemoryInMB();
        System.out.printf("Free memory: %.2f MB%n", freeMemoryMB);
    }
}
