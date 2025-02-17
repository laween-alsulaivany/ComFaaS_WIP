package comfaas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestLogger {
    private static final String LOG_FILE = "request_log.txt";
    private static final String ID_FILE = "request_id.txt";
    private static AtomicInteger requestIdCounter = new AtomicInteger(1);

    static {
        // Load the last used ID from file
        requestIdCounter.set(loadLastRequestId());
    }

    public static void logRequest(String requestName, boolean isStart) {
        int requestId = requestIdCounter.get();
        String timestamp = getCurrentTimestamp();
        String eventType = isStart ? "START" : "END";

        String logEntry = String.format("ID: %d | Name: %s | %s | Time: %s%n",
                requestId, requestName, eventType, timestamp);

        writeToFile(LOG_FILE, logEntry);

        // Increment ID only when logging a start event
        // if (isStart) {
        requestIdCounter.incrementAndGet();
        saveLastRequestId(requestIdCounter.get());
        // }
    }

    private static String getCurrentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    private static void writeToFile(String fileName, String content) {
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write(content);
        } catch (IOException e) {
            System.err.println("Error writing log: " + e.getMessage());
        }
    }

    private static int loadLastRequestId() {
        File file = new File(ID_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                return Integer.parseInt(reader.readLine().trim());
            } catch (IOException | NumberFormatException e) {
                System.err.println("Error reading last request ID: " + e.getMessage());
            }
        }
        return 1; // Default to 1 if no previous ID is found
    }

    private static void saveLastRequestId(int requestId) {
        try (FileWriter writer = new FileWriter(ID_FILE, false)) {
            writer.write(String.valueOf(requestId));
        } catch (IOException e) {
            System.err.println("Error saving last request ID: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java RequestLogger <RequestName> <start|end>");
            return;
        }

        String requestName = args[0];
        boolean isStart = args[1].equalsIgnoreCase("start");
        logRequest(requestName, isStart);
    }
}
