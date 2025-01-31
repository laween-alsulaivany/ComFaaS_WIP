// CoreOperations.java
package comfaas;

import comfaas.Logger.LogLevel;
import java.io.*;
import java.net.Socket;


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


    // Logging
    private static final Logger logger = new Logger(Main.LogFile);


    // ------------------------------------------
    // Server-Side: Manage incoming requests
    // ------------------------------------------
public void manageRequests() throws IOException, InterruptedException {
    while (true) {
        String command = dis.readUTF();
        if ("done".equals(command)) {
            logger.logEvent(LogLevel.INFO, "Server", "manageRequests", "Client sent 'done'. Exiting request management.", 0, -1);
            break;
        }
        // TODO: Shutdown command is not working properly due commands being sent as a line while readUTF() takes only binary. Fix this.
        else if ("shutdownServer".equals(command)) {
            logger.logEvent(LogLevel.INFO, "Server", "manageRequests", "Received shutdown command from client.", 0, -1);

            CloudServer.setShutdownFlag();
            EdgeServer.setShutdownFlag();

            // We'll rely on the outer code to handle shutting down the socket & thread pool.
            break;
    } else {
        switch (command) {
            case "uploadSingleFile" -> handleUploadSingleFile();
            case "downloadSingleFile" -> handleDownloadSingleFile();
            case "deleteSingleFile" -> handleDeleteSingleFile();
            case "uploadFolder" -> handleUploadFolder();
            case "downloadFolder" -> handleDownloadFolder();
            case "deleteFolder" -> handleDeleteFolder();
            case "listFiles" -> handleListFiles();
            case "executeTask" -> handleExecuteTask();
            default -> logger.logEvent(LogLevel.WARNING, "Server", "manageRequests", 
                "Unknown command received: " + command, 0, -1);
        }
    }
    }

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
        logger.logEvent(LogLevel.SUCCESS, "CoreOperations", "handleUploadSingleFile", 
            "File uploaded: " + outFile.getAbsolutePath(), 0, fileSize);
        dos.writeUTF("File uploaded successfully");
    } catch (IOException e) {
        logger.logEvent(LogLevel.ERROR, "CoreOperations", "handleUploadSingleFile", 
            "Error uploading file: " + e.getMessage(), 0, -1);
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
        logger.logEvent(LogLevel.ERROR, "CoreOperations", "handleDownloadSingleFile", 
            "File not found: " + inFile.getAbsolutePath(), 0, -1);
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
    logger.logEvent(LogLevel.SUCCESS, "CoreOperations", "handleDownloadSingleFile", 
    "File downloaded: " + inFile.getAbsolutePath(), 0, inFile.length());   
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
        logger.logEvent(LogLevel.ERROR, "CoreOperations", "handleDeleteSingleFile", 
            "File not found: " + fileToDelete.getAbsolutePath(), 0, -1);
        dos.writeUTF("File not found");
        return;
    }

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

    // ------------------------------------------
    // Handles uploading a folder to the edge's input folder.
    // ------------------------------------------
    protected void handleUploadFolder() throws IOException {
        // 1) Folder name
        String destinationFolder = dis.readUTF();
        // 2) number of files
        int numFiles = dis.readInt();
    
        File folder = new File(destinationFolder);
        if (!folder.exists()) folder.mkdirs();
    
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
                while (remaining > 0 && (bytesRead = dis.read(buffer, 0, (int)Math.min(buffer.length, remaining))) != -1) {
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
    // Handles downloading a folder from the edge's input folder.
    // ------------------------------------------
    protected void handleDownloadFolder() throws IOException {
        String sourceFolder = dis.readUTF();
        File folder = new File(sourceFolder);
        if (!folder.isDirectory()) {
            dos.writeUTF("ERR: " + sourceFolder + " is not a directory");
            return;
        }
        File[] files = folder.listFiles();
        if (files == null) files = new File[0];
    
        // send # of files
        dos.writeUTF("OK");
        dos.writeInt(files.length);
    
        for (File f : files) {
            if (f.isFile()) {
                // file name
                dos.writeUTF(f.getName());
                // file size
                dos.writeLong(f.length());
                // file bytes
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
        logger.logEvent(LogLevel.SUCCESS, "CoreOperations", "handleDownloadFolder",
            "Folder downloaded: " + sourceFolder, 0, -1);
    }
    
    // ------------------------------------------
    // Handles deleting a folder from the edge's input folder.
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
    // * CLIENT-SIDE: File Operations (uploadFile, downloadFile, deleteFile, listFiles)
    // ---------------------------------------------------------


    // ------------------------------------------
    // Client-Side: Upload a file
    // ------------------------------------------
    public void uploadFile(String localPath, String remoteFolder) throws IOException {
        File localFile = new File(localPath);
        if (!localFile.exists()) {
            logger.logEvent(LogLevel.ERROR, "CoreOperations", "uploadFile", 
            "Local file not found: " + localFile.getAbsolutePath(), 0, -1);
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
        logger.logEvent(LogLevel.NETWORK, "CoreOperations", "uploadFile", 
        "Server response: " + response, 0, localFile.length());    }

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
            logger.logEvent(LogLevel.ERROR, "CoreOperations", "downloadFile", 
            "Server error: " + response, 0, -1);
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
        logger.logEvent(LogLevel.SUCCESS, "CoreOperations", "downloadFile", 
        "File downloaded: " + outFile.getAbsolutePath(), 0, fileSize);        dos.writeUTF("done");
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
        logger.logEvent(LogLevel.NETWORK, "CoreOperations", "deleteFileOnServer", 
        "Server response: " + response, 0, -1);
                if (response.startsWith("ERR")) {
                    logger.logEvent(LogLevel.ERROR, "CoreOperations", "deleteFileOnServer", 
                    "Error from server: " + response, 0, -1);
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
        logger.logEvent(LogLevel.NETWORK, "CoreOperations", "listFilesOnServer", 
        "Server response: " + response, 0, -1);    
        if (response.startsWith("ERR:")) {
            // Some error from server
            logger.logEvent(LogLevel.ERROR, "CoreOperations", "listFilesOnServer", 
            "Error from server: " + response, 0, -1);
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
        logger.logEvent(LogLevel.SUCCESS, "CoreOperations", "listFilesOnServer", "File listing complete", 0, -1);
    
    }
    
    // ------------------------------------------
    // Client-Side: Upload a folder
    // ------------------------------------------
public void uploadFolder(String localFolder, String remoteFolder) throws IOException {
    // Command
    dos.writeUTF("uploadFolder");
    // Send the destination folder
    dos.writeUTF(remoteFolder);
    
    // List files in the local folder
    File folder = new File(localFolder);
    File[] files = folder.listFiles();
    if (files == null) {
        logger.logEvent(LogLevel.ERROR, "CoreOperations", "uploadFolder", 
        "Local folder not found: " + localFolder, 0, -1);
        throw new IOException("Local folder not found: " + localFolder);
    }
    
    // Send the number of files
    dos.writeInt(files.length);
    
    // Send each file
    for (File file : files) {
        dos.writeUTF(file.getName());
        dos.writeLong(file.length());
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
        }
    }
    
    // Read server response
    String response = dis.readUTF();
    logger.logEvent(LogLevel.NETWORK, "CoreOperations", "uploadFolder", 
    "Server response: " + response, 0, -1);
    if (response.startsWith("ERR")) {
        logger.logEvent(LogLevel.ERROR, "CoreOperations", "uploadFolder", 
        "Error from server: " + response, 0, -1);
        throw new IOException("Server error: " + response);
    }
    dos.writeUTF("done");
    dos.flush();
}

    // ------------------------------------------
    // Client-Side: Download a folder
    // ------------------------------------------
    // TODO: BUG for some reason, whatever it downloads it sets its size to 50MB
public void downloadFolder(String remoteFolder, String localFolder) throws IOException {
    // Command
    dos.writeUTF("downloadFolder");
    // Send the source folder
    dos.writeUTF(remoteFolder);
    
    // Read server response
    String response = dis.readUTF();
    if (response.startsWith("ERR")) {
        logger.logEvent(LogLevel.ERROR, "CoreOperations", "downloadFolder", 
        "Error from server: " + response, 0, -1);
        throw new IOException("Server error: " + response);
    }
    
    // Read the number of files
    int numFiles = dis.readInt();
    
    // Create the local folder if it doesn't exist
    File folder = new File(localFolder);
    if (!folder.exists()) {
        folder.mkdirs();
    }
    
    // Read each file
    for (int i = 0; i < numFiles; i++) {
        // Read file name
        String fileName = dis.readUTF();
        // Read file size
        long fileSize = dis.readLong();
        File outFile = new File(folder, fileName);
    
        // Read file bytes
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = dis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
    }
    
    // Read server response
    response = dis.readUTF();
    logger.logEvent(LogLevel.NETWORK, "CoreOperations", "downloadFolder", 
    "Server response: " + response, 0, -1);
    if (response.startsWith("ERR")) {
        logger.logEvent(LogLevel.ERROR, "CoreOperations", "downloadFolder", 
        "Error from server: " + response, 0, -1);
        throw new IOException("Server error: " + response);
    }
    dos.writeUTF("done");
    dos.flush();
}

    // ------------------------------------------
    // Client-Side: Delete a folder
    // ------------------------------------------
public void deleteFolder(String remoteFolder) throws IOException {
    // Command
    dos.writeUTF("deleteFolder");
    // Send the folder name
    dos.writeUTF(remoteFolder);
    
    // Read server response
    String response = dis.readUTF();
    logger.logEvent(LogLevel.NETWORK, "CoreOperations", "deleteFolder", 
    "Server response: " + response, 0, -1);
    if (response.startsWith("ERR")) {
        logger.logEvent(LogLevel.ERROR, "CoreOperations", "deleteFolder", 
        "Error from server: " + response, 0, -1);
        throw new IOException("Server error: " + response);
    }
    dos.writeUTF("done");
    dos.flush();

}
   // ---------------------------------------------------------
    // * Cleanup
    // ---------------------------------------------------------

    // ------------------------------------------
    // Close all streams and sockets
    // ------------------------------------------
    public void close(boolean logFromServer) throws IOException {
        if (logFromServer) {
        logger.logEvent(LogLevel.NETWORK, "CoreOperations", "close", 
            "Closing socket connections...", 0, -1);
        }
        if (this.dis != null) dis.close();
        if (this.dos != null) dos.close();
        if (this.socket != null) socket.close();
    if (logFromServer){
        logger.logEvent(LogLevel.NETWORK, "CoreOperations", "close", 
            "Socket connections closed successfully.", 0, -1);
    }
}
    
}
