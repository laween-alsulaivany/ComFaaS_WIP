package comfaas.theAlgoTools;
import java.io.IOException;
import java.net.InetAddress;

public class PingUtility {

    /**
     * Pings the specified IP address and returns the time taken in milliseconds as a double.
     * If the IP is not reachable within the given timeout, it returns -1.
     *
     * @param ip the IP address to ping.
     * @param timeoutMillis the timeout in milliseconds.
     * @return the time taken to ping the IP address in milliseconds as a double, or -1 if unreachable.
     */
    public static double ping(String ip, int timeoutMillis) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            long startTime = System.nanoTime();
            if (address.isReachable(timeoutMillis)) {
                long endTime = System.nanoTime();
                // Convert nanoseconds to milliseconds with decimal precision.
                return (endTime - startTime) / 1_000_000.0;
            } else {
                // IP not reachable within the specified timeout.
                return -1;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // Example usage:
    public static void main(String[] args) {
        String ip = "192.168.70.20";  // Replace with your target IP address.
        int timeout = 5000;     // Timeout of 3000 milliseconds (3 seconds).
        double pingTime = ping(ip, timeout);

        if (pingTime >= 0) {
            System.out.printf("Ping time to %s is %.2f ms.%n", ip, pingTime);
        } else {
            System.out.println("Failed to reach " + ip + " within " + timeout + " ms.");
        }
    }
}
