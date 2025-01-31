// EdgeClient.java

package comfaas;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

// ------------------------------------------
// * The EdgeClient class represents an edge node acting as a client
// * to a remote server (e.g., the Cloud server).
// * It connects to the server, sends tasks/files, and can run tasks
// * on the Edge or on the Cloud.
// ------------------------------------------

public class EdgeClient extends CoreOperations {
    // Logging
    private static final Logger logger = new Logger(Main.LogFile);

    // ------------------
    // Command-line arguments and task properties
    // ------------------
    public String server           = null;   // e.g., "localhost" or "1.2.3.4"
    public Integer port            = -1;
    public String type             = null;   // "cloud" or "edge"
    public Integer np              = -1;
    public Integer tid             = -1;     // Not used yet, but included
    public String tn               = null;   // task name
    public boolean kFlag           = false;
    public String lang             = null;
    public String[] args;
    public String sourceFolder     = null;
    public String destinationFolder= null;


    // ------------------------------------------
    // Constructor that takes in command-line args and connects to the server.
    // ------------------------------------------
    public EdgeClient(String[] args) {
        logger.info("EdgeClient", "Constructor", "Edge client initialized.");

        // Basic argument check
        if (args.length < 6) {
            logger.warning("EdgeClient", "Constructor", "Invalid arguments. Required at least: "
                    + "-server [host], -p [port], -t [cloud/edge], "
                    + "-np [#processes], -tn [taskname], -lang [language]");
            System.exit(1);
        }

        // Parse arguments
        this.args = args;
        CommandLineProcessor.processArguments(this);

        // Validate directories (Edge side)
        String[] requiredDirs = {edgeInputFolder, edgeOutputFolder, edgeProgramsFolder};
        for (String dir : requiredDirs) {
            File directory = new File(dir);
            if (!directory.exists() && !directory.mkdirs()) {
                logger.error("EdgeClient", "Constructor", "Failed to create directory: " + dir);
                System.exit(1);
            }
        }

        // Connect to the server
        try {
            logger.network("EdgeClient", "Constructor", "Connecting to server at " + this.server + ":" + this.port);

            // Create the socket
            this.socket = new Socket(this.server, this.port);

            // Create dis/dos from the socket
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());

            logger.success("EdgeClient", "Constructor", "Edge client successfully connected to the server.");

        } catch (UnknownHostException e) {
            logger.error("EdgeClient", "Constructor", "Unknown host: " + this.server);
            System.exit(1);
        } catch (IOException e) {
            logger.error("EdgeClient", "Constructor", "Error connecting to server: " + e.getMessage());
            System.exit(1);
        }
    }
    // ------------------------------------------
    // Executes the specified task on either the edge or the cloud,
    // based on 'type' ("edge"/"cloud"). Then closes connection.
    // THIS IS A LOCAL CONNECTION
    // ------------------------------------------
    // public void runTask() throws IOException {
    //     long startTime = System.currentTimeMillis();
    //     System.out.println("************************");
    //     System.out.println("Task: " + this.tn + " (" + this.type + ", " + this.lang + ", " + this.np + " Processes)");
    //     System.out.println("************************");
    //     edge_logger.log("INFO", "Task execution started", this.tn, this.type, startTime);

    //     try {
    //         switch (this.type) {
    //             case "cloud" -> {
    //                 System.out.println("[INFO] Executing task on the cloud...");
    //                 edge_logger.log("INFO", "Executing on cloud", this.tn, this.type, -1);
    //                 // runs *locally*, from the perspective of your code
    //                 // but for a *real* remote run, you'd do:
    //                 // -> send "executeTask" command to the server...
    //                 runProgramOnCloud(this.lang, this.tn, this.np);
    //                 System.out.println("[SUCCESS] Task completed on the cloud.");
    //                 edge_logger.log("INFO", "Cloud task completed successfully", this.tn, this.type, -1);
    //             }
    //             case "edge" -> {
    //                 System.out.println("[INFO] Executing task locally on the edge...");
    //                 edge_logger.log("INFO", "Executing locally on edge", this.tn, this.type, -1);
    //                 runProgramOnEdge(this.lang, this.tn, this.np);
    //                 System.out.println("[SUCCESS] Task completed locally on the edge.");
    //                 edge_logger.log("INFO", "Local task completed successfully", this.tn, this.type, -1);
    //             }
    //             default -> {
    //                 System.err.println("[ERROR] Invalid client type: " + this.type);
    //                 edge_logger.logError("Invalid client type", this.tn, this.type);
    //                 System.exit(1);
    //             }
    //         }
    //     } catch (IOException e) {
    //         System.err.println("[ERROR] Task execution failed: " + e.getMessage());
    //         edge_logger.logError("Task execution failed: " + e.getMessage(), this.tn, this.type);
    //         throw e;
    //     } finally {
    //         long endTime = System.currentTimeMillis();
    //         System.out.println("Task execution finished in " + (endTime - startTime) + " ms.");
    //         this.close();
    //     }
    // }


    // ------------------------------------------
    // Executes the specified task on either the edge or the cloud,
    // based on 'type' ("edge"/"cloud"). Then closes connection.
    // THIS IS A REMOTE CONNECTION
    // ------------------------------------------

    public void runRemoteTask(String location, String language, String programName, int np) throws IOException {
        // 1) Send the command
        dos.writeUTF("executeTask");
        logger.info("EdgeClient", "runRemoteTask", "Executing task remotely: " +
                "Location=" + location + ", Language=" + language + ", Program=" + programName + ", Processes=" + np);
        // 2) Send parameters
        dos.writeUTF(location);     // "cloud" or "edge"
        dos.writeUTF(language);     // e.g. "c" or "python"
        dos.writeUTF(programName);  // e.g. "WaitFor3Seconds.c"
        dos.writeInt(np);           // number of processes

        // 3) Read the server response
        String response;
        try {
            response = dis.readUTF(); 
        } catch (EOFException e) {
            throw new IOException("Server closed connection unexpectedly", e);
        }
        if (response.startsWith("ERR:")) {
            throw new IOException("Remote execution error: " + response);
        }
        logger.network("EdgeClient", "runRemoteTask", "Server response: " + response);
        // expected: "executeTaskComplete" or "OK: Task executed successfully"
        dos.writeUTF("done");
        dos.flush();

    }

}

