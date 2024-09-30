import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;


/*
 * This is the old version of the Client program.
 * The future model of ComFaaS will be Server, Edge, Caller model.
 */
public class ServerOld {
    final public String directory ;

    ServerOld() {
        directory = establishFolder() ;
    }

    public String establishFolder() {
        String currentDir = System.getProperty("user.dir");
        String folderPath = currentDir + "/serverDir"; // Adjust folder name as needed

        File folder = new File(folderPath);

        if (!folder.exists()) {
            boolean success = folder.mkdirs();
            if (success) {
                System.out.println("Directory: " + folderPath + " created");
            } else {
                System.out.println("Directory: " + folderPath + " creation failed");
                System.exit(-1) ;
            }
        } else {
            System.out.println("Directory: " + folderPath + " already exists");
        }
        return folderPath ;
    }
    
    /*
     * ~/jdk-21.0.2/bin/javac ServerOld.java
     * ~/jdk-21.0.2/bin/java ServerOld 4444
     */
    public static void main(String[] args) {

        String command ;
        int port = Integer.parseInt(args[0]) ;


        Scanner scanner = new Scanner(System.in) ;
        try {
            boolean serverLoop = true ;
            ServerOld server = new ServerOld() ;
            ServerSocket serverSocket = new ServerSocket(port) ;
            Communication communication ;
            do {
                communication = new Communication(serverSocket, server.directory) ;

                //Do work
                do {
                    command = communication.collectCommand() ;
                    if (communication.isCommCommand(command))
                        communication.processCommand(command) ;
                    else {
                        switch (command) {
                            case "done":
                                serverLoop = false ;
                                break;
                        
                            default:
                                break;
                        }
                    }
                } while (serverLoop);
                

                communication.close() ;
            } while(serverLoop) ;
            
            serverSocket.close() ;

        } catch (IOException e) {
            e.printStackTrace() ;
        }
        scanner.close() ; 
    }
}
