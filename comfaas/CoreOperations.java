package comfaas;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class CoreOperations {
    public InputStream inStream ;
    public OutputStream outStream ;
    public DataInputStream dis ;
    public DataOutputStream dos ;

    public String edgeInputFolder = "EdgeInput" ;
    public String edgeOutputFolder = "EdgeOutput" ;
    public String edgeProgramsFolder = "EdgePrograms" ;
    public String edgeVenv = ".edgeVenv" ;
    public String serverInputFolder = "ServerInput" ;
    public String serverOutputFolder = "ServerOutput" ;
    public String serverProgramsFolder = "ServerPrograms" ;
    public String serverVenv = ".serverVenv" ;
    

    public void deleteFile(String filePath) {
        Path path = Paths.get(filePath) ;
        
        try {
            Files.delete(path) ;
            // System.out.println("File deleted successfully.");
        } catch (IOException e) {
            // System.out.println("Failed to delete the file.");
        }
    }

    public void emptyFolder(String folderName) {
        File folder = new File(folderName);
        if(folder.isDirectory()) {
            File[] files= folder.listFiles() ;
            if (files != null ) {
                for(File file : files) {
                    if (file.isFile()) {
                        deleteFile(folderName+"/"+file.getName());
                    }
                }
            }
            else {
            System.out.println("folder has no files.");
            }
        }
        else {
        System.out.println("The folder does not exist.");
        }
    }
    
    public void manageRequests() throws IOException, InterruptedException{
        
        String tmp, tmp2 ;
        int np ;
        while (!(tmp = this.dis.readUTF()).equals("done")) { //!
                
            if (tmp.equals("requestFile")){
                tmp = this.dis.readUTF() ;
                tmp2 = this.dis.readUTF() ;
                // System.out.println(tmp + " " + tmp2);
                sendFile(tmp, tmp2);
            }
            else if (tmp.equals("loadFiles")) {
                tmp = this.dis.readUTF();
                recieveFolder(tmp);

            }
            else if (tmp.equals("runTestv2")) {
                tmp = this.dis.readUTF() ; // Language
                tmp2 = this.dis.readUTF() ; // Program
                np = this.dis.readInt() ;
                try {
                    runTest2(tmp,tmp2, np) ;
                    sendInt(0) ;
                } catch (IOException e) {
                    sendInt(1) ;
                }
                // System.out.println("Done with all");
            }
            else if (tmp.equals("requestFolder")) {
                tmp = this.dis.readUTF() ;
                sendFolder(tmp) ;
                emptyFolder(tmp); //be carefull with this.
            }
        }
        // System.out.println("leaving");
    }
    
    public void requestFolder(String serverExtention, String clientExtention) throws IOException {
        this.dos.writeUTF("requestFolder");
        this.dos.writeUTF(serverExtention) ; 
        this.dos.flush() ;
        recieveFolder(clientExtention) ;
    }

    public int runProgramOnCloud(String language, String Program, int np) throws IOException {
        this.dos.writeUTF("runTestv2") ;
        this.dos.writeUTF(language) ;
        this.dos.writeUTF(Program) ;
        this.dos.writeInt(np) ; 
        this.dos.flush() ;
        return this.dis.readInt() ;
    }

    public void runTest2(String langauge, String program, int np) throws IOException {
        
        if (np == 1) {
            if (langauge.equals("Python")||langauge.equals("python")) {
                try {
                    // System.out.println("----before----") ;
                    // PythonRunner.runPythonScript(serverProgramsFolder+"/"+program) ;
                    PythonRunner.runPythonScriptInVenv(serverVenv, serverProgramsFolder+"/"+program) ;
                    // System.out.println("----after----") ;
                } catch (IOException | InterruptedException e) {
                    
                    e.printStackTrace() ;
                    throw new IOException() ;
                    
                }
            } else if (langauge.equals("java")) {
                this.copyFile(serverProgramsFolder+"/"+program, program) ;
                JavaProgramRunner runner = new JavaProgramRunner() ;
                try {
                    if (runner.compileJavaProgram(program)) {
                        // System.out.println("----before----") ;
                        String className = runner.getClassName(program) ;
                        // System.out.println("Class Name: "+className) ;
                        runner.runJavaProgram(className) ;
                        // System.out.println("----after----") ;
                        this.deleteFile(className+".class") ; // This needs to be changed out of the if scope in the future.
                    }
                    
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    throw new IOException() ;
                    
                }
            } else if (langauge.equals("C")) {
                CProgramRunner runner = new CProgramRunner() ;
                String cSourceFilePath = serverProgramsFolder +"/"+ program ;
                try {
                    if (runner.compileCProgram(cSourceFilePath)) {
                        // System.out.println("----before----") ;
                        runner.runCProgram(cSourceFilePath) ;
                        // System.out.println("----after----") ;
                        runner.deleteExecutable(cSourceFilePath) ;
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace() ;
                    throw new IOException() ;
                    
                }
            }
        } else if (np > 1) {
            if (langauge.equals("Python")||langauge.equals("python")) {
                try {
                    // System.out.println("----before----") ;
                    // PythonRunner.runPythonScript(serverProgramsFolder+"/"+program) ;
                    // PythonRunner.runPythonScriptInVenv(serverVenv, serverProgramsFolder+"/"+program) ;
                    PythonRunner.runPythonScriptWithMpi(serverVenv, serverProgramsFolder+"/"+program, np) ;
                    // System.out.println("----after----") ;
                } catch (IOException | InterruptedException e) {
                    
                    e.printStackTrace() ;
                    throw new IOException() ;
                    
                }
                deleteFile(program);
            } else if (langauge.equals("C")) {
                CProgramRunner runner = new CProgramRunner() ;
                String cSourceFilePath = serverProgramsFolder +"/"+ program ;
                try {
                    if (runner.compileMPICHProgram(cSourceFilePath)) {
                        // System.out.println("----before----") ;
                        runner.runMPICHProgram(cSourceFilePath, np) ;
                        // System.out.println("----after----") ;
                        runner.deleteExecutable(cSourceFilePath) ;
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace() ;
                    throw new IOException() ;
                    
                }
            }
            
        }

    }
    

    public void loadFiles(String serverExtention, String foldername) throws IOException {
        this.dos.writeUTF("loadFiles");//!
        this.dos.writeUTF(serverExtention) ;
        this.dos.flush() ;
        sendFolder(foldername) ;
    }

    public void requestfile(String serverExtention, String name, String clientExtention) throws IOException{
        this.dos.writeUTF("requestFile");
        this.dos.writeUTF(serverExtention) ; 
        this.dos.writeUTF(name) ;
        this.dos.flush() ;
        recieveFile(clientExtention) ;
    }

    protected void moveFiles(String initialFolder, String finalFolder) throws IOException {
        Path sourceDir = Paths.get(initialFolder);
        Path targetDir = Paths.get(finalFolder);

        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
            for (Path file : stream) {
                Path targetPath = targetDir.resolve(file.getFileName());
                Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    public void moveFile(String sourcePath, String targetPath) throws IOException {
        // Create Path objects for source and target
        Path source = Paths.get(sourcePath);
        Path target = Paths.get(targetPath);

        try {
            // Move the file
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);

            // Notify successful move
            // System.out.println("File moved successfully!");
        } catch (IOException e) {
            // Handle possible exceptions
            System.out.println("An error occurred while moving the file: " + e.getMessage());
            throw e;
        }
    }

    public void copyFile(String sourcePath, String targetPath) throws IOException {
        // Create Path objects for source and target
        Path source = Paths.get(sourcePath);
        Path target = Paths.get(targetPath);

        try {
            // Copy the file
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

            // Notify successful copy
            // System.out.println("File copied successfully!");
        } catch (IOException e) {
            // Handle possible exceptions
            System.out.println("An error occurred while copying the file: " + e.getMessage());
            throw e;
        }
    }

    protected void sendFile(String extention, String filename) throws IOException {
        File file = new File(extention+"/"+filename) ;
        if (file.exists()) {
            this.dos.writeUTF(file.getName()) ;
            this.dos.writeLong(file.length()) ;
            FileInputStream fis = new FileInputStream(file) ;
            byte[] buffer = new byte[4096] ;
            int bytesRead ;
            while ((bytesRead = fis.read(buffer)) != -1) {
                this.dos.write(buffer, 0, bytesRead);
            }
            this.dos.flush() ;
            //System.out.println("File sent successfully.");
            fis.close() ;
        } else {
            System.out.println("File not found.");
        }
    }
    
    protected void recieveFile(String extention) throws IOException {
        String fileName = this.dis.readUTF();
        long fileSize = this.dis.readLong();
        File file = new File(extention + "/" + fileName);
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while (fileSize > 0 && (bytesRead = this.dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    fileSize -= bytesRead;
                }
        // System.out.println("File received successfully: " + fileName);
        fos.close() ;
    }

    protected void sendFolder(String folderName) throws IOException { 
        File folder = new File(folderName+"/");

        if (folder.isDirectory()) {
            File[] files = folder.listFiles() ;
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        sendInt(1) ;
                        sendFile(folderName, file.getName()) ;
                    }
                }
            }
            else {
                System.out.println("folder has no files.");
            }
            sendInt(0) ;
        }
        else {
            System.out.println("The folder does not exist.");
        }

    }

    protected void recieveFolder(String extention) throws IOException {
        while (this.recieveInt() == 1) {
            recieveFile(extention);
        }
    }

    protected void sendInt(int value) throws IOException {
        this.dos.writeInt(value);
        this.dos.flush() ;
    }

    protected int recieveInt() throws IOException {
        return this.dis.readInt() ;
    }

}
