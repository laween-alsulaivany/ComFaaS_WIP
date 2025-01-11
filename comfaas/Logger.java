// Logger.java

package comfaas;

import static comfaas.ANSI_Colors.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;


// ------------------------------------------
// * A unified Logger for both console (color-coded) and CSV file logging.
// ------------------------------------------ 
public class Logger {
    // File path for the log file where logs are written
    private final String logFilePath;

    // Log levels for different types of messages
    public enum LogLevel {
        INFO, WARNING, ERROR, SUCCESS, NETWORK, NEWLINE
    }

    // -----------------------------------------------------
    // Constructor to initialize the Logger with a specific log file path.
    // -----------------------------------------------------
    public Logger(String logFilePath) {
        this.logFilePath = logFilePath;
        initializeLogFile(); // Ensure the log file is initialized with headers
    }

    // -----------------------------------------------------
    // Ensures the log file exists and adds a header row if the file is empty.
    // -----------------------------------------------------
    private void initializeLogFile() {
        File logFile = new File(logFilePath);
        if (!logFile.exists() || logFile.length() == 0) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(logFile, true))) {
                pw.println("Timestamp,Level,Component,Operation,Message,Duration(ms),CPU_Usage(%),Memory_Usage(MB),FileSize,ThreadID");
            } catch (IOException e) {
                System.err.println(RED + "[ERROR] Failed to create header in CSV: " + e.getMessage() + RESET);
            }
        }
    }


    // -----------------------------------------------------
    // Logging Methods
    // - Writes to console in color-coded form
    // - Appends a line to the CSV file
    // -----------------------------------------------------
    public synchronized void logEvent(
        LogLevel level,       // e.g. INFO, WARNING, ERROR
        String component,     // e.g. "CloudServer", "EdgeServer", "EdgeClient"
        String operation,     // e.g. "uploadFile", "executeTask"
        String message,      // e.g. "File uploaded successfully"
        long durationMs,     // e.g. 3078ms
        long fileSize       // e.g. 1024 bytes
    ) {
        // Color-coded console logging
        consoleLog(level, component, operation, message);

        // CSV logging
        double cpuUsage = getCPUUsage();
        double memMB    = getMemoryUsage() / (1024.0 * 1024.0);
        long threadId   = Thread.currentThread().getId(); // TODO: to be implemented in the last column

        try (FileWriter fw = new FileWriter(logFilePath, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) 
        {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%.2f,%.2f,%.2f,%.0f%n",
                timestamp,
                level.toString(),
                component,
                operation,
                message.replace("\"", "'"), // CSV-friendly
                (double) durationMs,
                cpuUsage,
                memMB,
                (double) fileSize,
                (double) threadId // store in last column
            );
        } catch (IOException e) {
            System.err.println(RED + "[ERROR] Failed to write CSV log: " + e.getMessage() + RESET);
        }
    }

    // -----------------------------------------------------
    // A helper for console color-coded logging
    // -----------------------------------------------------
    private void consoleLog(LogLevel level, String component, String operation, String message) {
        // pick a color based on level
        String color;
        switch (level) {
            case INFO    -> color = CYAN;
            case WARNING -> color = YELLOW;
            case ERROR   -> color = RED;
            case SUCCESS -> color = GREEN;
            case NETWORK -> color = BLUE;
            case NEWLINE -> color = WHITE;
            default      -> color = RESET;
        }

        String currentTime = new Time(System.currentTimeMillis()).toString();
        System.out.println(color
            + "[" + currentTime + "] "
            + "[" + level + "] " + component + " :: " + operation + " -> " + message
            + RESET);
    }
    
    // convenience methods for short calls:
    public void info(String comp, String op, String msg) {
        logEvent(LogLevel.INFO, comp, op, msg, 0, -1);
    }
    public void error(String comp, String op, String msg) {
        logEvent(LogLevel.ERROR, comp, op, msg, 0, -1);
    }
    public void success(String comp, String op, String msg) {
        logEvent(LogLevel.SUCCESS, comp, op, msg, 0, -1);
    }
    public void network(String comp, String op, String msg) {
        logEvent(LogLevel.NETWORK, comp, op, msg, 0, -1);
    }
    public void warning(String comp, String op, String msg) {
        logEvent(LogLevel.WARNING, comp, op, msg, 0, -1);
    }
    public void newline() {
        System.out.println();
        System.out.println("-----------------------------------------------------");
        System.out.println();
    }


    // TODO: CPU USAGE, MEM USAGE, and TIMESTAMPING to be implemented
    // -----------------------------------------------------
    // CPU usage in % or -1 if not available
    // -----------------------------------------------------
    private double getCPUUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean operatingSystemMXBean) {
            double load = operatingSystemMXBean.getCpuLoad();
            if (load >= 0) return load * 100.0;
        }
        return -1;
    }

    // -----------------------------------------------------
    // Memory usage in bytes
    // -----------------------------------------------------
    private long getMemoryUsage() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

}
   


  



 


//     // -----------------------------------------------------
//     // Console Logging with Color-Coded Output
//     // -----------------------------------------------------

//     /**
//      * Logs an informational message to the console with green color.
//      * @param message The message to display.
//      */
//     public void logInfo(String message) {
//         System.out.println("INFO: " + message);
//     }

//     /**
//      * Logs an error message to the console with red color.
//      * @param message The message to display.
//      */
//     public void logError(String message) {
//         System.out.println(RED + "ERROR: " + message + RESET);
//     }

//     /**
//      * Logs a warning message to the console with yellow color.
//      * @param message The message to display.
//      */
//     public void logWarning(String message) {
//         System.out.println(YELLOW + "WARNING: " + message + RESET);
//     }

//     /**
//      * Logs a new line message to the console with cyan color.
//      * @param message The message to display.
//      */
//     public void logNewline(String message) {
//         System.out.println();
//         System.out.println(CYAN + message + RESET);
//         System.out.println();

    
//     }

//     public void logSuccess(String message) {
//         System.out.println(GREEN + "SUCCESS: " + message + RESET);
//     }

//     public void logNetwork(String message) {
//         System.out.println(BLUE + "NETWORK: " + message + RESET);
//     }
// }
