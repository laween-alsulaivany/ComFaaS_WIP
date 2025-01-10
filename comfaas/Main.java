// Main.java
package comfaas;


import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;


// import java.io.DataOutputStream;
// import java.io.File;
// import java.io.FileOutputStream;
// import java.net.Socket;  
// import java.nio.file.attribute.DosFileAttributes;
// import java.util.concurrent.ThreadPoolExecutor;


// -------------------------
// * The Main class serves as the entry point for the ComFaaS application.
// * It handles command-line arguments and invokes the appropriate functionality
// * for server and edge operations.
// -------------------------
public class Main {
    // Logging
    private static final Logger edge_logger = new Logger("edge_logs.csv");
    private static final Logger server_logger = new Logger("server_logs.csv");
    
    
    // Properties for config
    private static final Properties configProps = new Properties();


    // -------------------------
    // The main method processes the initial command-line arguments
    // and directs the program flow based on the first argument.
    // -------------------------
    public static void main(String[] args) throws InterruptedException {
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

    // -------------------------
    //  Loads config from comfaas.properties, if present.
    // -------------------------
    private static void loadProperties() {
        Path configPath = Paths.get("comfaas.properties");

            if (Files.exists(configPath)) {
                try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
                    configProps.load(fis);
                    System.out.println("Loaded config from comfaas.properties");
                } catch (IOException e) {
                    System.err.println("Error loading config file: " + e.getMessage());
                }
            } else {
                System.out.println("No comfaas.properties file found; using defaults.");
            }
    }

    // -------------------------
    // * SERVER RELATED COMMANDS
    // -------------------------

    // -------------------------
    // Processes server-related commands: initialization and running.
    // -------------------------

    private static void handleServerCommands(String[] args) throws InterruptedException {
            switch (args[1]) {
                case "init"  -> initializeServer();
                case "cloud" -> runCloudServer(args);
                case "edge"  -> runEdgeServer(args);
                default      -> printUsage();
            }
    }

    // -------------------------
    // Initializes the server by creating necessary directories
    // and setting up a Python virtual environment.
    // -------------------------
    private static void initializeServer() {
        server_logger.logNetwork("Server initializing...");

        // Directory paths
        Path[] directories = {
            Paths.get("ServerPrograms"),
            Paths.get("ServerInput"),
            Paths.get("ServerOutput")
        };
            try {
                // Check directories exist
                createDirectories(directories);
                System.out.println("Server directories verified.");
                server_logger.logInfo("Server directories verified.");

                // Verify virtual environment setup
                Path venvPath = Paths.get(".serverVenv");
                if (!Files.exists(venvPath)) {
                    throw new IOException("Python virtual environment not found. Run 'setup_environment.sh' first.");
                }
                server_logger.logInfo("Python virtual environment verified.");

                // Log success
                server_logger.logSuccess("Server environment initialized.");
                server_logger.log("INFO", "Server environment initialized.", "ServerInit", "Initialization", -1);
            }
            catch (IOException e) {
                System.err.println("Error initializing server: " + e.getMessage());
                server_logger.logError(e.getMessage(), "ServerInit", "Initialization");
                System.exit(1);
                }
    }

    // -------------------------
    // Starts the cloud server by creating necessary directories
    // and initiating the server socket.
    // -------------------------
    private static void runCloudServer(String[] args) throws InterruptedException {
        // Default port from config or fallback to 12353
        int defaultCloudPort = Integer.parseInt(configProps.getProperty("cloud.server.port", "12353"));
        int port = defaultCloudPort;
            // Overwrite if user passes -p
            for (int i = 2; i < args.length; i++) {
                if ("-p".equals(args[i]) && i + 1 < args.length) {
                    port = Integer.parseInt(args[++i]);
                }
            }

        server_logger.logNetwork("Cloud server starting on port " + port);
        server_logger.log("INFO", "Cloud server started.", "ServerRun", "Initialization", -1);

        // Directory paths
        Path[] directories = {
            Paths.get("ServerPrograms"),
            Paths.get("ServerInput"),
            Paths.get("ServerOutput")
        };

        try {
            createDirectories(directories);
            CloudServer.run(port);
        } catch (IOException e) {
            server_logger.logError("Error running cloud server: " + e.getMessage());
            System.exit(1);
        }
    }

        // -------------------------
        // Starts the edge server by creating necessary directories and
        // initiating the server socket.
        // -------------------------

    private static void runEdgeServer(String[] args) throws InterruptedException {
            // Default port from config or fallback to 12354
            int defaultEdgePort = Integer.parseInt(configProps.getProperty("edge.server.port", "12354"));
            int port = defaultEdgePort;
            // Overwrite if user passes -p
            for (int i = 2; i < args.length; i++) {
                if ("-p".equals(args[i]) && i + 1 < args.length) {
                    port = Integer.parseInt(args[++i]);
                }
            }

        server_logger.logNetwork("Edge server starting on port " + port);
        server_logger.log("INFO", "Edge server started.", "ServerRun", "Initialization", -1);

        // Directory paths
        Path[] directories = {
            Paths.get("EdgePrograms"),
            Paths.get("EdgeInput"),
            Paths.get("EdgeOutput")
            };

            try {
                createDirectories(directories);
                EdgeServer.run(port);
            } catch (IOException e) {
                System.err.println("Error running edge server: " + e.getMessage());
                server_logger.logError("Error running edge server: " + e.getMessage());
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
                // case "run" -> runEdge(args);
                case "remotetask" -> handleEdgeRemoteTask(args);
                case "upload" -> handleEdgeUpload(args);
                case "download" -> handleEdgeDownload(args);
                case "listfiles" -> handleEdgeListFiles(args);
                case "deletefile" -> handleEdgeDeleteFile(args);
                default -> printUsage();
            }
    }

    // -------------------------
    // Initializes the edge environment by creating necessary directories
    // and setting up a Python virtual environment.
    // -------------------------
    private static void initializeEdge() {
        edge_logger.logInfo("Edge initializing...");

        // Directory paths
        Path[] directories = {
            Paths.get("EdgePrograms"),
            Paths.get("EdgeInput"),
            Paths.get("EdgeOutput")
            };


            try {
                createDirectories(directories);
                edge_logger.logInfo("Edge directories verified.");

                Path venvPath = Paths.get(".edgeVenv");
                if (!Files.exists(venvPath)) {
                    throw new IOException("Python virtual environment not found. Run 'setup_environment.sh' first.");
                }
                edge_logger.logInfo("Python virtual environment verified.");

                edge_logger.logSuccess("Edge environment initialized.");
                edge_logger.log("INFO", "Edge environment initialized.", "EdgeInit", "Initialization", -1);
            } catch (IOException e) {
                edge_logger.logError("Error initializing edge: " + e.getMessage());
                System.exit(1);
        }
    }

    // -------------------------
    // Starts the edge client by initializing it with the given arguments
    // and executing the requested task.
    // THIS IS A LOCAL CONNECTION
    // -------------------------

    // private static void runEdge(String[] args) {
    //     System.out.println("Starting edge client...");
    //     edge_logger.log("INFO", "Edge client started.", "EdgeRun", "Initialization", -1);

    //     // Pass the rest of the arguments to EdgeClient
    //     EdgeClient edge = new EdgeClient(java.util.Arrays.copyOfRange(args, 2, args.length));

    //         try {
    //             edge.runTask(); // executes the "executeTask" command
    //         } catch (IOException e) {
    //             System.err.println("Error running edge task: " + e.getMessage());
    //             edge_logger.logError(e.getMessage(), "EdgeRun", "Initialization");
    //             }
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
        String location = null;    // "cloud" or "edge"
        String lang = null;        // "c", "python", "java", etc.
        String programName = null; // the .c / .py / .java file
        int np = 1;
    
        // skip args[0]="edge", args[1]="remotetask", start from i=2
        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "-server" -> server = args[++i];
                case "-p" -> port = Integer.parseInt(args[++i]);
                case "-t" -> location = args[++i];   // "cloud" or "edge"
                case "-lang" -> lang = args[++i];
                case "-tn" -> programName = args[++i];
                case "-np" -> np = Integer.parseInt(args[++i]);
            }
        }
    
        if (server == null || port < 0 || location == null 
            || lang == null || programName == null) {
            edge_logger.logWarning("Usage: edge remotetask -server <ip> -p <port> -t [cloud|edge] -lang <language> -tn <program> -np <processCount>");
            return;
        }
    
        // create the EdgeClient
        String[] clientArgs = {
            "-server", server,
            "-p", String.valueOf(port),
            "-t", "edge",      // not used for local run
            "-np", String.valueOf(np),
            "-tn", programName,
            "-lang", lang
        };
        EdgeClient client = new EdgeClient(clientArgs);
    
        try {
            // call the new runRemoteTask
            client.runRemoteTask(location, lang, programName, np);
            edge_logger.logSuccess("Remote task complete");
        } catch (IOException e) {
            edge_logger.logError("Error executing remote task: " + e.getMessage());
        } finally {
            try {
                client.close();
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
    
        // parse arguments
        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "-server" -> server = args[++i];
                case "-p" -> port = Integer.parseInt(args[++i]);
                case "-local" -> localFile = args[++i];
                case "-remote" -> remoteFolder = args[++i];
            }
        }
    
        if (server == null || port < 0 || localFile == null || remoteFolder == null) {
            edge_logger.logWarning("Usage: edge upload -server <ip> -p <port> -local <localFile> -remote <remoteFolder>");
            return;
        }
    
        // Create EdgeClient with dummy parameters for the rest
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
            edge_logger.logSuccess("Uploaded " + localFile + " to " + remoteFolder + "Successfully");
        } catch (IOException e) {
            edge_logger.logError("Error uploading file: " + e.getMessage());
        } finally {
            try {
                client.close();
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
            edge_logger.logWarning("Usage: edge download -server <ip> -p <port> -remote <folder> -file <filename> -local <folder>");
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
            edge_logger.logSuccess("Downloaded " + fileName + " from " + remoteFolder + " to " + localFolder + " successfully");
        } catch (IOException e) {
            edge_logger.logError("Error downloading file: " + e.getMessage());
        } finally {
            try {
                client.close();
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
            edge_logger.logWarning("Usage: edge listfiles -server <ip> -p <port> -dir <remoteFolder>");
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
            edge_logger.logInfo("Listing files in " + remoteFolder + " on server " + server + ":" + port + "...");
            client.listFilesOnServer(remoteFolder);
        } catch (IOException e) {
            edge_logger.logError("Error listing files: " + e.getMessage());
        } finally {
            try {
                client.close();
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
            edge_logger.logWarning("Usage: edge deletefile -server <ip> -p <port> -folder <remoteFolder> -file <filename>");
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
            edge_logger.logSuccess("Deleted " + fileName + " from folder " + folder + " on " + server + ":" + port + " successfully");
        } catch (IOException e) {
            edge_logger.logError("Error deleting file: " + e.getMessage());
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
    
    // -------------------------
    // * UTILITY FUNCTIONS
    // -------------------------

    // -------------------------
    // Creates directories if they do not exist.
    // -------------------------
    private static void createDirectories(Path[] directories) throws IOException {
        for (Path directory : directories) {
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
                edge_logger.logInfo("Created directory: " + directory);
            }
        }
    }
    // -------------------------
    // Prints the usage instructions for the ComFaaS application.
    // -------------------------
    private static void printUsage() {
        System.err.println("Invalid arguments. Usage:");
        System.err.println(" - Server Init: server init");
        System.err.println(" - Server Run: server run");
        System.err.println(" - Edge Init: edge init");
        System.err.println(" - Edge Run: edge run <options>");
        System.err.println(" - Edge Send: edge send -tn <fileName>");
        System.err.println(" - Edge Request: edge request -tn <fileName>");
        System.err.println(" - Edge Delete: edge delete -tn <fileName>");
        System.err.println(" - Edge List: edge list -dir <directory>");
    }

}
