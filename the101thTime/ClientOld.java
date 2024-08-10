

/*
 * This is the old version of the Client program.
 * The future model of ComFaaS will be Server, Edge, Caller model.
 */

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class ClientOld {
    final public String directory ;

    ClientOld() {
        directory = establishFolder() ;
    }

    public String establishFolder() {
        String currentDir = System.getProperty("user.dir");
        String folderPath = currentDir + "/clientDir"; // Adjust folder name as needed

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
     * ~/jdk-21.0.2/bin/javac ClientOld.java
     * ~/jdk-21.0.2/bin/java ClientOld 4444 the.server.ip.address
     */
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]) ;
        String ip = args[1] ;
        Scanner scanner = new Scanner(System.in) ;
        try {
            boolean serverLoop = true ;
            ClientOld clientOld = new ClientOld() ;
            Communication communication ;

            communication = new Communication(ip, port, clientOld.directory) ;

            //Do work
            communication.requestFile("another.c") ;

            communication.requestFolder("test") ;

            communication.close() ;

        }
        catch (IOException e) {
            e.printStackTrace() ;
        }
    }

}
