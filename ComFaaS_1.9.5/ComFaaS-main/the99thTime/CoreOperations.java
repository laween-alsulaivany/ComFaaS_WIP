import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CoreOperations {
    public InputStream inStream ;
    public OutputStream outStream ;
    public DataInputStream dis ;
    public DataOutputStream dos ;

    public void deleteFile(String filePath) {
        Path path = Paths.get(filePath) ;
        
        try {
            Files.delete(path) ;
            // System.out.println("File deleted successfully.");
        } catch (IOException e) {
            System.out.println("Failed to delete the file.");
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
        int ct ;
        while (!(tmp = this.dis.readUTF()).equals("done")) { //!
                
            if (tmp.equals("requestFile")){ 
                tmp = this.dis.readUTF() ; 
                tmp2 = this.dis.readUTF() ; 
                System.out.println(tmp + " " + tmp2);
                sendFile(tmp, tmp2);
            }
            else if (tmp.equals("loadFiles")) {
                tmp = this.dis.readUTF();
                recieveFolder(tmp);

            }
            else if (tmp.equals("runTest")) {
                ct = this.dis.readInt() ;// returns number of commands sent over
                tmp = this.dis.readUTF() ; // read first command
                tmp2 = "" ;
                if(ct == 2)
                    tmp2 = this.dis.readUTF() ; // read the second command
                moveFiles("Input/", "Input/") ; //This needs to be avoided.
                if(ct == 2)
                    runTest(tmp, tmp2) ;
                else
                    runTest(tmp) ;
                moveFiles("Output/", "Output/") ;//This needs to be avoided.
                emptyFolder("Input/");
            }
            else if (tmp.equals("requestFolder")) {
                tmp = this.dis.readUTF() ;
                //System.out.println("Sending: "+tmp);
                sendFolder(tmp) ;
                emptyFolder(tmp); //be carefull with this.
            }

            else if (tmp.equals("lazyRun")) {

            }
        }

    }
    
    public void requestFolder(String serverExtention, String clientExtention) throws IOException {
        this.dos.writeUTF("requestFolder");
        this.dos.writeUTF(serverExtention) ; 
        this.dos.flush() ;
        //System.out.println("Requesting: "+serverExtention+" storing: "+clientExtention);
        recieveFolder(clientExtention);
    }
    
    public void initialiseRemoteTest(String Exec) throws IOException {
        this.dos.writeUTF("runTest") ;
        this.dos.writeInt(1) ;
        this.dos.writeUTF(Exec) ;
        this.dos.flush() ;
    }

    public void initialiseRemoteTest(String firstExec, String secondExec) throws IOException {
        this.dos.writeUTF("runTest") ;
        this.dos.writeInt(2);
        this.dos.writeUTF(firstExec) ;
        this.dos.writeUTF(secondExec) ;
        this.dos.flush() ;
        
    }
    
    public void runTest(String Exec) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime() ;
        Process process = runtime.exec(Exec) ;
        int exitCode = process.waitFor() ;
    }

    public void runTest(String firstExec, String secondExec) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime() ;
        Process process = runtime.exec(firstExec) ;
        int exitCode = process.waitFor() ;
        process = runtime.exec(secondExec) ;
        exitCode = process.waitFor() ;
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

    protected void moveFiles(String initialFolder, String finalFolder) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime() ;
        Process process = runtime.exec("mv " + initialFolder + "* " + finalFolder) ;
        int exitCode = process.waitFor() ;
    }

    protected void sendFile(String extention, String filename) throws IOException {
        File file = new File(extention+filename) ;
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
        File file = new File(extention + fileName);
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while (fileSize > 0 && (bytesRead = this.dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    fileSize -= bytesRead;
                }
        //System.out.println("File received successfully: " + fileName);
        fos.close() ;
    }

    protected void sendFolder(String folderName) throws IOException { //"Input/"
        File folder = new File(folderName);

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
