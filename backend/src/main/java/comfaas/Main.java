// Main.java
package comfaas;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * The Main class serves as the entry point for the ComFaaS application.
 * It handles command-line arguments and invokes the appropriate functionality
 * for server and client operations.
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
    public static int serverPort;
    // Our logger instance (constructed later)
    private static Logger logger;

    public static String serverType = "cloud"; // default to "cloud" if not specified
    public static String[] cliArgs; // for edge discovery

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
        return Paths.get("").toAbsolutePath();
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

        // String RootDirectory = FindFolder("COMFAAS_WIP");
        // String comfaas_code = FindFolder("comfaas");
        // String config = FindFolder("configs");
        // String Program_folder = FindFolder("Programs");
        // System.err.println("Root Directory: " + RootDirectory);
        // System.err.println("Comfaas Code: " + comfaas_code);
        // System.err.println("Config: " + config);
        // System.err.println("Programs: " + Program_folder);
        cliArgs = args; // <--- store them
        loadProperties();
        logger = new Logger(LogFile); // logger is now properly initialized

        if (args.length < 2) {
            printUsage(); // TODO: DONE
            return;
        }

        switch (args[0]) {
            case "server" -> handleServerCommands(args);
            case "client" -> handleClientCommands(args);
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
        serverPort = Integer.parseInt(configProps.getProperty("cloud.server.port", "12353"));
        // serverType = configProps.getProperty("server.type", "cloud"); //TODO: Add it
        // later
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
            case "server" -> runServer(args);
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
    // runServer():
    // Creates server directories if needed, then starts the server.
    // -------------------------

    private static void runServer(String[] args) throws InterruptedException {
        int port = serverPort; // default
        // Check for -p <port> override
        // Check for -p <port> AND an optional -role <cloud|edge>
        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "-p" -> {
                    if (i + 1 < args.length) {
                        port = Integer.parseInt(args[++i]);
                    }
                }
                case "-role" -> {
                    if (i + 1 < args.length) {
                        serverType = args[++i].toLowerCase();
                    }
                }
            }
        }
        logger.network("Main", "runServer", "Server starting on port " + port);

        Path[] directories = {
                rootDir.resolve("server").resolve("Programs"),
                rootDir.resolve("server").resolve("Input"),
                rootDir.resolve("server").resolve("Output")
        };

        try {
            verifyDirectories(directories, "runServer");
            Server.run(port);
        } catch (IOException e) {
            logger.error("Main", "runServer", "Error running server: " + e.getMessage());
            System.exit(1);
        }
    }

    // -------------------------
    // * CLIENT RELATED COMMANDS
    // -------------------------
    // -------------------------
    // Processes client-related commands: initialization, running tasks,
    // and file operations (send, request, delete, list).
    // -------------------------

    private static void handleClientCommands(String[] args) {
        switch (args[1]) {
            case "init" -> initializeClient();
            case "runtask" -> handleClientRemoteTask(args);
            case "upload" -> handleClientUpload(args); // TODO: BUNDLE ALL THE FILE OPERATION COMMANDS INTO ONE
            case "download" -> handleClientDownload(args);
            case "listfiles" -> handleClientListFiles(args);
            case "deletefile" -> handleClientDeleteFile(args);
            case "uploadfolder" -> handleClientUploadFolder(args);
            case "downloadfolder" -> handleClientDownloadFolder(args);
            case "deletefolder" -> handleClientDeleteFolder(args);
            default -> printUsage();
        }
    }

    // -------------------------
    // initializeClient():
    // Creates client directories under <rootDir>/client/ (Programs, Input, Output),
    // and checks for .clientVenv.
    // -------------------------
    private static void initializeClient() {
        logger.network("Main", "initializeClient", "Client initializing...");

        Path[] directories = {
                rootDir.resolve("client").resolve("Programs"),
                rootDir.resolve("client").resolve("Input"),
                rootDir.resolve("client").resolve("Output")
        };

        try {
            createDirectories(directories);
            logger.success("Main", "initializeClient", "Client directories verified.");

            Path venvPath = rootDir.resolve(".clientVenv");
            if (!Files.exists(venvPath)) {
                throw new IOException("Python virtual environment not found at: " + venvPath);
            }
            logger.success("Main", "initializeClient", "Python virtual environment verified.");

        } catch (IOException e) {
            logger.error("Main", "initializeClient", "Error initializing client: " + e.getMessage());
            System.exit(1);
        }
    }

    // -------------------------
    // Starts the client by initializing it with the given arguments
    // and executing the requested task.
    // THIS IS A REMOTE CONNECTION
    // -------------------------

    private static void handleClientRemoteTask(String[] args) {
        // Parse arguments
        Map<String, String> params = parseArguments(args, 2);
        String server = params.get("-server");
        int port = params.containsKey("-p") ? Integer.parseInt(params.get("-p")) : -1;
        String location = params.containsKey("-l") ? params.get("-l") : "server";
        String lang = params.containsKey("-lang") ? params.get("-lang") : "null";
        String programName = params.containsKey("-tn") ? params.get("-tn") : "null";
        int np = params.containsKey("-np") ? Integer.parseInt(params.get("-np")) : 1;

        // Validate required parameters:
        if (server == null || port < 0 || location == null || lang == null || programName == null) {
            logger.warning("Main", "handleClientRemoteTask",
                    "Usage: client runtask -server <ip> -p <port> -l [cloud|edge] -lang <language> -tn <program> -np <count>");
            return;
        }

        Map<String, String> clientParams = new HashMap<>();
        clientParams.put("server", server);
        clientParams.put("port", String.valueOf(port));
        clientParams.put("location", location);
        clientParams.put("lang", lang);
        clientParams.put("tn", programName);
        clientParams.put("np", String.valueOf(np));

        String[] clientArgs = buildClientArgs(clientParams);
        Client client = new Client(clientArgs);
        System.out.println("Client initialized");
        System.out.println("Client args: " + Arrays.toString(clientArgs));
        System.out.println("Location: " + location);
        System.out.println("Lang: " + lang);
        System.out.println("Program: " + programName);
        System.out.println("NP: " + np);
        try {
            System.out.println("Trying to executre remote task...");
            client.runTask(location, lang, programName, np);
            System.out.println("Remote task complete");
            logger.success("Main", "handleClientRemoteTask", "Remote task complete");
        } catch (SocketException e) {

            // logger.error("Main", "handleClientRemoteTask", "Error executing remote task:
            // " + e.getStackTrace());
            // e.printStackTrace();
        } catch (IOException e) {
            logger.error("Main", "handleClientRemoteTask", "Error executing remote task: " + e.getStackTrace());
            e.printStackTrace();
        } finally {
            try {
                client.close(false);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    // -------------------------
    // Handles the client upload command by uploading a file to the server.
    // -------------------------
    private static void handleClientUpload(String[] args) {

        // Parse arguments
        Map<String, String> params = parseArguments(args, 2);
        String server = params.get("-server");
        int port = params.containsKey("-p") ? Integer.parseInt(params.get("-p")) : -1;
        String localFile = params.get("-local");
        String remoteFolder = params.get("-remote");
        String location = params.containsKey("-l") ? params.get("-l") : "server";

        // Validate required parameters:
        if (server == null || port < 0 || localFile == null || remoteFolder == null) {
            logger.warning("Main", "handleClientUpload",
                    "Usage: client upload -server <ip> -p <port> -l [cloud|edge] -local <localFile> -remote <remoteFolder>");
            return;
        }

        Map<String, String> clientParams = new HashMap<>();
        clientParams.put("server", server);
        clientParams.put("port", String.valueOf(port));
        clientParams.put("location", location);
        // clientParams.put("local", localFile);
        // clientParams.put("remote", remoteFolder);

        String[] clientArgs = buildClientArgs(clientParams);
        Client client = new Client(clientArgs);

        try {
            client.uploadFile(localFile, remoteFolder);
            logger.success("Main", "handleClientUpload",
                    "Uploaded " + localFile + " to " + remoteFolder + " successfully");
        } catch (IOException e) {
            logger.error("Main", "handleClientUpload", "Error uploading file: " + e.getMessage());
        } finally {
            try {
                client.close(false);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    // -------------------------
    // Handles the client download command by downloading a file from the server.
    // -------------------------
    private static void handleClientDownload(String[] args) {
        // Parse arguments
        Map<String, String> params = parseArguments(args, 2);
        String server = params.get("-server");
        int port = params.containsKey("-p") ? Integer.parseInt(params.get("-p")) : -1;
        String location = params.containsKey("-l") ? params.get("-l") : "server";
        String remoteFolder = params.get("-remote");
        String localFolder = params.get("-local");
        String fileName = params.get("-file");

        // Validate required parameters:
        if (server == null || port < 0 || remoteFolder == null || fileName == null || localFolder == null) {
            logger.warning("Main", "handleClientUpload",
                    "Usage: client download -server <ip> -p <port> -l [cloud|edge] -remote <folder> -file <filename> -local <folder>");
            return;
        }

        Map<String, String> clientParams = new HashMap<>();
        clientParams.put("server", server);
        clientParams.put("port", String.valueOf(port));
        clientParams.put("location", location);
        // clientParams.put("local", localFolder);
        // clientParams.put("remote", remoteFolder);
        // clientParams.put("file", fileName);

        String[] clientArgs = buildClientArgs(clientParams);
        Client client = new Client(clientArgs);

        try {
            client.downloadFile(remoteFolder, fileName, localFolder);
            logger.success("Main", "handleClientDownload",
                    "Downloaded " + fileName + " from " + remoteFolder + " to " + localFolder + " successfully");
        } catch (IOException e) {
            logger.error("Main", "handleClientDownload", "Error downloading file: " + e.getMessage());
        } finally {
            try {
                client.close(false);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    // -------------------------
    // Handles the client list command by listing files in a remote folder.
    // -------------------------
    private static void handleClientListFiles(String[] args) {
        // Parse arguments
        Map<String, String> params = parseArguments(args, 2);
        String server = params.get("-server");
        int port = params.containsKey("-p") ? Integer.parseInt(params.get("-p")) : -1;
        String location = params.containsKey("-l") ? params.get("-l") : "server";
        String remoteFolder = params.get("-dir");

        // Validate required parameters:
        if (server == null || port < 0 || remoteFolder == null) {
            logger.warning("Main", "handleClientListFiles",
                    "Usage: client listfiles -server <ip> -p <port> -l [cloud|edge] -dir <remoteFolder>");
            return;
        }

        Map<String, String> clientParams = new HashMap<>();
        clientParams.put("server", server);
        clientParams.put("port", String.valueOf(port));
        clientParams.put("location", location);
        // clientParams.put("remote", remoteFolder);

        String[] clientArgs = buildClientArgs(clientParams);
        Client client = new Client(clientArgs);

        try {
            logger.info("Main", "handleClientListFiles",
                    "Listing files in " + remoteFolder + " on server " + server + ":" + port + "...");
            client.listFilesOnServer(remoteFolder);
        } catch (IOException e) {
            logger.error("Main", "handleClientListFiles", "Error listing files: " + e.getMessage());
        } finally {
            try {
                client.close(false);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    // -------------------------
    // Handles the client deletefile command by deleting a file from the server.
    // -------------------------
    private static void handleClientDeleteFile(String[] args) {
        // Parse arguments
        Map<String, String> params = parseArguments(args, 2);
        String server = params.get("-server");
        int port = params.containsKey("-p") ? Integer.parseInt(params.get("-p")) : -1;
        String location = params.containsKey("-l") ? params.get("-l") : "server";
        String remoteFolder = params.get("-folder");
        String fileName = params.get("-file");

        // Validate required parameters:
        if (server == null || port < 0 || remoteFolder == null || fileName == null) {
            logger.warning("Main", "handleClientDeleteFile",
                    "Usage: client deletefile -server <ip> -p <port>  -l [cloud|edge] -folder <remoteFolder> -file <filename>");
            return;
        }
        Map<String, String> clientParams = new HashMap<>();
        clientParams.put("server", server);
        clientParams.put("port", String.valueOf(port));
        clientParams.put("location", location);
        // clientParams.put("remote", remoteFolder);
        // clientParams.put("file", fileName);

        String[] clientArgs = buildClientArgs(clientParams);
        Client client = new Client(clientArgs);

        try {
            client.deleteFileOnServer(remoteFolder, fileName);
            logger.success("Main", "handleClientDeleteFile",
                    "Deleted " + fileName + " from folder " + remoteFolder + " on " + server + ":" + port
                            + " successfully");
        } catch (IOException e) {
            logger.error("Main", "handleClientDeleteFile", "Error deleting file: " + e.getMessage());
        } finally {
            try {
                client.close(false);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    // TODO: when uploading a folder, it only uploads files not the folder itself,
    // so it could be an issue when downloading it
    // -------------------------
    // Handles the client folder upload command by uploading a folder to the server.
    // -------------------------
    private static void handleClientUploadFolder(String[] args) {
        // Parse arguments
        Map<String, String> params = parseArguments(args, 2);
        String server = params.get("-server");
        int port = params.containsKey("-p") ? Integer.parseInt(params.get("-p")) : -1;
        String location = params.containsKey("-l") ? params.get("-l") : "server";
        String remoteFolder = params.get("-remote");
        String localFolder = params.get("-local");

        // Validate required parameters:
        if (server == null || port < 0 || remoteFolder == null || localFolder == null) {
            logger.warning("Main", "handleClientUploadFolder",
                    "Usage: client uploadfolder -server <ip> -p <port> -l [cloud|edge] -remote <folder> -local <folder>");
            return;
        }
        Map<String, String> clientParams = new HashMap<>();
        clientParams.put("server", server);
        clientParams.put("port", String.valueOf(port));
        clientParams.put("location", location);
        // clientParams.put("local", localFolder);
        // clientParams.put("remote", remoteFolder);

        String[] clientArgs = buildClientArgs(clientParams);
        Client client = new Client(clientArgs);

        try {
            client.uploadFolder(localFolder, remoteFolder);
            logger.success("Main", "handleClientUploadFolder",
                    "Uploaded " + localFolder + " to " + remoteFolder + " successfully");
        } catch (IOException e) {
            logger.error("Main", "handleClientUploadFolder", "Error uploading folder: " + e.getMessage());
        } finally {
            try {
                client.close(false);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    // -------------------------
    // Handles the client folder download command by downloading a folder from the
    // server.
    // -------------------------
    private static void handleClientDownloadFolder(String[] args) {
        // Parse arguments
        Map<String, String> params = parseArguments(args, 2);
        String server = params.get("-server");
        int port = params.containsKey("-p") ? Integer.parseInt(params.get("-p")) : -1;
        String location = params.containsKey("-l") ? params.get("-l") : "server";
        String remoteFolder = params.get("-remote");
        String localFolder = params.get("-local");

        // Validate required parameters:
        if (server == null || port < 0 || remoteFolder == null || localFolder == null) {
            logger.warning("Main", "handleClientDownloadFolder",
                    "Usage: client downloadfolder -server <ip> -p <port> -l [cloud|edge] -remote <folder> -local <folder>");
            return;
        }
        Map<String, String> clientParams = new HashMap<>();
        clientParams.put("server", server);
        clientParams.put("port", String.valueOf(port));
        clientParams.put("location", location);
        // clientParams.put("local", localFolder);
        // clientParams.put("remote", remoteFolder);

        String[] clientArgs = buildClientArgs(clientParams);
        Client client = new Client(clientArgs);

        try {
            client.downloadFolder(remoteFolder, localFolder);
            logger.success("Main", "handleClientDownloadFolder",
                    "Downloaded " + remoteFolder + " to " + localFolder + " successfully");
        } catch (IOException e) {
            logger.error("Main", "handleClientDownloadFolder", "Error downloading folder: " + e.getMessage());
        } finally {
            try {
                client.close(false);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    // -------------------------
    // Handles the client folder delete command by deleting a folder from the
    // server.
    // -------------------------
    private static void handleClientDeleteFolder(String[] args) {

        // Parse arguments
        Map<String, String> params = parseArguments(args, 2);
        String server = params.get("-server");
        int port = params.containsKey("-p") ? Integer.parseInt(params.get("-p")) : -1;
        String location = params.containsKey("-l") ? params.get("-l") : "server";
        String remoteFolder = params.get("-folder");

        // Validate required parameters:
        if (server == null || port < 0 || remoteFolder == null) {
            logger.warning("Main", "handleClientDeleteFolder",
                    "Usage: client deletefolder -server <ip> -p <port> -l [cloud|edge] -folder <folder>");
            return;
        }
        Map<String, String> clientParams = new HashMap<>();
        clientParams.put("server", server);
        clientParams.put("port", String.valueOf(port));
        clientParams.put("location", location);
        // clientParams.put("remote", remoteFolder);

        String[] clientArgs = buildClientArgs(clientParams);
        Client client = new Client(clientArgs);

        try {
            client.deleteFolder(remoteFolder);
            logger.success("Main", "handleClientDeleteFolder",
                    "Deleted " + remoteFolder + " successfully");
        } catch (IOException e) {
            logger.error("Main", "handleClientDeleteFolder", "Error deleting folder: " + e.getMessage());
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

    // TODO: Is createDirectories() necessary?
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

    // TODO: Update this to be more specific
    // -------------------------------------------------------------------------
    // printUsage(): Quick usage instructions for ComFaaS
    // -------------------------------------------------------------------------
    private static void printUsage() {
        System.err.println("Invalid arguments. Usage:");
        System.err.println();
        System.err.println("  Server Commands:");
        System.err.println("    server init");
        System.err.println("        - Initialize server directories and verify Python virtual environment.");
        System.err.println("    server [cloud|edge (only server for now)] [-p <port>]");
        System.err
                .println("        - Run the server on the specified port. (Ensure you have run 'server init' first.)");
        System.err.println();
        System.err.println("  Client Commands:");
        System.err.println("    client init");
        System.err.println("        - Initialize client directories and verify Python virtual environment.");
        System.err.println(
                "    client runtask -server <ip> -p <port> -l [cloud|edge] -lang <language> -tn <program> -np <count>");
        System.err.println("        - Execute a remote task on the server.");
        System.err.println(
                "    client upload -server <ip> -p <port> -l [cloud|edge] -local <localFile> -remote <remoteFolder>");
        System.err.println("        - Upload a file to the server.");
        System.err
                .println(
                        "    client download -server <ip> -p <port> -l [cloud|edge] -remote <folder> -file <filename> -local <folder>");
        System.err.println("        - Download a file from the server.");
        System.err.println("    client listfiles -server <ip> -p <port> -l [cloud|edge] -dir <remoteFolder>");
        System.err.println("        - List files in a remote folder.");
        System.err.println(
                "    client deletefile -server <ip> -p <port> -l [cloud|edge] -folder <remoteFolder> -file <filename>");
        System.err.println("        - Delete a file on the server.");
        System.err.println(
                "    client uploadfolder -server <ip> -p <port> -l [cloud|edge] -local <localFolder> -remote <remoteFolder>");
        System.err.println("        - Upload a folder to the server.");
        System.err.println(
                "    client downloadfolder -server <ip> -p <port> -l [cloud|edge] -remote <folder> -local <folder>");
        System.err.println("        - Download a folder from the server.");
        System.err.println("    client deletefolder -server <ip> -p <port> -l [cloud|edge] -folder <folder>");
        System.err.println("        - Delete a folder on the server.");
        System.exit(1);
    }

    // Verifies that all given directories exist. If any is missing,
    // logs an error and exits, instructing the user to run init again.
    public static void verifyDirectories(Path[] directories, String context) {
        for (Path dir : directories) {
            if (!Files.exists(dir)) {
                logger.error("Main", context,
                        "Directory " + dir + " does not exist. Please run the init command first.");
                System.exit(1);
            }
        }
    }

    /// Verify files exists
    // -------------------------------------------------------------------------
    public static void verifyFiles(File[] files, String context) {
        for (File file : files) {
            if (!file.exists()) {
                logger.error("Main", context,
                        "File " + file + " does not exist. Please run the init command first.");
                System.exit(1);
            }
        }
    }

    // Converts command-line arguments (starting at startIndex) into a map of
    // flag-value pairs.
    // For example, given args: {"-server", "1.2.3.4", "-p", "1234"} starting at
    // index 2,
    // it will return a map with keys "-server" and "-p" mapped to their values.

    private static Map<String, String> parseArguments(String[] args, int startIndex) {
        Map<String, String> params = new HashMap<>();
        for (int i = startIndex; i < args.length - 1; i += 2) {
            params.put(args[i], args[i + 1]);
        }
        return params;
    }

    // buildClientArgs():
    // Constructs a client argument array from the given parameters.
    // Expects keys: "server", "port", "location"; optionally "lang", "tn", "np".

    private static String[] buildClientArgs(Map<String, String> params) {
        List<String> argsList = new ArrayList<>();
        argsList.add("-server");
        argsList.add(params.get("server"));
        argsList.add("-p");
        argsList.add(params.get("port"));
        argsList.add("-l");
        argsList.add(params.get("location"));
        // Add task-specific arguments only if they exist.
        if (params.containsKey("lang")) {
            argsList.add("-lang");
            argsList.add(params.get("lang"));
        }
        if (params.containsKey("tn")) {
            argsList.add("-tn");
            argsList.add(params.get("tn"));
        }
        if (params.containsKey("np")) {
            argsList.add("-np");
            argsList.add(params.get("np"));
        }
        return argsList.toArray(new String[0]);

    }

    public static String FindFolder(String targetName) {
        String projectRoot = getProjectRoot();
        if (projectRoot == null) {
            System.out.println("[ERROR] Could not determine project root.");
            return null;
        }

        File projectDir = new File(projectRoot);
        File result = searchRecursively(projectDir, targetName);
        return (result != null) ? result.getAbsolutePath() : null;
    }

    public static String getProjectRoot() {
        File currentDir = new File(System.getProperty("user.dir"));

        // Keep moving up until we find a known project identifier
        while (currentDir != null) {
            if (new File(currentDir, "ComFaaS.Jar").exists() || // Check for Maven projects
                    new File(currentDir, ".gitignore").exists()) { // Check for src/ directory
                return currentDir.getAbsolutePath();
            }
            currentDir = currentDir.getParentFile(); // Move up one level
        }

        return null; // Project root not found
    }

    private static File searchRecursively(File dir, String targetName) {
        File[] files = dir.listFiles();
        if (files == null)
            return null; // Handle permission issues

        for (File file : files) {
            if (file.isDirectory() && file.getName().equalsIgnoreCase(targetName)) {
                return file;
            }
            if (file.isDirectory()) {
                File found = searchRecursively(file, targetName);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

}
