// CoreOperations.java
package comfaas;

import java.io.*;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


    // ------------------------------------------
    // * CoreOperations class provides utility functions for file and folder operations,
    // * task execution, and communication between the server and the edge.
    // ------------------------------------------
public class CoreOperations {
    

    // Streams & Socket
    protected Socket socket;
    protected DataInputStream dis;
    protected DataOutputStream dos;

    // Folder names for Edge vs. Server
    public String edgeInputFolder    = "EdgeInput";
    public String edgeOutputFolder   = "EdgeOutput";
    public String edgeProgramsFolder = "EdgePrograms";
    public String edgeVenv           = ".edgeVenv";

    public String serverInputFolder    = "ServerInput";
    public String serverOutputFolder   = "ServerOutput";
    public String serverProgramsFolder = "ServerPrograms";
    public String serverVenv           = ".serverVenv";

    // protected InputStream inStream;
    // protected OutputStream outStream;

    // Logging
    private static final Logger edge_logger = new Logger("edge_logs.csv");
    private static final Logger server_logger = new Logger("server_logs.csv");


    // ------------------------------------------
    // Server-Side: Manage incoming requests
    // ------------------------------------------
    public void manageRequests() throws IOException, InterruptedException {
        String command;
        while (!(command = this.dis.readUTF()).equals("done")) {
            switch (command) {
                case "uploadSingleFile" -> handleUploadSingleFile();
                case "downloadSingleFile" -> handleDownloadSingleFile();
                case "deleteSingleFile" -> handleDeleteSingleFile();

                case "uploadFolder" -> handleUploadFolder();
                case "downloadFolder" -> handleDownloadFolder();
                case "deleteFolder" -> handleDeleteFolder();

                case "listFiles" -> handleListFiles();

                case "executeTask" -> handleExecuteTask();

                default -> System.err.println("Unknown command received: " + command);
            }
        }

        server_logger.logInfo("Client sent 'done'. Exiting request management.");
        server_logger.log("INFO", "Client sent 'done'. Exiting request management.", "N/A", "Server", -1);
    }

    // ---------------------------------------------------------
    // * Single File: Upload / Download / Delete
    // ---------------------------------------------------------
      // ------------------------------------------
    // Handles sending a file to the edge's input folder.
    // ------------------------------------------

private void handleUploadSingleFile() throws IOException {
    // Read the destination folder and file name
    String destinationFolder = dis.readUTF();
    String fileName = dis.readUTF();
    long fileSize = dis.readLong();

    // Create the destination directory if it does not exist
    File folder = new File(destinationFolder);
    if (!folder.exists()) {
        folder.mkdirs();
    }
    // Create the output file
    File outFile = new File(folder, fileName);

    // Write the file to the output stream
    try (FileOutputStream fos = new FileOutputStream(outFile))
    {
        byte[] buffer = new byte[4096];
        long remaining = fileSize;
        int bytesRead;
        while (remaining > 0 && (bytesRead = dis.read(buffer, 0, (int)Math.min(buffer.length, remaining))) != -1) {
            fos.write(buffer, 0, bytesRead);
            remaining -= bytesRead;
        }
        server_logger.logSuccess("File uploaded: " + outFile.getAbsolutePath());
        dos.writeUTF("File uploaded successfully");

    }

    }

    // ------------------------------------------
    // Handles downloading a file from the edge's input folder.
    // ------------------------------------------
private void handleDownloadSingleFile() throws IOException {
    // Read the source folder and file name
    String sourceFolder = dis.readUTF();
    String fileName = dis.readUTF();

    // Create the source file
    File inFile = new File(sourceFolder, fileName);

    // Check if the file exists
    if (!inFile.exists()) {
        server_logger.logError("File not found: " + inFile.getAbsolutePath());
        dos.writeUTF("File not found");
        return;
    }


    // Write the file to the output stream
    try (FileInputStream fis = new FileInputStream(inFile))
    {
        dos.writeUTF("File found");
        dos.writeLong(inFile.length());
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            dos.write(buffer, 0, bytesRead);
        }
    }
    dos.flush();
    server_logger.logSuccess("File downloaded: " + inFile.getAbsolutePath());
    }

    // ------------------------------------------
    // Handles deleting a file from the edge's input folder.
    // ------------------------------------------
    private void handleDeleteSingleFile() throws IOException {
    // Read the source folder and file name
    String sourceFolder = dis.readUTF();
    String fileName = dis.readUTF();

    // Create the source file
    File fileToDelete = new File(sourceFolder, fileName);

    // Check if the file exists
    if (!fileToDelete.exists()) {
        server_logger.logError("File not found: " + fileToDelete.getAbsolutePath());
        dos.writeUTF("File not found");
        return;
    }

    // Delete the file
    if (fileToDelete.delete()) {
        server_logger.logSuccess("File deleted: " + fileToDelete.getAbsolutePath());
        dos.writeUTF("File deleted successfully");
    } else {
        server_logger.logError("Error deleting file: " + fileToDelete.getAbsolutePath());
        dos.writeUTF("Error deleting file");
    }
    }

    // ------------------------------------------
    // Handles listing files in a given directory.
    // ------------------------------------------
    private void handleListFiles() throws IOException {
        // Read the folder name from the client
        String sourceFolder = dis.readUTF();
        File directory = new File(sourceFolder);
    
        // If directory doesn't exist or isn't a directory, notify client
        if (!directory.exists() || !directory.isDirectory()) {
            dos.writeUTF("ERR: " + sourceFolder + " does not exist or is not a directory");
            server_logger.logError("Directory not found: " + sourceFolder);
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
        server_logger.logInfo("End of file list");
    }
    


    // ------------------------------------------
    // Handles executing a task on the edge.
    // ------------------------------------------
    private void handleExecuteTask() throws IOException, InterruptedException {
        String location = dis.readUTF();   // "cloud" or "edge"
        String language = dis.readUTF();
        String programName = dis.readUTF();
        int np = dis.readInt();
    
        if (null == location) {
            System.err.println("Invalid location for executeTask: " + location);
        } else switch (location) {
            case "cloud" -> runProgramOnCloud(language, programName, np);
            case "edge" -> runProgramOnEdge(language, programName, np);
            default -> System.err.println("Invalid location for executeTask: " + location);
        }
        dos.writeUTF("executeTaskComplete");
    }

    // ---------------------------------------------------------
    // * Folder-based commands
    // ---------------------------------------------------------
    protected void handleUploadFolder() throws IOException {
        // e.g., read the folder name from the client, read each file,
        // create them on the server side. We'll leave this unimplemented for now.
        dos.writeUTF("ERR: handleUploadFolder not implemented");
    }

    protected void handleDownloadFolder() throws IOException {
        dos.writeUTF("ERR: handleDownloadFolder not implemented");
    }

    protected void handleDeleteFolder() throws IOException {
        dos.writeUTF("ERR: handleDeleteFolder not implemented");
    }

    // -------------------------
    // * UTILITY FUNCTIONS
    // -------------------------

    // ------------------------------------------
    // Copy a file from one path to another
    // ------------------------------------------
    public void copy(String sourcePath, String targetPath) throws IOException {
        Path source = Paths.get(sourcePath);
        Path target = Paths.get(targetPath);

        try {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            System.err.println("File copied successfully: " + sourcePath + " to " + targetPath);
            edge_logger.logFileAction("INFO", "File copied", sourcePath, "COPY", -1, Files.size(source));
        } catch (IOException e) {
            String err = "An error occurred while copying the file: " + e.getMessage();
            System.out.println(err);
            edge_logger.logFileAction("ERROR", e.getMessage(), sourcePath, "COPY_FAILED", -1, -1);
            throw e;
        }
    }

    // ------------------------------------------
    // Move or rename a file/folder
    // ------------------------------------------
    public void move(String sourcePath, String targetPath) throws IOException {
        Path source = Paths.get(sourcePath);
        Path target = Paths.get(targetPath);

        // Check if the source exists
        if (!Files.exists(source)) {
            System.err.println("Source path does not exist: " + sourcePath);
            return;
        }

        // If it's a file, move it
        if (Files.isRegularFile(source)) {
            try {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("File moved successfully: " + sourcePath + " -> " + targetPath);
            } catch (IOException e) {
                System.err.println("Error moving file: " + e.getMessage());
                throw e;
            }
        }
        // If it's a directory, move all files from it
        else if (Files.isDirectory(source)) {
            if (!Files.exists(target)) {
                Files.createDirectories(target);
            }
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(source)) {
                for (Path file : stream) {
                    Path targetFile = target.resolve(file.getFileName());
                    Files.move(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Moved file: " + file + " -> " + targetFile);
                }
                System.out.println("All files moved from folder: " + sourcePath + " -> " + targetPath);
            } catch (IOException e) {
                System.err.println("Error moving files from folder: " + e.getMessage());
                throw e;
            }
        } else {
            System.err.println("Source path is neither a file nor a folder: " + sourcePath);
        }
    }

    // ------------------------------------------
    // * TASK EXECUTION
    // ------------------------------------------

    
    // ------------------------------------------
    // Run a program on the Edge
    // ------------------------------------------

    public int runProgramOnEdge(String language, String program, int np) throws IOException {
        // This code is basically your 'executeLocalTask' from before.
        String filePath = edgeProgramsFolder + "/" + program;  // e.g. "EdgePrograms/mytask"
        File taskFile = new File(filePath);
    
        if (!taskFile.exists()) {
            throw new IOException("Edge task file not found: " + filePath);
        }
    
        try {
            if (np == 1) {
                switch (language.toLowerCase()) {
                    case "python" -> PythonRunner.runPythonScriptInVenv(edgeVenv, filePath);
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
                    case "python" -> PythonRunner.runPythonScriptWithMpi(edgeVenv, filePath, np);
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
            throw new IOException("Error executing local task on Edge: " + e.getMessage(), e);
        }
        return 0; // success
    }

    // ------------------------------------------
    // Run a program on the Cloud
    // ------------------------------------------

    public int runProgramOnCloud(String language, String program, int np) throws IOException {
        // This code is basically identical to the Edge approach, but referencing the 'serverProgramsFolder'.
        String filePath = serverProgramsFolder + "/" + program; // "ServerPrograms/mytask"
        File taskFile = new File(filePath);
    
        if (!taskFile.exists()) {
            throw new IOException("Cloud task file not found: " + filePath);
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
    // * Cleanup
    // ---------------------------------------------------------

    // ------------------------------------------
    // Close all streams and sockets
    // ------------------------------------------
    public void close() throws IOException {
        server_logger.logNetwork("Closing socket connections...");
        if (this.dis != null)    dis.close();
        if (this.dos != null)    dos.close();
        if (this.socket != null) socket.close();
        server_logger.logNetwork("Socket connections closed successfully.");
    }


    // ---------------------------------------------------------
    // * CLIENT-SIDE: File Operations (uploadFile, downloadFile, deleteFile, listFiles)
    // ---------------------------------------------------------


    // ------------------------------------------
    // Client-Side: Upload a file
    // ------------------------------------------
    public void uploadFile(String localPath, String remoteFolder) throws IOException {
        File localFile = new File(localPath);
        if (!localFile.exists()) {
            throw new IOException("Local file not found: " + localFile.getAbsolutePath());
        }

        // Tell server the command:
        dos.writeUTF("uploadSingleFile");

        // Send the destination folder, file name, and file size
        dos.writeUTF(remoteFolder);              // e.g. "EdgeInput"
        dos.writeUTF(localFile.getName());       // e.g. "myfile.txt"
        dos.writeLong(localFile.length());       // size in bytes

        // Send file bytes
        try (FileInputStream fis = new FileInputStream(localFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
        }
        dos.writeUTF("done");
        dos.flush();

        // Optionally read server response (OK/ERR)
        String response = dis.readUTF();
        edge_logger.logNetwork("Server response: " + response);
    }

    // ------------------------------------------
    // Client-Side: Download a file
    // ------------------------------------------

    public void downloadFile(String remoteFolder, String fileName, String localFolder) throws IOException {
        // Command
        dos.writeUTF("downloadSingleFile");

        // Source folder and file name
        dos.writeUTF(remoteFolder);
        dos.writeUTF(fileName);

        // Read server response
        String response = dis.readUTF(); // e.g. "OK: File found" or "ERR: File not found"
        if (response.startsWith("ERR")) {
            // server says error
            throw new IOException("Server error: " + response);
        }
        // If OK, then read file size
        long fileSize = dis.readLong();

        // Prepare local folder/file
        File outDir = new File(localFolder);
        if (!outDir.exists()) outDir.mkdirs();
        File outFile = new File(outDir, fileName);

        // Read bytes from server
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[4096];
            long remaining = fileSize;
            int bytesRead;
            while (remaining > 0 && (bytesRead = dis.read(buffer, 0, (int)Math.min(buffer.length, remaining))) != -1) {
                fos.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
        }
        edge_logger.logSuccess("File downloaded: " + outFile.getAbsolutePath());
        dos.writeUTF("done");
        dos.flush();
    }

    // ------------------------------------------
    // Client-Side: Delete a file
    // ------------------------------------------

    public void deleteFileOnServer(String folder, String fileName) throws IOException {
        // Command
        dos.writeUTF("deleteSingleFile");

        // Send folder and file name
        dos.writeUTF(folder);
        dos.writeUTF(fileName);

        // Read server response
        String response = dis.readUTF(); // e.g. "OK: File deleted successfully" or "ERR: File not found"
        edge_logger.logNetwork("Server response: " + response);
        if (response.startsWith("ERR")) {
            throw new IOException("Server error: " + response);
        }
        dos.writeUTF("done");
        dos.flush();
    }

    // ------------------------------------------
    // Client-Side: List files in a folder
    // ------------------------------------------
    public void listFilesOnServer(String remoteFolder) throws IOException {
        // Send the command
        dos.writeUTF("listFiles");
        // Send the folder name
        dos.writeUTF(remoteFolder);
    
        // Read the server's initial response: either "ERR: ...", or "OK: listing files"
        String response = dis.readUTF();
        edge_logger.logNetwork("Server response: " + response);
    
        if (response.startsWith("ERR:")) {
            // Some error from server
            throw new IOException("Server error: " + response);
        }
        // Otherwise, we assume it starts with "OK: listing files"
    
        // Now read lines until "End of list"
        while (true) {
            String fileName = dis.readUTF();
            if ("End of list".equals(fileName)) {
                break;
            }
            System.out.println(" - " + fileName);
        }
    
    }
    
    
    // ------------------------------------------
    // Server-Side: Send one file
    // ------------------------------------------
    public void sendFile(String folderName, String filename) throws IOException {
        File file = new File(folderName + "/" + filename);
        System.err.println("Looking for file: " + file.getAbsolutePath());
        if (file.exists() && file.isFile()) {
            System.out.println("Sending file: " + file.getName());
            server_logger.log("INFO", "Sending file: " + file.getName(), "N/A", "Server", -1);

            dos.writeUTF("file");         
            dos.writeUTF(file.getName());    
            dos.writeLong(file.length());    

            // Now send file bytes
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
                dos.flush();
            }
            System.out.println("File sent successfully.");
            server_logger.log("INFO", "File sent successfully", "N/A", "Server", -1);

        } else {
            System.out.println("File not found: " + file.getName());
            server_logger.logError("File not found: " + file.getName(), "N/A", "Server");
        }
        dos.writeUTF("done");
        dos.flush();
    }

    // ------------------------------------------
    // Server-Side: Send folder listing
    // ------------------------------------------
    protected void sendFolder(String folderName) throws IOException {
        File folder = new File(folderName + "/");

        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null && files.length > 0) {
                dos.writeUTF("folder");
                dos.writeInt(files.length);
                for (File file : files) {
                    if (file.isFile()) {
                        dos.writeUTF(file.getName());
                    }
                }
                System.err.println("Folder listing sent: " + files.length + " file(s).");
                server_logger.log("INFO", "Folder listing sent", "N/A", "Server", -1);
            } else {
                // no files
                dos.writeUTF("empty");
                System.out.println("folder has no files.");
            }
        } else {
            // not a folder
            dos.writeUTF("empty");
            System.out.println("The folder does not exist or is not a directory.");
        }
        dos.writeUTF("done");
        dos.flush();
    }

    // ------------------------------------------
    // Client-Side: Request one file from server
    // ------------------------------------------
    public void requestFile(String serverFolder, String fileName, String clientFolder) throws IOException {
        System.out.println("Requesting file: " + fileName + " from " + serverFolder + " to " + clientFolder);
        server_logger.log("INFO", "Requesting file: " + fileName + " from " + serverFolder + " to " + clientFolder, "N/A", "Server", -1);

        // 1) Tell server we want this file
        dos.writeUTF("requestFile");
        dos.writeUTF(serverFolder);
        dos.writeUTF(fileName);

        // 2) The server's sendFile() now runs. We read server response:
        String response = dis.readUTF();        // "file" or possibly an error

        if ("file".equals(response)) {
            // Next read actual file name
            String sentFileName = dis.readUTF();
            long fileSize       = dis.readLong();

            File outFile = new File(clientFolder + "/" + sentFileName);

            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[4096];
                long remaining = fileSize;
                int bytesRead;
                while (remaining > 0
                       && (bytesRead = this.dis.read(buffer, 0, (int)Math.min(buffer.length, remaining))) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    remaining -= bytesRead;
                }
            }
            System.out.println("File received successfully: " + sentFileName);

        } else {
            // Possibly the server responded "File not found" or something
            String err = "Expected 'file' but got: " + response;
            System.out.println(err);
            server_logger.logError(err, "N/A", "Server");
        }
        dos.writeUTF("done");
        dos.flush();
    }

    // ------------------------------------------
    // Client-Side: Request an entire folder
    // ------------------------------------------
    public void requestFolder(String serverFolder, String clientFolder) throws IOException {
        System.out.println("Requesting folder: " + serverFolder + " to " + clientFolder);
        server_logger.log("INFO", "Requesting folder: " + serverFolder + " to " + clientFolder, "N/A", "Server", -1);

        // 1) Ask the server for a folder listing
        dos.writeUTF("requestFolder");
        dos.writeUTF(serverFolder);

        // 2) Server either responds "folder" with file count, or "empty"
        String response = dis.readUTF();

        if (response == null) {
            System.err.println("Server returned a null response for folder request.");
            return;
        }

        switch (response) {
            case "folder" -> {
                int fileCount = dis.readInt(); // Number of files in that folder
                for (int i = 0; i < fileCount; i++) {
                    // read the next filename
                    String foundFileName = dis.readUTF();
                    // For each file, do a new requestFile() call
                    requestFile(serverFolder, foundFileName, clientFolder);
                }
                System.out.println("Folder received successfully: " + clientFolder);
            }
            case "empty" -> {
                System.out.println("Requested folder is empty or does not exist on server: " + serverFolder);
            }
            default -> {
                // Possibly an error from server
                String errMsg = "Error requesting folder: " + response;
                System.err.println(errMsg);
                server_logger.logError(errMsg, "N/A", "Server");
            }
        }
    }

    // ------------------------------------------
    // Moves or loads files from source to target
    // ------------------------------------------
    public void loadFiles(String sourceFolder, String destinationFolder) throws IOException {
        System.out.println("Loading files from " + sourceFolder + " to " + destinationFolder);
        server_logger.log("INFO", "Loading files from " + sourceFolder + " to " + destinationFolder, "N/A", "Server", -1);
        move(sourceFolder, destinationFolder);
    }

    // ------------------------------------------
    // Delete a file
    // ------------------------------------------
    public void deleteFile(String filePath) throws IOException {
        long startTime = System.currentTimeMillis();
        System.out.println("Attempting to delete file: " + filePath);

        Path path = Paths.get(filePath);
        long fileSize = Files.size(path);
        try {
            Files.delete(path);
            long endTime = System.currentTimeMillis();

            System.out.println("File deleted: " + filePath);
            edge_logger.logFileAction("INFO", "File deleted", filePath, "DELETE", (endTime - startTime), fileSize);

        } catch (IOException e) {
            System.err.println("Error deleting file: " + e.getMessage());
            edge_logger.logFileAction("ERROR", e.getMessage(), filePath, "DELETE_FAILED", -1, -1);
        }
    }

    // ------------------------------------------
    // Empty a folder
    // ------------------------------------------
    public void emptyFolder(String folderName) {
        File folder = new File(folderName);
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        try {
                            deleteFile(folderName + "/" + file.getName());
                        } catch (IOException e) {
                            System.err.println("Error deleting file: " + e.getMessage());
                        }
                    }
                }
            } else {
                System.out.println("folder has no files.");
            }
        } else {
            System.out.println("The folder does not exist.");
        }
    }

 


}
