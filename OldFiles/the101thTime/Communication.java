import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

/*
 * In collectFolder, there is an else condition that could be something else.
 */

public class Communication {
    final protected SocketWork socketWork ;
    public String directory ;

    Communication(ServerSocket serverSocket, String directory) throws IOException {
        socketWork = new SocketWork(serverSocket) ;
        this.directory = directory ;
        collectCommand() ;
    }

    Communication(String ip, int port, String directory) throws UnknownHostException, IOException {
        socketWork = new SocketWork(ip, port) ;
        this.directory = directory ;
        sendCommand("initialization") ;
    }

    public void close() {
        socketWork.close() ;
    }

    public boolean sendFile(String name) throws IOException {
        File file = new File(directory+"/"+name) ;
        System.out.println("sending: "+file.getAbsolutePath());
        if (file.exists()) {
            sendCommand("found") ;
            socketWork.dataOutputStream.writeLong(file.length()) ;
            FileInputStream fis = new FileInputStream(file) ;
            byte[] buffer = new byte[4096] ;
            int bytesRead ;
            while ((bytesRead = fis.read(buffer)) != -1) {
                socketWork.dataOutputStream.write(buffer,0,bytesRead) ;
            }
            socketWork.dataOutputStream.flush() ;
            fis.close() ;
            return true ;
        }
        else {
            sendCommand("not found") ;
            return false ;
        }
    }

    public void sendFolder(String name) throws IOException {
        File folder = new File(directory+"/"+name) ;
        if (folder.isDirectory()) {
            File[] files = folder.listFiles() ;
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        sendCommand("file") ;
                        sendCommand(file.getName()) ;
                        sendFile(name+"/"+file.getName()) ;
                    }
                }
                sendCommand("nofile") ;
            }
            else {
                System.out.println("folder has not files") ;
                sendCommand("nofile") ;
            }   
        }
        else {
            System.out.println("The folder does not exist") ;
            sendCommand("noDir") ;
        }
    }
    
    private int collectFile(String name) throws IOException {
        System.out.println("collectFile name: "+name) ;
        String fileFound = collectCommand() ;
        if(fileFound.equals("found")) {
            long fileSize = socketWork.dataInputStream.readLong() ;
            File file = new File(directory+"/"+name) ;
            FileOutputStream fos = new FileOutputStream(file) ;
            byte[] buffer = new byte[4096] ;
            int bytesRead ;

            while ( fileSize > 0 && (bytesRead = socketWork.dataInputStream.read( buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                fos.write(buffer, 0, bytesRead) ;
                fileSize -= bytesRead ;
            }
            fos.close() ;
            System.out.println(directory+"/"+name);

            return 1 ;
        }
        else if (fileFound.equals("not found")) {
            System.out.println("collectFile: not found") ;
            return 0 ;
        }
        else {
            throw new IOException("This error was not throw by defult but by result of unexected command in collectFile: "+fileFound) ;
            // return -1 ;
        }

    }

    private void collectFolder(String name) throws IOException {
        String file = collectCommand(); 
        boolean success ;
        File folder ;
        String file_name_tmp ;
        if (file.equals("noDir")) {
            // Do nothing
        }
        else if (file.equals("nofile")) {
            // make the directory still.
            folder = new File(directory+"/"+name) ;
            if (!folder.exists()) {
                success = folder.mkdirs() ;
                if (success) {
                    System.out.println("Directory: " + folder + " created");
                } else { //This could be changed to something else.
                    System.out.println("Directory: " + folder + " creation failed");
                    socketWork.close() ;
                    System.exit(-1) ;
                }
            } else {
                System.out.println("Directory: " + folder + " already exists");
            }
        }
        else if (file.equals("file")) {
            // make the directort and start collecting files.
            folder = new File(directory+"/"+name) ;
            if (!folder.exists()) {
                success = folder.mkdirs() ;
                if (success) {
                    System.out.println("Directory: " + folder + " created");
                } else { //This could be changed to something else.
                    System.out.println("Directory: " + folder + " creation failed");
                    socketWork.close() ;
                    System.exit(-1) ;
                }
            } else {
                System.out.println("Directory: " + folder + " already exists");
            }
            do {
                
                file_name_tmp = collectCommand() ;
                System.out.println("name: "+name) ;
                collectFile(name+"/"+file_name_tmp) ;
                file = collectCommand(); 
            } while(file.equals("file")) ;
        }
        else {
            throw new IOException("collectFolder: command was unrecognised.") ;
        }
    }
    
    public void requestFile(String name) throws IOException {
        sendCommand("requestFile") ;
        sendCommand(name) ;
        collectFile(name) ;
        sendCommand("requestFile sucess") ;

    }

    public void requestFolder(String name) throws IOException {
        sendCommand("requestFolder") ;
        sendCommand(name) ;
        collectFolder(name) ;
        sendCommand("requestFolder sucess") ;
    }

    public boolean processCommand(String command) throws IOException {
        boolean status = false ;
        String name ;
        switch (command) {
            case "requestFolder":
                name = collectCommand() ;
                sendFolder(name) ;
                name = collectCommand() ;
                if (name.equals("requestFolder sucess")) {
                    status = true ;
                }
                else {
                    status = false ;
                }
                break;
            case "requestFile":
                name = collectCommand() ;
                System.out.println("processCommand: requestFile: name: " + name) ;
                sendFile(name) ;
                name = collectCommand() ; //this is status
                System.out.println("processCommand: requestFile: status: " + name) ;
                if (name.equals("requestFile sucess")) {
                    status = true ;
                }
                else {
                    status = false ;
                }
                break;
        
            default:
                System.out.println("command was incorrect, recieved: " + command) ;
        }
        return status ;
    }

    public boolean isCommCommand(String command) {
        switch (command) {
            case "requestFolder":
            case "requestFile":
                return true ;
            default:
                return false ;
        }
    }

    public int sendCommand(String command) throws IOException {

        //send the command
        socketWork.dataOutputStream.writeUTF(command) ;
        socketWork.dataOutputStream.flush() ;
        System.out.println("command sent: "+command);

        // collect request
        return socketWork.dataInputStream.readInt() ;
    }

    public String collectCommand() throws IOException {
        String tmp = socketWork.dataInputStream.readUTF() ;
        System.out.println("command caught: "+tmp) ;
        socketWork.dataOutputStream.writeInt(1) ;
        socketWork.dataOutputStream.flush() ;
        return tmp ;
    }

    

    public static void main(String[] args) {
        String testMsg ;
        String classPath ;
        String folderPath ;
        File folder ;
        boolean success ;
        String command ;

        int port = Integer.parseInt(args[1]) ;

        if (args[0].equals("server")) {
            Scanner scanner = new Scanner(System.in) ;
            try {
                ServerSocket serverSocket = new ServerSocket(port) ;

                // This gets the directory for where the class is stored.
                classPath = Communication.class.getProtectionDomain().getCodeSource().getLocation().getPath() ;
                // This set up to find the serverFolder or create it.
                folderPath = new File(classPath).getParent() + "/serverFolder" ;
                folder = new File(folderPath) ;
                if (!folder.exists()) {
                    success = folder.mkdirs() ;
                    if (success) {
                        System.out.println("Directory: " + folderPath + " created");
                    } else {
                        System.out.println("Directory: " + folderPath + " creation failed");
                        serverSocket.close() ;
                        System.exit(-1) ;
                    }
                } else {
                    System.out.println("Directory: " + folderPath + " already exists");
                }
                
                Communication communication = new Communication(serverSocket, folderPath) ;

                // Do work
                // this is for communication.requestFile("another.c") ;

                command = communication.collectCommand() ;
                communication.processCommand(command) ;

                // this is for communication.requestFolder("test") ;
                command = communication.collectCommand() ;
                communication.processCommand(command) ;
                

                communication.close() ;
                serverSocket.close() ;

            } catch (IOException e) {
                e.printStackTrace() ;
            }
            scanner.close() ;
        }
        else if (args[0].equals("client")) {
            String ip = args[2] ;

            try {
                // This gets the directory for where the class is stored.
                classPath = Communication.class.getProtectionDomain().getCodeSource().getLocation().getPath() ;
                // This set up to find the serverFolder or create it.
                folderPath = new File(classPath).getParent() + "/clientFolder" ;
                folder = new File(folderPath) ;
                if (!folder.exists()) {
                    success = folder.mkdirs() ;
                    if (success) {
                        System.out.println("Directory: " + folderPath + " created");
                    } else {
                        System.out.println("Directory: " + folderPath + " creation failed");
                        System.exit(-1) ;
                    }
                } else {
                    System.out.println("Directory: " + folderPath + " already exists");
                }
                Communication communication = new Communication(ip, port, folderPath) ;

                // Do work here.
                communication.requestFile("another.c") ;

                communication.requestFolder("test") ;
                
                communication.close() ;

            } catch (IOException e) {
                e.printStackTrace() ;
            }
        }
        else {
            System.out.println("Read the source code") ;
        }
    }
}
