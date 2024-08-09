import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.IllegalArgumentException;

/*
 * This file manages the sockets set up and breakdown.
 * Created: 8/8/2024
 * 
 */

public class SocketWork {
    // public ServerSocket serverSocket ;
    public Socket socket ;
    public InputStream inStream ;
    public OutputStream outStream ;
    public DataInputStream dis ;
    public DataOutputStream dos ;
    
    SocketWork(Socket acceptedSocket) {
        // this takes in an active socket only and builds from there.
        try {
            if (!socket.isConnected()) {
                throw new IllegalArgumentException("socket needs to be a proper connection.") ;
            }
        } catch (NullPointerException e) {
            System.out.println("the acceptedSocket in SockWork.java was null") ;
            System.exit(-1) ;
        }
        socket = acceptedSocket ;

        try {
            inStream = socket.getInputStream() ;
        }
        catch (IOException e) {
            e.printStackTrace() ;
            System.exit(-1) ;
        }
        try {
            outStream = socket.getOutputStream() ;
        }
        catch (IOException e) {
            e.printStackTrace() ;
            System.exit(-1) ;
        }
        dis = new DataInputStream(inStream) ; 
        dos = new DataOutputStream(outStream) ;
    }

    public void closeSockets() {
        try {
            dos.close() ;
        } catch (IOException e) {
            e.printStackTrace() ;
        }
        try {
            dis.close() ;
        } catch (IOException e) {
            e.printStackTrace() ;
        }
        try {
            inStream.close() ;
        } catch (IOException e) {
            e.printStackTrace() ;
        }
        try {
            outStream.close() ;
        } catch (IOException e) {
            e.printStackTrace() ;
        }
        try {
            socket.close() ;
        } catch (IOException e) {
            e.printStackTrace() ;
        }
    }
}
