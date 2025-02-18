// Client.java

package comfaas;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;

import comfaas.Logger.LogLevel;

// ------------------------------------------
// * The Client class represents a client connecting to a remote server.
// ------------------------------------------

public class Client extends CoreOperations {
    // Logging
    private static final Logger logger = new Logger(Main.LogFile);

    // ------------------
    // Command-line arguments and task properties
    // ------------------
    public String server = null; // e.g., "localhost" or "1.2.3.4"
    public Integer port = -1;
    public String[] args;
    public String location = "server"; // "server" or "client"
    public int np = 1; // number of processes
    public String tn = null; // task name
    public String lang = null; // e.g., "c" or "python"
    public String programName = null; // e.g., "WaitFor3Seconds.c"
    public String sourceFolder = null; // e.g., "EdgeInput"
    public String destinationFolder = null; // e.g., "EdgeOutput"

    // ------------------

    public static final Path rootDir = Main.getRootDir();

    // ------------------
    // Constructor that takes in command-line args and connects to the server.
    // ------------------
    private void initializeClient() {
        CommandLineProcessor.processArguments(this);
    }

    // ------------------------------------------
    // Constructor that takes in command-line args and connects to the server.
    // ------------------------------------------
    public Client(String[] args) {
        super() ;
        logger.info("Client", "Constructor", "Client initialized.");

        // Parse arguments
        this.args = args;
        initializeClient();

        Path[] directories = {
                rootDir.resolve("client").resolve("Programs"),
                rootDir.resolve("client").resolve("Input"),
                rootDir.resolve("client").resolve("Output")
        };

        // System.out.println("Programs: " + directories[0]);
        // System.out.println("Input: " + directories[1]);
        // System.out.println("Output: " + directories[2]);
        Main.verifyDirectories(directories, "Client Constructor");

        // Connect to the server
        try {
            logger.network("Client", "Constructor", "Connecting to server at " + this.server + ":" + this.port);

            // TODO: Replace sockets with HTTP requests
            // Create the socket
            this.socket = new Socket(this.server, this.port);
            this.socket.setSoTimeout(10000); // Set a 10-second timeout

            // Create dis/dos from the socket
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());

            logger.success("Client", "Constructor", "client successfully connected to the server.");

        } catch (UnknownHostException e) {
            logger.error("Client", "Constructor", "Unknown host: " + this.server);
            System.exit(1);
        } catch (IOException e) {
            logger.error("Client", "Constructor", "Error connecting to server: " + e.getMessage());
            System.exit(1);
        }

    }

    // ------------------------------------------
    // Executes the specified task on server, then closes connection.
    // ------------------------------------------

    public void runTask(String location, String language, String programName, int np) throws IOException {
        // 1) Send the command
        // System.out
        // .println("Executing task: Location=" + location + ", Language=" + language +
        // ", Program=" + programName
        // + ", Processes=" + np);
        // System.err.println(programName);
        // System.out.println("trying to Autoupload task file");
        autoUploadTaskFile(programName);
        logger.info("Client", "runTask", "Autoupload task file done");

        RequestLogger.logRequest("runTask", true);
        dos.writeUTF("runTask");
        // System.out.println("writing runTask");
        logger.info("Client", "runTask", "Executing task: " +
                "Location=" + location + ", Language=" + language + ", Program=" + programName + ", Processes=" + np);
        // 2) Send parameters
        dos.writeUTF(location); // "cloud" or "edge"
        dos.writeUTF(language); // e.g. "c" or "python"
        dos.writeUTF(programName); // e.g. "WaitFor3Seconds.c"
        dos.writeInt(np); // number of processes
        // System.out.println("Sent task parameters");

        // 3) Read the server response
        String response;
        // System.out.println("Reading server response: ");
        try {
            response = dis.readUTF();
            // System.out.println("Server response: " + response);
        } catch (EOFException e) {
            throw new IOException("Server closed connection unexpectedly", e);
        }
        if (response.startsWith("ERR:")) {
            throw new IOException("Remote execution error: " + response);
        }
        logger.network("Client", "runRemoteTask", "Server response: " + response);

        // response = dis.readUTF();
        // if ("executeTaskComplete".equals(response)) {
        // System.out.println("Task completed!");
        // } else {
        // System.out.println("Unexpected response: " + response);
        // }
        // expected: "executeTaskComplete" or "OK: Task executed successfully"
        // dos.writeUTF("done");
        dos.flush();

    }

    // ---------------------------------------------------------
    // * CLIENT-SIDE: File Operations (uploadFile, downloadFile, deleteFile,
    // listFiles)
    // ---------------------------------------------------------

    // ------------------------------------------
    // Client-Side: Upload a file
    // ------------------------------------------
    public void uploadFile(String localPath, String remoteFolder) throws IOException {
        File localFile = Main.resolveLocalFile(localPath);
        if (!localFile.exists()) {
            logger.logEvent(LogLevel.ERROR, "CoreOperations", "uploadFile",
                    "Local file not found: " + localFile.getAbsolutePath(), 0, -1);
            throw new IOException("Local file not found: " + localFile.getAbsolutePath());
        }

        // Tell server the command:
        dos.writeUTF("uploadSingleFile");

        // Send the destination folder, file name, and file size
        dos.writeUTF(remoteFolder); // e.g. "EdgeInput"
        dos.writeUTF(localFile.getName()); // e.g. "myfile.txt"
        dos.writeLong(localFile.length()); // size in bytes

        // Send file bytes
        try (FileInputStream fis = new FileInputStream(localFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
        }
        // dos.writeUTF("done");
        dos.flush();

        // Optionally read server response (OK/ERR)
        String response = dis.readUTF();
        logger.logEvent(LogLevel.NETWORK, "CoreOperations", "uploadFile",
                "Server response: " + response, 0, localFile.length());
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
            logger.logEvent(LogLevel.ERROR, "CoreOperations", "downloadFile",
                    "Server error: " + response, 0, -1);
            throw new IOException("Server error: " + response);
        }
        // If OK, then read file size
        long fileSize = dis.readLong();

        // Prepare local folder/file
        File outDir = Main.resolveLocalFile(localFolder);
        if (!outDir.exists())
            outDir.mkdirs();
        File outFile = new File(outDir, fileName);

        // Read bytes from server
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[4096];
            long remaining = fileSize;
            int bytesRead;
            while (remaining > 0 && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                fos.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
        }
        logger.logEvent(LogLevel.SUCCESS, "CoreOperations", "downloadFile",
                "File downloaded: " + outFile.getAbsolutePath(), 0, fileSize);
        // dos.writeUTF("done");
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
        // dos.writeUTF("done");
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
        File folder = Main.resolveLocalFile(localFolder);
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
        // dos.writeUTF("done");
        dos.flush();
    }

    // ------------------------------------------
    // Client-Side: Download a folder
    // ------------------------------------------

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
        File folder = Main.resolveLocalFile(localFolder);
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

            // Read file bytes using the known file size
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
        }

        // Read final termination message from the server
        response = dis.readUTF();
        logger.logEvent(LogLevel.NETWORK, "CoreOperations", "downloadFolder",
                "Server termination message: " + response, 0, -1);
        if (response.startsWith("ERR")) {
            logger.logEvent(LogLevel.ERROR, "CoreOperations", "downloadFolder",
                    "Error from server: " + response, 0, -1);
            throw new IOException("Server error: " + response);
        }
        // dos.writeUTF("done");
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
        // dos.writeUTF("done");
        dos.flush();

    }

    // ------------------------------------------
    // Auto-upload a task file
    // ------------------------------------------
    public void autoUploadTaskFile(String programName) throws IOException {
        // Construct the local file path relative to the project root.
        // Since Main.resolveLocalFile() is used inside uploadFile(), we can pass a
        // relative path.
        String localPath = "client/Input/" + programName;
        String remoteFolder = "server/Programs";

        // Determine the destination folder.
        // This is dynamic; for now both edge and cloud use "server/Programs",
        // but you can modify this logic if different behavior is desired.
        try {
            // Call the existing uploadFile() method to perform the upload.
            uploadFile(localPath, remoteFolder);

        } catch (Exception e) {
            logger.info("Client", "autoUploadTaskFile",
                    "Uploading file " + programName + " from " + localPath + " to " + remoteFolder);
        }

    }
}
