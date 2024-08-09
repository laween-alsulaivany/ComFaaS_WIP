import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Communication {
    /*
     * The point of this program is to organize the 
     * commands for sending and recieving files thru 
     * java functions.
     * 
     * There is an example program of how to use it at the bottom of the code.
     * 
    */

    // This value is what keeps the loop running.
    public SocketWork socketWork ;
    public boolean acceptFileFolderAcyncBreak = true ;
    public final Thread FileFolderAcync; 

    Communication(SocketWork theSocketsk) {
        socketWork = theSocketsk ;
        FileFolderAcync = acceptFileFolderAcync_initialization() ;
    }

    public void sendFile() {

    }
    
    public void requestFile() {

    }

    public void sendFolder() {

    }

    public void requestFolder() {

    }

    public Thread acceptFileFolderAcync_initialization() {
        // This starts the acceptFileFolderAcync thread to start collecting and managing files and folders.
        Thread FileFolderAcync = new Thread(() -> {
            this.acceptFileFolderAcync(); 
        });
        return FileFolderAcync ;
    }

    public void acceptFileFolderAcync_close() {
        acceptFileFolderAcyncBreak = false ;
        try {
            FileFolderAcync.join() ;
        } catch (InterruptedException e) {
            System.out.println("Issues with the thread closing present. Program continuing...") ;
        }
        
    }

    private void acceptFileFolderAcync() {
        /*
         * This needs logic for accepting files and folders,
         *  and the requests for files and folders.
         * 
         * This needs to handle stopping by either the server or client.
         * 
         * This should only be called by a thread. 
        */
        
    }

    public static void main(String[] args) {
        // This is an example program of running the Communications library.

        final int port; 
        int tmp_port = -1 ;
        int serverClientVal = 0 ;

        for (String arg : args) {
            if (arg.startsWith("-port=")) {
                try {
                    tmp_port = Integer.parseInt(arg.substring(6));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid  port number: " + arg);
                    System.exit(1);
                }
            }
            else if (arg.equals("server")) {
                serverClientVal = 1 ;
            }
            else if (arg.equals("client")) {
                serverClientVal = 2 ;
            }
        }

        if (tmp_port == -1) {
            System.err.println("Port number not specified");
            System.exit(1);
        }
        port = tmp_port ;

        if (serverClientVal == 1) {
            /*
             * This thread will act as the server for the example program.
             * It will set up the socket and await on stanby. It will then 
             * prompt the command line for arguments if the user wants to close
             * the server. 
             */
            ServerSocket serverSocket = null;
            SocketWork socketWork = null;
            Communication communication; //= new Communication() ;
            Scanner input = new Scanner(System.in) ;

            try {
                serverSocket = new ServerSocket(port) ;
            } catch (IOException ex) {
                System.out.println("Can't set up server on port "+port+". ") ;
                System.exit(-1) ;
            }

            try {
                socketWork = new SocketWork(serverSocket.accept()) ;
            } catch (IOException e) {
                e.printStackTrace() ;
                System.exit(-1) ;
            }
            

            communication = new Communication(socketWork) ;

            // Now that the server is set up, time for server control
            String userInput ;
            System.out.println("Please enter 'quit' to stop program");
            do {
                userInput = input.nextLine() ;
                System.out.println( "Command: " + userInput + " was called." );

            } while (userInput.equals("quit")) ;
            
            System.out.println("Now Closing Server") ;

            communication.acceptFileFolderAcync_close() ;

            socketWork.closeSockets() ;
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.out.println(e);
            }
            input.close() ;
        }

        else if (serverClientVal == 2) {
            Socket socket = null;
            SocketWork socketWork = null ;
            Communication communication = null ;
            try {
                socket = new Socket("localhost", port) ;
            }
            catch (UnknownHostException e) {
                e.printStackTrace() ;
                System.exit(-1);
            }
            catch (IOException e) {
                e.printStackTrace() ;
                System.exit(-1);
            }
            socketWork = new SocketWork(socket) ;
            communication = new Communication(socketWork) ;

            // Work happpens in heres

            System.out.println("Now Closing Client");

            communication.acceptFileFolderAcync_close() ;

            socketWork.closeSockets() ;

            try {
                socket.close();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        else {
            System.out.println("You need to specify 'server' or 'client' ");
        }

    }
}
