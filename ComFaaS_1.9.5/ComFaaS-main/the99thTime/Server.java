import java.io.*;
import java.net.*;

public class Server extends CoreOperations {
    public ServerSocket serverSocket ;
    public Socket clientSocket ;

    public Server(ServerSocket tmp) throws UnknownHostException, IOException {
        this.clientSocket = tmp.accept() ;
        this.outStream = this.clientSocket.getOutputStream() ;
        this.dos = new DataOutputStream(this.outStream) ;
        this.inStream = this.clientSocket.getInputStream() ;
        this.dis = new DataInputStream(this.inStream) ;
        //this.outStream = socket.getOutputStream() ;
        //this.objectOutStream = new ObjectOutputStream(this.outStream) ;
    };

    public void close() throws IOException {
        this.dis.close() ;
        this.dos.close() ;
        this.inStream.close() ;
        this.outStream.close() ;
        this.clientSocket.close() ;
    }

    public static void main(String[] args) {
        final int port = 12353; // Change this to your desired port number
        int unique = 0 ;
        try {
            ServerSocket tmp = new ServerSocket(port) ;
            System.out.println("At port " + port+ " ready to go.");
            while (true) {
                unique++ ;
                Server server = new Server(tmp) ;
                try {
                    server.manageRequests() ;
                } catch (IOException e) {
                    e.printStackTrace() ;
                } catch (InterruptedException e) {
                    e.printStackTrace() ;
                }
                server.close() ;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
