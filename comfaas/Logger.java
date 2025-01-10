package comfaas;

import static comfaas.ANSI_Colors.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.text.SimpleDateFormat;
import java.util.Date; // Import ANSI color codes for console logging

/**
 * Logger class handles two functionalities:
 * 1. Logs detailed entries into a CSV file for tracking tasks and events.
 * 2. Prints color-coded messages to the console for quick debugging or status updates.
 */
public class Logger {
    // File path for the log file where logs are written
    private final String logFilePath;

    /**
     * Constructor to initialize the Logger with a specific log file path.
     * @param logFilePath The path to the log file where entries will be stored.
     */
    public Logger(String logFilePath) {
        this.logFilePath = logFilePath;
        initializeLogFile(); // Ensure the log file is initialized with headers
    }

    /**
     * Ensures the log file exists and adds a header row if the file is empty.
     */
    private void initializeLogFile() {
        File logFile = new File(logFilePath);
        if (!logFile.exists() || logFile.length() == 0) {
            try (FileWriter fw = new FileWriter(logFilePath, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter pw = new PrintWriter(bw)) {
                // Add column headers to the log file
                pw.println("Timestamp,Level,Message,TaskName,Type,Duration(ms),CPU_Usage(%),Memory_Usage(MB),Action,FileSize(Bytes)");
            } catch (IOException e) {
                // Print error message to the console
                System.err.println(RED + "[ERROR] Failed to initialize log file: " + e.getMessage() + RESET);
            }
        }
    }

    /**
     * Logs an event with minimal details to the CSV file.
     * @param level The severity level (INFO, WARNING, ERROR).
     * @param message The message describing the event.
     * @param taskName The name of the task related to the log.
     * @param type The type of the log (e.g., "Operation", "Error").
     * @param duration The duration of the task in milliseconds.
     */
    public synchronized void log(String level, String message, String taskName, String type, long duration) {
        double cpuUsage = getCPUUsage();
        long memoryUsage = getMemoryUsage();
        log(level, message, taskName, type, duration, cpuUsage, memoryUsage, "N/A", -1);
    }

    /**
     * Logs a file-related action (e.g., reading, writing) to the CSV file.
     * @param level The severity level.
     * @param message A message describing the file action.
     * @param fileName The name of the file being operated on.
     * @param action The action performed (e.g., "read", "write").
     * @param duration The duration of the action in milliseconds.
     * @param fileSize The size of the file in bytes.
     */
    public synchronized void logFileAction(String level, String message, String fileName, String action, long duration, long fileSize) {
        double cpuUsage = getCPUUsage();
        long memoryUsage = getMemoryUsage();
        log(level, message, fileName, "FileOperation", duration, cpuUsage, memoryUsage, action, fileSize);
    }

    /**
     * Logs a detailed entry to the CSV file with all parameters.
     * @param level The severity level.
     * @param message The message describing the event.
     * @param taskName The name of the task.
     * @param type The type of log.
     * @param duration The duration of the event.
     * @param cpuUsage CPU usage during the event.
     * @param memoryUsage Memory usage during the event.
     * @param action Additional action description.
     * @param fileSize File size (for file-related operations).
     */
    public synchronized void log(String level, String message, String taskName, String type, long duration, double cpuUsage, long memoryUsage, String action, long fileSize) {
        try (FileWriter fw = new FileWriter(logFilePath, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            // Write the log entry to the CSV file
            pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%.2f,%.2f,\"%s\",%d%n",
                    timestamp, level, message, taskName, type, (double) duration,
                    cpuUsage, memoryUsage / (1024.0 * 1024.0), action, fileSize);
        } catch (IOException e) {
            // Print error message to the console
            System.err.println(RED + "[ERROR] Failed to write log: " + e.getMessage() + RESET);
        }
    }

    /**
     * Logs an error event to the CSV file.
     * @param message The error message.
     * @param taskName The name of the task where the error occurred.
     * @param type The type of error.
     */
    public synchronized void logError(String message, String taskName, String type) {
        log("ERROR", message, taskName, type, -1);
    }

    /**
     * Retrieves the current CPU usage.
     * @return CPU usage as a percentage, or -1 if unavailable.
     */
    private double getCPUUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            double cpuLoad = ((com.sun.management.OperatingSystemMXBean) osBean).getCpuLoad();
            if (cpuLoad >= 0) {
                return cpuLoad * 100; // Convert to percentage
            }
        }
        return -1; // Indicate that CPU usage couldn't be retrieved
    }

    /**
     * Retrieves the current memory usage.
     * @return Memory usage in bytes.
     */
    private long getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory(); // In bytes
    }

    // -----------------------------------------------------
    // Console Logging with Color-Coded Output
    // -----------------------------------------------------

    /**
     * Logs an informational message to the console with green color.
     * @param message The message to display.
     */
    public void logInfo(String message) {
        System.out.println("INFO: " + message);
    }

    /**
     * Logs an error message to the console with red color.
     * @param message The message to display.
     */
    public void logError(String message) {
        System.out.println(RED + "ERROR: " + message + RESET);
    }

    /**
     * Logs a warning message to the console with yellow color.
     * @param message The message to display.
     */
    public void logWarning(String message) {
        System.out.println(YELLOW + "WARNING: " + message + RESET);
    }

    /**
     * Logs a new line message to the console with cyan color.
     * @param message The message to display.
     */
    public void logNewline(String message) {
        System.out.println();
        System.out.println(CYAN + message + RESET);
        System.out.println();

    
    }

    public void logSuccess(String message) {
        System.out.println(GREEN + "SUCCESS: " + message + RESET);
    }

    public void logNetwork(String message) {
        System.out.println(BLUE + "NETWORK: " + message + RESET);
    }
}
