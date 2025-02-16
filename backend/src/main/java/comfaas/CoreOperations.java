// CoreOperations.java
package comfaas;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import comfaas.Logger.LogLevel;
import comfaas.theAlgoTools.ProcessCpuUsage;
import comfaas.theAlgoTools.ProcessMemoryUsage;

// ------------------------------------------
// * CoreOperations class provides utility functions for file and folder operations,
// * task execution, and communication between the server and the client.
// ------------------------------------------
public class CoreOperations {

    // Streams & Socket
    protected Socket socket;
    protected DataInputStream dis;
    protected DataOutputStream dos;

    // Root directory
    private static final Path ROOT = Main.getRootDir();

    // Algorithm
    protected AbstractAlgo algo;

    // Folder names for Client vs. Server
    public String clientInputFolder = ROOT.resolve("client").resolve("Input").toString();
    public String clientOutputFolder = ROOT.resolve("client").resolve("Output").toString();
    public String clientProgramsFolder = ROOT.resolve("client").resolve("Programs").toString();
    public String clientVenv = ROOT.resolve(".clientVenv").toString();

    public String serverInputFolder = ROOT.resolve("server").resolve("Input").toString();
    public String serverOutputFolder = ROOT.resolve("server").resolve("Output").toString();
    public String serverProgramsFolder = ROOT.resolve("server").resolve("Programs").toString();
    public String serverVenv = ROOT.resolve(".serverVenv").toString();

    // Logging
    private static final Logger logger = new Logger(Main.getLogFile());

    // ------------------------------------------
    // Server-Side: Manage incoming requests
    // ------------------------------------------

    public void manageRequests() throws IOException, InterruptedException {
        while (true) {
            String command;
            try {
                command = dis.readUTF();
            } catch (EOFException eof) {
                logger.logEvent(LogLevel.WARNING, "Server", "manageRequests",
                        "EOF encountered - client disconnected unexpectedly?", 0, -1);
                break;
            }

            if ("done".equals(command)) {
                logger.logEvent(LogLevel.INFO, "Server", "manageRequests",
                        "Client sent 'done'. Exiting request management.", 0, -1);
                break;
            } else if ("shutdownServer".equals(command)) {
                logger.logEvent(LogLevel.INFO, "Server", "manageRequests",
                        "Received shutdown command from client.", 0, -1);
                Server.setShutdownFlag();
                break;
            } else {
                switch (command) {
                    case "heartbeat" -> {

                        handleHeartbeat();
                        // break;
                    }
                    case "uploadSingleFile" -> handleUploadSingleFile();
                    case "downloadSingleFile" -> handleDownloadSingleFile();
                    case "deleteSingleFile" -> handleDeleteSingleFile();
                    case "uploadFolder" -> handleUploadFolder();
                    case "downloadFolder" -> handleDownloadFolder();
                    case "deleteFolder" -> handleDeleteFolder();
                    case "listFiles" -> handleListFiles();
                    case "runTask" -> handleExecuteTask();
                    default -> logger.logEvent(LogLevel.WARNING, "Server", "manageRequests",
                            "Unknown command received: " + command, 0, -1);
                }
            }
        }
    }

    /**
     * If the client says "heartbeat", this method will be called to handle the
     * request.
     */

    private void handleHeartbeat() throws IOException {
        logger.logEvent(LogLevel.INFO, "CoreOperations", "handleHeartbeat", "Received heartbeat", 0, -1);
        try {
            // Read the node type (e.g., "edge" or "cloud") and the sender's IP address
            String nodeType = dis.readUTF();
            String senderIp = dis.readUTF();

            // For an edge, store its info in the registry (using the remote port from the
            // socket)
            if ("edge".equalsIgnoreCase(nodeType)) {
                int port = socket.getPort();
                EdgeInfo info = new EdgeInfo(-1, senderIp, port);
                Server.storeEdgeInfo(info);
                logger.logEvent(LogLevel.INFO, "CoreOperations", "handleHeartbeat",
                        "Stored edge info: " + senderIp + ":" + port, 0, -1);
            } else if ("cloud".equalsIgnoreCase(nodeType)) {
                logger.logEvent(LogLevel.INFO, "CoreOperations", "handleHeartbeat",
                        "Received heartbeat from cloud: " + senderIp, 0, -1);
            }

            dos.writeUTF("OK");
            dos.flush();
        } catch (Exception e) {
            logger.logEvent(LogLevel.ERROR, "CoreOperations", "handleHeartbeat",
                    "Error handling heartbeat: " + e.getMessage(), 0, -1);
            dos.writeUTF("ERR: " + e.getMessage());
            dos.flush();
        }
    }

    // ---------------------------------------------------------
    // * Single File: Upload / Download / Delete
    // ---------------------------------------------------------

    // ------------------------------------------
    // Handles sending a file to the client's input folder.
    // ------------------------------------------
    private void handleUploadSingleFile() throws IOException {
        // Read the destination folder and file name
        String destinationFolder = dis.readUTF();
        String fileName = dis.readUTF();
        long fileSize = dis.readLong();

        // Verify the destination directory
        File folder = new File(destinationFolder);
        Main.verifyDirectories(new Path[] { folder.toPath() }, "CoreOperations.handleUploadSingleFile()");

        // Create the output file
        File outFile = new File(folder, fileName);
        // if ("edge".equalsIgnoreCase(Main.serverType)) {
        // forwardFileToCloud(outFile);
        // }
        // Write the file to the output stream
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[4096];
            long remaining = fileSize;
            int bytesRead;
            while (remaining > 0 && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                fos.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }

            logger.logEvent(LogLevel.SUCCESS, "CoreOperations", "handleUploadSingleFile",
                    "File uploaded: " + outFile.getAbsolutePath(), 0, fileSize);
            dos.writeUTF("File uploaded successfully");

        } catch (IOException e) {
            logger.logEvent(LogLevel.ERROR, "CoreOperations", "handleUploadSingleFile",
                    "Error uploading file: " + e.getMessage(), 0, -1);
        }

    }

    // ------------------------------------------
    // Handles downloading a file from the client's input folder.
    // ------------------------------------------
    private void handleDownloadSingleFile() throws IOException {
        // Read the source folder and file name
        String sourceFolder = dis.readUTF();
        String fileName = dis.readUTF();

        // Create the source file and verify the directory
        File inFile = new File(sourceFolder, fileName);
        Main.verifyDirectories(new Path[] { inFile.toPath() }, "CoreOperations.handleDownloadSingleFile()");

        // Write the file to the output stream
        try (FileInputStream fis = new FileInputStream(inFile)) {
            dos.writeUTF("File found");
            dos.writeLong(inFile.length());
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
        }
        dos.flush();
        logger.logEvent(LogLevel.SUCCESS, "CoreOperations", "handleDownloadSingleFile",
                "File downloaded: " + inFile.getAbsolutePath(), 0, inFile.length());
    }

    // TODO: apparently there is an issue with the file path, it is directing to the
    // correct path but it is not detecting the file. debug this
    // ------------------------------------------
    // Handles deleting a file from the client's input folder.
    // ------------------------------------------
    private void handleDeleteSingleFile() throws IOException {
        // Read the source folder and file name
        String sourceFolder = dis.readUTF();
        String fileName = dis.readUTF();
        System.out.println("sourceFolder: " + sourceFolder);
        System.out.println("fileName: " + fileName);

        // Create the source file
        // File fileToDelete = new File(sourceFolder, fileName);
        // System.out.println("fileToDelete: " + fileToDelete);
        Path filePath = Paths.get(sourceFolder, fileName);
        File fileToDelete = filePath.toFile();
        // Main.verifyDirectories(new Path[] { filePath },
        // "CoreOperations.handleDeleteSingleFile()");
        // TODO: IT CANNOT SEE THE DIRECTORY, FIX IT
        Main.verifyDirectories(new Path[] { Paths.get(sourceFolder) }, "CoreOperations.handleDeleteSingleFile()");
        Main.verifyFiles(new File[] { fileToDelete }, "CoreOperations.handleDeleteSingleFile()");
        // System.out.println("fileToDelete: " + fileToDelete);

        // if (!Files.exists(filePath)) {
        // logger.logEvent(LogLevel.ERROR, "CoreOperations", "handleDeleteSingleFile",
        // "File not found: " + fileToDelete.getAbsolutePath(), 0, -1);
        // dos.writeUTF("File not found");
        // return;
        // }
        // // Check if the file exists
        // if (!fileToDelete.exists()) {
        // logger.logEvent(LogLevel.ERROR, "CoreOperations", "handleDeleteSingleFile",
        // "File not found: " + fileToDelete, 0, -1);
        // dos.writeUTF("File not found");
        // return;
        // // if (!fileToDelete.exists()) {
        // // logger.logEvent(LogLevel.ERROR, "CoreOperations",
        // "handleDeleteSingleFile",
        // // "File not found: " + fileToDelete.getAbsolutePath(), 0, -1);
        // // dos.writeUTF("File not found");
        // // return;
        // }

        // Delete the file
        if (fileToDelete.delete()) {
            logger.logEvent(LogLevel.SUCCESS, "CoreOperations", "handleDeleteSingleFile",
                    "File deleted: " + fileToDelete.getAbsolutePath(), 0, -1);
            dos.writeUTF("File deleted successfully");
        } else {
            logger.logEvent(LogLevel.ERROR, "CoreOperations", "handleDeleteSingleFile",
                    "Error deleting file: " + fileToDelete.getAbsolutePath(), 0, -1);
            dos.writeUTF("Error deleting file");
        }
    }

    // ------------------------------------------
    // Handles listing files in a given directory.
    // ------------------------------------------
    private void handleListFiles() throws IOException {

        try {
            // Read the folder name from the client
            String sourceFolder = dis.readUTF();

            // Validate the directory
            if (sourceFolder == null || sourceFolder.isEmpty()) {
                logger.logEvent(LogLevel.ERROR, "CoreOperations", "handleListFiles",
                        "Invalid directory: null or empty", 0, -1);
                dos.writeUTF("ERR: Directory path is null or empty");
                return;
            }

            File directory = new File(sourceFolder);

            // If directory doesn't exist or isn't a directory, notify client
            if (!directory.exists() || !directory.isDirectory()) {
                dos.writeUTF("ERR: " + sourceFolder + " does not exist or is not a directory");
                logger.logEvent(LogLevel.ERROR, "CoreOperations", "handleListFiles",
                        "Directory not found: " + sourceFolder, 0, -1);
                return; // stop here
            }

            // Send an initial "OK" response so the client knows it can read filenames
            dos.writeUTF("OK: listing files");

            // List the files in that directory
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    dos.writeUTF(file.getName());
                }
            }

            // Finally, send "End of list"
            dos.writeUTF("End of list");
            logger.logEvent(LogLevel.INFO, "CoreOperations", "handleListFiles",
                    "File listing sent for directory: " + sourceFolder, 0, -1);
        } catch (IOException e) {
            logger.logEvent(LogLevel.ERROR, "CoreOperations", "handleListFiles",
                    "Error during file listing: " + e.getMessage(), 0, -1);
            dos.writeUTF("ERR: Exception occurred during file listing");
        }

    }

    // ------------------------------------------
    // Handles executing a task on the client.
    // ------------------------------------------
    // private void handleExecuteTask() throws IOException, InterruptedException {
    // // Read task parameters from the client.
    // // String location = dis.readUTF(); // e.g., "server"
    // // String language = dis.readUTF();
    // // String programName = dis.readUTF();
    // // int np = dis.readInt();

    // // Compute a snapshot of pre-task metrics.
    // double startCpu =
    // comfaas.theAlgoTools.ProcessCpuUsage.getProcessCpuLoadPercentage();
    // double startMem = comfaas.theAlgoTools.ProcessMemoryUsage.getUsedMemoryMB();
    // File programFile = new File(serverProgramsFolder, programName);
    // double fileSize = programFile.exists() ? programFile.length() : 0;

    // // Optionally update the algorithm with pre-task metrics.
    // if (algo != null) {
    // algo.faasUpdate(programName, startCpu, startMem, fileSize);
    // } else {
    // logger.logEvent(LogLevel.WARNING, "CoreOperations", "handleExecuteTask",
    // "Algorithm instance is null. Skipping faasUpdate.", 0, -1);
    // }

    // // Immediately send acknowledgment to the client.
    // dos.writeUTF("Task started");
    // dos.flush();

    // // Now, run the task asynchronously so that the client isn't kept waiting.
    // ExecutorService taskExecutor = Executors.newSingleThreadExecutor();
    // taskExecutor.submit(() -> {
    // try {
    // // Execute the task.
    // if ("server".equalsIgnoreCase(location)) {
    // runProgramOnServer(language, programName, np);
    // if ("edge".equalsIgnoreCase(Main.serverType)) {
    // forwardFileToCloud(serverProgramsFolder, serverProgramsFolder, programName);
    // deleteLocalProgram(programName);
    // logger.logEvent(LogLevel.INFO, "CoreOperations", "handleExecuteTask",
    // "Edge server type was detected", 0, -1);
    // }
    // } else {
    // logger.logEvent(LogLevel.ERROR, "CoreOperations", "handleExecuteTask",
    // "Invalid location: " + location, 0, -1);
    // }
    // // Record post-task metrics.
    // double endCpu =
    // comfaas.theAlgoTools.ProcessCpuUsage.getProcessCpuLoadPercentage();
    // double endMem = comfaas.theAlgoTools.ProcessMemoryUsage.getUsedMemoryMB();
    // // Optionally, compute averages or differences here.
    // if (algo != null) {
    // // Update the algorithm with final per-task metrics.
    // algo.faasUpdate(programName, endCpu, endMem, fileSize);
    // }
    // logger.logEvent(LogLevel.INFO, "CoreOperations", "handleExecuteTask",
    // "Task completed asynchronously", 0, -1);
    // } catch (Exception ex) {
    // logger.logEvent(LogLevel.ERROR, "CoreOperations", "handleExecuteTask",
    // "Error executing task asynchronously: " + ex.getMessage(), 0, -1);
    // }
    // });
    // taskExecutor.shutdown();
    // // Do not wait for the task to complete—client already got the
    // acknowledgment.
    // }

    private void handleExecuteTask() throws IOException, InterruptedException {
        // System.out.println("handleExecuteTask we have recieved");
        String location = dis.readUTF(); // "server"
        String language = dis.readUTF();
        String programName = dis.readUTF();
        int np = dis.readInt();

        // Compute metrics to pass to the FaaS update:

        File programFile = new File(serverProgramsFolder, programName);
        double fileSize = programFile.exists() ? programFile.length() : 0;
        // double ProcessCpuUsage =
        // comfaas.theAlgoTools.ProcessCpuUsage.getProcessCpuLoadPercentage();
        // double ProcessMemoryUsage =
        // comfaas.theAlgoTools.ProcessMemoryUsage.getUsedMemoryMB();

        long startTime = System.nanoTime();
        long startCpuTime = ProcessCpuUsage.getProcessCpuTimeNanos();

        // Immediately send acknowledgment to the client.
        dos.writeUTF("Task started");
        dos.flush();

        ExecutorService taskExecutor = Executors.newSingleThreadExecutor();
        taskExecutor.submit(() -> {
            try {
                // Execute the task.
                if ("server".equalsIgnoreCase(location)) {
                    runProgramOnServer(language, programName, np);
                    if ("edge".equalsIgnoreCase(Main.serverType)) {
                        forwardFileToCloud(serverProgramsFolder, serverProgramsFolder, programName);
                        deleteLocalProgram(programName);
                        logger.logEvent(LogLevel.INFO, "CoreOperations", "handleExecuteTask",
                                "Edge server type was detected", 0, -1);
                    }
                } else {
                    logger.logEvent(LogLevel.ERROR, "CoreOperations", "handleExecuteTask",
                            "Invalid location: " + location, 0, -1);
                }
                logger.logEvent(LogLevel.INFO, "CoreOperations", "handleExecuteTask",
                        "Task completed asynchronously", 0, -1);
            } catch (Exception ex) {
                logger.logEvent(LogLevel.ERROR, "CoreOperations", "handleExecuteTask",
                        "Error executing task asynchronously: " + ex.getMessage(), 0, -1);
            }

        });

        taskExecutor.shutdown();
        // if (algo != null)

        // {
        // algo.faasUpdate(programName, ProcessCpuUsage, ProcessMemoryUsage, fileSize);
        // } else {
        // logger.logEvent(LogLevel.WARNING, "CoreOperations", "handleExecuteTask",
        // "Algorithm instance is null. Skipping faasUpdate.", 0, -1);
        // }

        long endTime = System.nanoTime();
        long endCpuTime = ProcessCpuUsage.getProcessCpuTimeNanos();
        long elapsedTime = endTime - startTime;
        int cores = Runtime.getRuntime().availableProcessors();

        double avgCpuTimeMethod = ProcessCpuUsage.averageCpuUsageUsingTime(startCpuTime, endCpuTime, elapsedTime,
                cores);
        System.out.println("Average CPU Usage by Time: " + avgCpuTimeMethod);
        System.out.println("   ");

        double avgCpuTimeSampling = ProcessCpuUsage.averageCpuUsageBySampling(5000, 500); // sample for 5 seconds every
        // 500ms
        System.out.println("Average CPU Usage by Sampling: " + avgCpuTimeSampling);
        System.out.println("   ");

        System.out.println("This is Max Memory Used");
        double maxMemoryUsed = ProcessMemoryUsage.maxMemoryUsageDuringPeriod(5000, 500); // sample for 5 seconds every
        // 500ms
        System.out.println("   ");
        System.out.println("====================================================");
        System.out.println("   ");
        System.out.println("Max Memory Used: " + maxMemoryUsed);
        System.out.println("   ");
        System.out.println("====================================================");
        System.out.println("   ");

        System.out.println("====================================================");
        System.out.println("Sampling Method");
        System.out.println("====================================================");
        algo.faasUpdate(programName, avgCpuTimeMethod, maxMemoryUsed, fileSize);
        System.out.println("====================================================");
        System.out.println("Time Method");
        System.out.println("====================================================");
        algo.faasUpdate(programName, avgCpuTimeMethod, maxMemoryUsed, fileSize);

    }

    // ---------------------------------------------------------
    // * Folder-based commands
    // ---------------------------------------------------------

    // ------------------------------------------
    // Handles uploading a folder to the client's input folder.
    // ------------------------------------------
    protected void handleUploadFolder() throws IOException {
        // 1) Folder name
        String destinationFolder = dis.readUTF();
        // 2) number of files
        int numFiles = dis.readInt();

        File folder = new File(destinationFolder);
        Main.verifyDirectories(new Path[] { folder.toPath() }, "CoreOperations.handleUploadFolder()");
        // if (!folder.exists())
        // folder.mkdirs();

        logger.logEvent(LogLevel.INFO, "CoreOperations", "handleUploadFolder",
                "Uploading " + numFiles + " file(s) to folder: " + destinationFolder, 0, -1);

        for (int i = 0; i < numFiles; i++) {
            // read file name
            String fileName = dis.readUTF();
            // read file size
            long fileSize = dis.readLong();
            File outFile = new File(folder, fileName);

            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[4096];
                long remaining = fileSize;
                int bytesRead;
                while (remaining > 0
                        && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    remaining -= bytesRead;
                }
            }

            logger.logEvent(LogLevel.SUCCESS, "CoreOperations", "handleUploadFolder",
                    "Uploaded file: " + outFile.getAbsolutePath(), 0, fileSize);
        }
        dos.writeUTF("Folder upload complete");
        dos.flush();
    }

    // ------------------------------------------
    // Handles downloading a folder from the client's input folder.
    // ------------------------------------------
    protected void handleDownloadFolder() throws IOException {
        String sourceFolder = dis.readUTF();
        File folder = new File(sourceFolder);
        if (!folder.isDirectory()) {
            dos.writeUTF("ERR: " + sourceFolder + " is not a directory");
            return;
        }
        File[] files = folder.listFiles();
        if (files == null)
            files = new File[0];

        // Send initial OK response and the number of files
        dos.writeUTF("OK");
        dos.writeInt(files.length);

        for (File f : files) {
            if (f.isFile()) {
                // Send file name
                dos.writeUTF(f.getName());
                // Send file size
                dos.writeLong(f.length());
                // Send file bytes
                try (FileInputStream fis = new FileInputStream(f)) {
                    byte[] buf = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buf)) != -1) {
                        dos.write(buf, 0, bytesRead);
                    }
                }
                dos.flush();
            }
        }
        // Send a termination message so that the client is not blocked
        dos.writeUTF("Folder download complete");
        dos.flush();
        logger.logEvent(LogLevel.SUCCESS, "CoreOperations", "handleDownloadFolder",
                "Folder downloaded: " + sourceFolder, 0, -1);
    }

    // ------------------------------------------
    // Handles deleting a folder from the client's input folder.
    // ------------------------------------------
    protected void handleDeleteFolder() throws IOException {
        String folderName = dis.readUTF();
        File folder = new File(folderName);
        if (!folder.exists() || !folder.isDirectory()) {
            dos.writeUTF("ERR: folder does not exist or not a directory");
            return;
        }

        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    f.delete();
                } else {
                    // TODO: optional recursive delete if subdirectories exist

                }
            }
        }
        boolean result = folder.delete();
        if (result) {
            dos.writeUTF("Folder deleted successfully");
            logger.logEvent(LogLevel.SUCCESS, "CoreOperations", "handleDeleteFolder",
                    "Deleted folder: " + folderName, 0, -1);
        } else {
            dos.writeUTF("ERR: Failed to delete folder");
            logger.logEvent(LogLevel.ERROR, "CoreOperations", "handleDeleteFolder",
                    "Failed to delete folder: " + folderName, 0, -1);
        }
        dos.flush();
    }

    // ------------------------------------------
    // * TASK EXECUTION
    // ------------------------------------------

    // ------------------------------------------
    // Run a program on the Server
    // ------------------------------------------

    public int runProgramOnServer(String language, String program, int np) throws IOException {
        // This code is basically identical to the Edge approach, but referencing the
        // 'serverProgramsFolder'.
        String filePath = serverProgramsFolder + "/" + program; // "ServerPrograms/mytask"
        File taskFile = new File(filePath);

        if (!taskFile.exists()) {
            throw new IOException("Server task file not found: " + filePath);
        }

        try {
            if (np == 1) {
                switch (language.toLowerCase()) {
                    case "python" -> PythonRunner.runPythonScriptInVenv(serverVenv, filePath);
                    case "java" -> {
                        JavaProgramRunner jRunner = new JavaProgramRunner();
                        if (jRunner.compileJavaProgram(filePath)) {
                            String className = jRunner.getClassName(program);
                            jRunner.runJavaProgram(className);
                        }
                    }
                    case "c" -> {
                        CProgramRunner cRunner = new CProgramRunner();
                        if (cRunner.compileCProgram(filePath)) {
                            cRunner.runCProgram(filePath);
                        }
                    }
                    default -> throw new IOException("Unsupported language: " + language);
                }
            } else if (np > 1) {
                // Multi-process approach
                switch (language.toLowerCase()) {
                    case "python" -> PythonRunner.runPythonScriptWithMpi(serverVenv, filePath, np);
                    case "c" -> {
                        CProgramRunner cRunner = new CProgramRunner();
                        if (cRunner.compileMPICHProgram(filePath)) {
                            cRunner.runMPICHProgram(filePath, np);
                        }
                    }
                    default -> throw new IOException("Unsupported language for multi-process: " + language);
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new IOException("Error executing task on Cloud: " + e.getMessage(), e);
        }
        return 0; // success
    }

    // ---------------------------------------------------------
    // * HELPER FUNCTIONS
    // ---------------------------------------------------------

    private void forwardFileToCloud(String localpath, String cloudpath, String programName) {
        // 1) figure out Cloud IP / port
        // e.g. from stored "cloudIP" or parse "Main.cliArgs"
        // Here we show a simplified approach:
        // String cloudIP = "127.0.0.1";
        // int cloudPort = 12353;

        String cloudIP = null;
        int cloudPort = -1;
        for (int i = 0; i < Main.cliArgs.length; i++) {
            if (Main.cliArgs[i].equals("-cloudIP") && i + 1 < Main.cliArgs.length) {
                cloudIP = Main.cliArgs[i + 1];
            }
            if (Main.cliArgs[i].equals("-cloudPort") && i + 1 < Main.cliArgs.length) {
                cloudPort = Integer.parseInt(Main.cliArgs[i + 1]);
            }
        }
        if (cloudIP == null) {
            try {
                cloudIP = java.net.InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                cloudIP = "127.0.0.1";
            }
        }
        if (cloudPort == -1) {
            cloudPort = 12353;
        }

        File localFile = Paths.get(localpath).resolve(programName).toFile();
        // 2) Connect to cloud
        try (Socket sock = new Socket(cloudIP, cloudPort);
                DataOutputStream cloudDos = new DataOutputStream(sock.getOutputStream());
                DataInputStream cloudDis = new DataInputStream(sock.getInputStream());
                FileInputStream fis = new FileInputStream(localFile)) {
            // 3) Send the command
            cloudDos.writeUTF("uploadSingleFile");

            cloudDos.writeUTF(cloudpath);
            cloudDos.writeUTF(localFile.getName());

            long fileSize = localFile.length();
            cloudDos.writeLong(fileSize);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                cloudDos.write(buffer, 0, bytesRead);
            }
            cloudDos.flush();

            // 5) Read response from the cloud
            String resp = cloudDis.readUTF();
            System.out.println("Forwarded file to Cloud. Response: " + resp);

        } catch (IOException e) {
            System.err.println("Failed to forward file to Cloud: " + e.getMessage());
        }
    }

    private void deleteLocalProgram(String programName) {
        // Suppose the Edge stored programs in "edgeProgramsFolder"
        String edgeProgramsFolder = Main.rootDir.resolve("server")
                .resolve("Programs")
                .toString();
        File fileToDelete = new File(edgeProgramsFolder, programName);
        if (fileToDelete.exists()) {
            if (fileToDelete.delete()) {
                System.out.println("Removed local copy of " + programName + " from Edge.");
            } else {
                System.err.println("Failed to remove local copy of " + programName);
            }
        }
    }

    // ---------------------------------------------------------
    // * Cleanup
    // ---------------------------------------------------------

    // ------------------------------------------
    // Close all streams and sockets
    // ------------------------------------------
    public void close(boolean logFromServer) throws IOException {
        // if (logFromServer) {
        // logger.logEvent(LogLevel.NETWORK, "CoreOperations", "close",
        // "Closing socket connections...", 0, -1);
        // }
        if (this.dis != null)
            dis.close();
        if (this.dos != null)
            dos.close();
        if (this.socket != null)
            socket.close();
        if (logFromServer) {
            logger.logEvent(LogLevel.NETWORK, "CoreOperations", "close",
                    "Socket connections closed successfully.", 0, -1);
        }
    }

}
