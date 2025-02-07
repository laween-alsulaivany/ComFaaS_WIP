// Main.java
package comfaas;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.io.File;


/**
 * The Main class serves as the entry point for the ComFaaS application.
 * It handles command-line arguments and invokes the appropriate functionality
 * for server and edge operations.
 */
public class Main {

    // -------------------------------------------------------------------------
    // Configuration
    // -------------------------------------------------------------------------
    private static final Properties configProps = new Properties();

    // Pulled from configProps or defaults
    public static String LogFile;
    public static int maxThreads;
    public static int shutdownTimeout; // in seconds
    private static int cloudPort;
    private static int edgePort;

    // Our logger instance (constructed later)
    private static Logger logger;

    // -------------------------------------------------------------------------
    // Determine the root directory of the project.
    // 1) Try environment variable ROOT_DIR.
    // 2) If not set, fallback to one level above the current directory.
    // -------------------------------------------------------------------------
    public static final Path rootDir = getRootDir();

    public static Path getRootDir() {
        String envRoot = System.getenv("ROOT_DIR");
        if (envRoot != null && !envRoot.isEmpty()) {
            return Paths.get(envRoot).toAbsolutePath();
        }
        // Fallback: assume the jar runs from the "comfaas_V3" root or a subfolder
        // We go one level up from the current folder:
        return Paths.get("").toAbsolutePath().getParent();
        // Alternatively, we could do:
        // return Paths.get("").toAbsolutePath();
    }

    public static File resolveLocalFile(String path) {
    File file = new File(path);
    // If not absolute, resolve it against the project root directory.
    if (!file.isAbsolute()) {
         file = rootDir.resolve(path).toFile();
    }
    return file;
}


    // Getters for the static fields
    public static String getLogFile() {
        return LogFile;
    }

    public static int getMaxThreads() {
        return maxThreads;
    }

    public static int getShutdownTimeout() {
        return shutdownTimeout;
    }

    // -------------------------------------------------------------------------
    // main(): Entry point for ComFaaS. Parses arguments and calls appropriate
    // methods.
    // -------------------------------------------------------------------------
    public static void main(String[] args) throws InterruptedException {
        loadProperties();
        logger = new Logger(LogFile); // logger is now properly initialized

        if (args.length < 2) {
            printUsage();
            return;
        }

        switch (args[0]) {
            case "server" -> handleServerCommands(args);
            case "edge" -> handleEdgeCommands(args);
            default -> printUsage();
        }
    }

    // -------------------------------------------------------------------------
    // loadProperties(): Attempt to load comfaas.properties from
    // <rootDir>/backend/configs/comfaas.properties
    // -------------------------------------------------------------------------
    private static void loadProperties() {
        Path configPath = rootDir
                .resolve("backend")
                .resolve("configs")
                .resolve("comfaas.properties");

        if (Files.exists(configPath)) {
            try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
                configProps.load(fis);
                System.out.println("\u001B[32m[INFO] Loaded config from: "
                        + configPath.toAbsolutePath() + "\u001B[0m");
            } catch (Exception e) {
                System.err.println("Error loading config file: " + e.getMessage());
            }
        } else {
            System.out.println("No comfaas.properties file found at: "
                    + configPath.toAbsolutePath() + " using defaults.");
        }

        // Parse config properties
        LogFile = configProps.getProperty("log.file", "comfaas_logs.csv");
        maxThreads = Integer.parseInt(configProps.getProperty("max.threads", "10"));
        shutdownTimeout = Integer.parseInt(configProps.getProperty("graceful.shutdown.timeout", "30"));
        cloudPort = Integer.parseInt(configProps.getProperty("cloud.server.port", "12353"));
        edgePort = Integer.parseInt(configProps.getProperty("edge.server.port", "12354"));
    }

    // -------------------------
    // * SERVER RELATED COMMANDS
    // -------------------------

    // -------------------------
    // Processes server-related commands: initialization and running.
    // -------------------------

    private static void handleServerCommands(String[] args) throws InterruptedException {
        switch (args[1]) {
            case "init" -> initializeServer();
            case "cloud" -> runCloudServer(args);
            case "edge" -> runEdgeServer(args);
            default -> printUsage();
        }
    }

    // -------------------------
    // initializeServer():
    // Creates server directories under <rootDir>/server/ (Programs, Input, Output),
    // and checks for .serverVenv.
    // -------------------------

    private static void initializeServer() {
        logger.network("Main", "initializeServer", "Server initializing...");

        // Desired directories for the server
        Path[] directories = {
                rootDir.resolve("server").resolve("Programs"),
                rootDir.resolve("server").resolve("Input"),
                rootDir.resolve("server").resolve("Output")
        };

        try {
            createDirectories(directories);
            System.out.println("Server directories verified.");
            logger.success("Main", "initializeServer", "Server directories verified.");

            // Check server venv existence
            Path venvPath = rootDir.resolve(".serverVenv");
            if (!Files.exists(venvPath)) {
                throw new IOException("Python virtual environment not found at: " + venvPath);
            }
            logger.success("Main", "initializeServer", "Python virtual environment verified.");

        } catch (IOException e) {
            logger.error("Main", "initializeServer", "Error initializing server: " + e.getMessage());
            System.exit(1);
        }
    }

    // -------------------------
    // runCloudServer():
    // Creates server directories if needed, then starts the cloud server.
    // -------------------------

    private static void runCloudServer(String[] args) throws InterruptedException {
        int port = cloudPort; // default
        // Check for -p <port> override
        for (int i = 2; i < args.length; i++) {
            if ("-p".equals(args[i]) && i + 1 < args.length) {
                port = Integer.parseInt(args[++i]);
            }
        }
        logger.network("Main", "runCloudServer", "Cloud server starting on port " + port);

        Path[] directories = {
                rootDir.resolve("server").resolve("Programs"),
                rootDir.resolve("server").resolve("Input"),
                rootDir.resolve("server").resolve("Output")
        };

        try {
            createDirectories(directories);
            CloudServer.run(port);
        } catch (IOException e) {
            logger.error("Main", "runCloudServer", "Error running cloud server: " + e.getMessage());
            System.exit(1);
        }
    }

    // -------------------------
    // runEdgeServer():
    // runEdgeServer():
    // (Sometimes used for local dev) Creates server directories if needed, then
    // starts an edge server.
    // -------------------------
    // TODO: In the current usage, "server edge" could be a separate mode, but here
    // it reuses the same logic. maybe fix later.
    private static void runEdgeServer(String[] args) throws InterruptedException {
        int port = edgePort;
        for (int i = 2; i < args.length; i++) {
            if ("-p".equals(args[i]) && i + 1 < args.length) {
                port = Integer.parseInt(args[++i]);
            }
        }
        logger.network("Main", "runEdgeServer", "Edge server starting on port " + port);

        Path[] directories = {
                rootDir.resolve("server").resolve("Programs"),
                rootDir.resolve("server").resolve("Input"),
                rootDir.resolve("server").resolve("Output")
        };

        try {
            createDirectories(directories);
            EdgeServer.run(port);
        } catch (IOException e) {
            logger.error("Main", "runEdgeServer", "Error running edge server: " + e.getMessage());
            System.exit(1);
        }
    }

    // -------------------------
    // * CLIENT RELATED COMMANDS
    // -------------------------
    // -------------------------
    // Processes edge-related commands: initialization, running tasks,
    // and file operations (send, request, delete, list).
    // -------------------------

    private static void handleEdgeCommands(String[] args) {
        switch (args[1]) {
            case "init" -> initializeEdge();
            case "remotetask" -> handleEdgeRemoteTask(args);
            case "upload" -> handleEdgeUpload(args); // TODO: BUNDLE ALL THE FILE OPERATION COMMANDS INTO ONE
            case "download" -> handleEdgeDownload(args);
            case "listfiles" -> handleEdgeListFiles(args);
            case "deletefile" -> handleEdgeDeleteFile(args);
            case "uploadfolder" -> handleEdgeUploadFolder(args);
            case "downloadfolder" -> handleEdgeDownloadFolder(args);
            case "deletefolder" -> handleEdgeDeleteFolder(args);
            default -> printUsage();
        }
    }

    // -------------------------
    // initializeEdge():
    // Creates edge directories under <rootDir>/edge/ (Programs, Input, Output),
    // and checks for .edgeVenv.
    // -------------------------
    private static void initializeEdge() {
        logger.network("Main", "initializeEdge", "Edge initializing...");

        Path[] directories = {
                rootDir.resolve("edge").resolve("Programs"),
                rootDir.resolve("edge").resolve("Input"),
                rootDir.resolve("edge").resolve("Output")
        };

        try {
            createDirectories(directories);
            logger.success("Main", "initializeEdge", "Edge directories verified.");

            Path venvPath = rootDir.resolve(".edgeVenv");
            if (!Files.exists(venvPath)) {
                throw new IOException("Python virtual environment not found at: " + venvPath);
            }
            logger.success("Main", "initializeEdge", "Python virtual environment verified.");

        } catch (IOException e) {
            logger.error("Main", "initializeEdge", "Error initializing edge: " + e.getMessage());
            System.exit(1);
        }
    }

    // -------------------------
    // Starts the edge client by initializing it with the given arguments
    // and executing the requested task.
    // THIS IS A LOCAL CONNECTION
    // -------------------------

    // private static void runEdge(String[] args) {
    // System.out.println("Starting edge client...");
    // edge_logger.log("INFO", "Edge client started.", "EdgeRun", "Initialization",
    // -1);

    // // Pass the rest of the arguments to EdgeClient
    // EdgeClient edge = new EdgeClient(java.util.Arrays.copyOfRange(args, 2,
    // args.length));

    // try {
    // edge.runTask(); // executes the "executeTask" command
    // } catch (IOException e) {
    // System.err.println("Error running edge task: " + e.getMessage());
    // edge_logger.logError(e.getMessage(), "EdgeRun", "Initialization");
    // }
    // }

    // -------------------------
    // Starts the edge client by initializing it with the given arguments
    // and executing the requested task.
    // THIS IS A REMOTE CONNECTION
    // -------------------------

    private static void handleEdgeRemoteTask(String[] args) {
        // parse arguments
        String server = null;
        int port = -1;
        String location = null; // "cloud" or "edge"
        String lang = null; // "c", "python", "java", etc.
        String programName = null;
        int np = 1;

        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "-server" -> server = args[++i];
                case "-p" -> port = Integer.parseInt(args[++i]);
                case "-t" -> location = args[++i];
                case "-lang" -> lang = args[++i];
                case "-tn" -> programName = args[++i];
                case "-np" -> np = Integer.parseInt(args[++i]);
            }
        }

        if (server == null || port < 0 || location == null ||
                lang == null || programName == null) {
            logger.warning("Main", "handleEdgeRemoteTask",
                    "Usage: edge remotetask -server <ip> -p <port> -t [cloud|edge] -lang <language> -tn <program> -np <count>");
            return;
        }

        String[] clientArgs = {
                "-server", server,
                "-p", String.valueOf(port),
                "-t", "edge",
                "-np", String.valueOf(np),
                "-tn", programName,
                "-lang", lang
        };

        EdgeClient client = new EdgeClient(clientArgs);
        try {
            client.runRemoteTask(location, lang, programName, np);
            logger.success("Main", "handleEdgeRemoteTask", "Remote task complete");
        } catch (IOException e) {
            logger.error("Main", "handleEdgeRemoteTask", "Error executing remote task: " + e.getMessage());
        } finally {
            try {
                client.close(false);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    // -------------------------
    // Handles the edge upload command by uploading a file to the server.
    // -------------------------
    private static void handleEdgeUpload(String[] args) {
        String server = null;
        int port = -1;
        String localFile = null;
        String remoteFolder = null;

        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "-server" -> server = args[++i];
                case "-p" -> port = Integer.parseInt(args[++i]);
                case "-local" -> localFile = args[++i];
                case "-remote" -> remoteFolder = args[++i];
            }
        }

        if (server == null || port < 0 || localFile == null || remoteFolder == null) {
            logger.warning("Main", "handleEdgeUpload",
                    "Usage: edge upload -server <ip> -p <port> -local <localFile> -remote <remoteFolder>");
            return;
        }

        String[] clientArgs = {
                "-server", server,
                "-p", String.valueOf(port),
                "-t", "edge",
                "-np", "1",
                "-tn", "dummy",
                "-lang", "c"
        };
        EdgeClient client = new EdgeClient(clientArgs);

        try {
            client.uploadFile(localFile, remoteFolder);
            logger.success("Main", "handleEdgeUpload",
                    "Uploaded " + localFile + " to " + remoteFolder + " successfully");
        } catch (IOException e) {
            logger.error("Main", "handleEdgeUpload", "Error uploading file: " + e.getMessage());
        } finally {
            try {
                client.close(false);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    // -------------------------
    // Handles the edge download command by downloading a file from the server.
    // -------------------------
    private static void handleEdgeDownload(String[] args) {
        String server = null;
        int port = -1;
        String remoteFolder = null;
        String fileName = null;
        String localFolder = null;

        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "-server" -> server = args[++i];
                case "-p" -> port = Integer.parseInt(args[++i]);
                case "-remote" -> remoteFolder = args[++i];
                case "-file" -> fileName = args[++i];
                case "-local" -> localFolder = args[++i];
            }
        }

        if (server == null || port < 0 || remoteFolder == null || fileName == null || localFolder == null) {
            logger.warning("Main", "handleEdgeDownload",
                    "Usage: edge download -server <ip> -p <port> -remote <folder> -file <filename> -local <folder>");
            return;
        }

        String[] clientArgs = {
                "-server", server,
                "-p", String.valueOf(port),
                "-t", "edge",
                "-np", "1",
                "-tn", "dummy",
                "-lang", "c"
        };
        EdgeClient client = new EdgeClient(clientArgs);

        try {
            client.downloadFile(remoteFolder, fileName, localFolder);
            logger.success("Main", "handleEdgeDownload",
                    "Downloaded " + fileName + " from " + remoteFolder + " to " + localFolder + " successfully");
        } catch (IOException e) {
            logger.error("Main", "handleEdgeDownload", "Error downloading file: " + e.getMessage());
        } finally {
            try {
                client.close(false);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    // -------------------------
    // Handles the edge list command by listing files in a remote folder.
    // -------------------------
    private static void handleEdgeListFiles(String[] args) {
        String server = null;
        int port = -1;
        String remoteFolder = null;

        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "-server" -> server = args[++i];
                case "-p" -> port = Integer.parseInt(args[++i]);
                case "-dir" -> remoteFolder = args[++i];
            }
        }

        if (server == null || port < 0 || remoteFolder == null) {
            logger.warning("Main", "handleEdgeListFiles",
                    "Usage: edge listfiles -server <ip> -p <port> -dir <remoteFolder>");
            return;
        }

        String[] clientArgs = {
                "-server", server,
                "-p", String.valueOf(port),
                "-t", "edge",
                "-np", "1",
                "-tn", "dummy",
                "-lang", "c"
        };
        EdgeClient client = new EdgeClient(clientArgs);

        try {
            logger.info("Main", "handleEdgeListFiles",
                    "Listing files in " + remoteFolder + " on server " + server + ":" + port + "...");
            client.listFilesOnServer(remoteFolder);
        } catch (IOException e) {
            logger.error("Main", "handleEdgeListFiles", "Error listing files: " + e.getMessage());
        } finally {
            try {
                client.close(false);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    // -------------------------
    // Handles the edge deletefile command by deleting a file from the server.
    // -------------------------
    private static void handleEdgeDeleteFile(String[] args) {
        String server = null;
        int port = -1;
        String folder = null;
        String fileName = null;

        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "-server" -> server = args[++i];
                case "-p" -> port = Integer.parseInt(args[++i]);
                case "-folder" -> folder = args[++i];
                case "-file" -> fileName = args[++i];
            }
        }

        if (server == null || port < 0 || folder == null || fileName == null) {
            logger.warning("Main", "handleEdgeDeleteFile",
                    "Usage: edge deletefile -server <ip> -p <port> -folder <remoteFolder> -file <filename>");
            return;
        }

        String[] clientArgs = {
                "-server", server,
                "-p", String.valueOf(port),
                "-t", "edge",
                "-np", "1",
                "-tn", "dummy",
                "-lang", "c"
        };
        EdgeClient client = new EdgeClient(clientArgs);

        try {
            client.deleteFileOnServer(folder, fileName);
            logger.success("Main", "handleEdgeDeleteFile",
                    "Deleted " + fileName + " from folder " + folder + " on " + server + ":" + port + " successfully");
        } catch (IOException e) {
            logger.error("Main", "handleEdgeDeleteFile", "Error deleting file: " + e.getMessage());
        } finally {
            try {
                client.close(false);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    // -------------------------
    // Handles the edge folder upload command by uploading a folder to the server.
    // -------------------------
    private static void handleEdgeUploadFolder(String[] args) {
        String server = null;
        int port = -1;
        String localFolder = null;
        String remoteFolder = null;

        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "-server" -> server = args[++i];
                case "-p" -> port = Integer.parseInt(args[++i]);
                case "-local" -> localFolder = args[++i];
                case "-remote" -> remoteFolder = args[++i];
            }
        }

        if (server == null || port < 0 || localFolder == null || remoteFolder == null) {
            logger.warning("Main", "handleEdgeUploadFolder",
                    "Usage: edge uploadfolder -server <ip> -p <port> -local <localFolder> -remote <remoteFolder>");
            return;
        }

        String[] clientArgs = {
                "-server", server,
                "-p", String.valueOf(port),
                "-t", "edge",
                "-np", "1",
                "-tn", "dummy",
                "-lang", "c"
        };
        EdgeClient client = new EdgeClient(clientArgs);

        try {
            client.uploadFolder(localFolder, remoteFolder);
            logger.success("Main", "handleEdgeUploadFolder",
                    "Uploaded " + localFolder + " to " + remoteFolder + " successfully");
        } catch (IOException e) {
            logger.error("Main", "handleEdgeUploadFolder", "Error uploading folder: " + e.getMessage());
        } finally {
            try {
                client.close(false);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    // -------------------------
    // Handles the edge folder download command by downloading a folder from the
    // server.
    // -------------------------
    private static void handleEdgeDownloadFolder(String[] args) {
        String server = null;
        int port = -1;
        String remoteFolder = null;
        String localFolder = null;

        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "-server" -> server = args[++i];
                case "-p" -> port = Integer.parseInt(args[++i]);
                case "-remote" -> remoteFolder = args[++i];
                case "-local" -> localFolder = args[++i];
            }
        }

        if (server == null || port < 0 || remoteFolder == null || localFolder == null) {
            logger.warning("Main", "handleEdgeDownloadFolder",
                    "Usage: edge downloadfolder -server <ip> -p <port> -remote <folder> -local <folder>");
            return;
        }

        String[] clientArgs = {
                "-server", server,
                "-p", String.valueOf(port),
                "-t", "edge",
                "-np", "1",
                "-tn", "dummy",
                "-lang", "c"
        };
        EdgeClient client = new EdgeClient(clientArgs);

        try {
            client.downloadFolder(remoteFolder, localFolder);
            logger.success("Main", "handleEdgeDownloadFolder",
                    "Downloaded " + remoteFolder + " to " + localFolder + " successfully");
        } catch (IOException e) {
            logger.error("Main", "handleEdgeDownloadFolder", "Error downloading folder: " + e.getMessage());
        } finally {
            try {
                client.close(false);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    // -------------------------
    // Handles the edge folder delete command by deleting a folder from the server.
    // -------------------------
    private static void handleEdgeDeleteFolder(String[] args) {
        String server = null;
        int port = -1;
        String folder = null;

        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "-server" -> server = args[++i];
                case "-p" -> port = Integer.parseInt(args[++i]);
                case "-folder" -> folder = args[++i];
            }
        }

        if (server == null || port < 0 || folder == null) {
            logger.warning("Main", "handleEdgeDeleteFolder",
                    "Usage: edge deletefolder -server <ip> -p <port> -folder <folder>");
            return;
        }

        String[] clientArgs = {
                "-server", server,
                "-p", String.valueOf(port),
                "-t", "edge",
                "-np", "1",
                "-tn", "dummy",
                "-lang", "c"
        };
        EdgeClient client = new EdgeClient(clientArgs);

        try {
            client.deleteFolder(folder);
            logger.success("Main", "handleEdgeDeleteFolder",
                    "Deleted " + folder + " successfully");
        } catch (IOException e) {
            logger.error("Main", "handleEdgeDeleteFolder", "Error deleting folder: " + e.getMessage());
        } finally {
            try {
                client.close(false);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    // -------------------------
    // * UTILITY FUNCTIONS
    // -------------------------

    // -------------------------------------------------------------------------
    // createDirectories(): Creates directories if they don't exist
    // -------------------------------------------------------------------------
    private static void createDirectories(Path[] directories) throws IOException {
        for (Path directory : directories) {
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
                logger.success("Main", "createDirectories", "Created directory: " + directory);
            }
        }
    }

    // -------------------------------------------------------------------------
    // printUsage(): Quick usage instructions for ComFaaS
    // -------------------------------------------------------------------------
    private static void printUsage() {
        System.err.println("Invalid arguments. Usage:");
        System.err.println("  - Server Init: server init");
        System.err.println("  - Server Cloud: server cloud [-p <port>]");
        System.err.println("  - Server Edge: server edge [-p <port>]");
        System.err.println("  - Edge Init: edge init");
        System.err.println("  - Edge Remote Task: edge remotetask [args]");
        System.err.println("  - Edge Upload: edge upload [args]");
        System.err.println("  - Edge Download: edge download [args]");
        System.err.println("  - Edge List Files: edge listfiles [args]");
        System.err.println("  - Edge Delete File: edge deletefile [args]");
        System.err.println("  - Edge Upload Folder: edge uploadfolder [args]");
        System.err.println("  - Edge Download Folder: edge downloadfolder [args]");
        System.err.println("  - Edge Delete Folder: edge deletefolder [args]");
        System.exit(1);
    }

}
