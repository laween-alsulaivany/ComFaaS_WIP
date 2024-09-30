package comfaas;

import java.io.IOException ;
import java.io.DataOutputStream ;
import java.io.DataInputStream ;
import java.net.ServerSocket ;
import java.net.Socket ;
import java.net.UnknownHostException ;


public class Server extends CoreOperations {
    public ServerSocket serverSocket ;
    public Socket clientSocket ;


    public Server(ServerSocket tmp) throws UnknownHostException, IOException {
        this.clientSocket = tmp.accept() ;
        this.outStream = this.clientSocket.getOutputStream() ;
        this.dos = new DataOutputStream(this.outStream) ;
        this.inStream = this.clientSocket.getInputStream() ;
        this.dis = new DataInputStream(this.inStream) ;
    }

    public void init(String[] args) {

    }

    public void close() throws IOException {
        this.dis.close() ;
        this.dos.close() ;
        this.inStream.close() ;
        this.outStream.close() ;
        this.clientSocket.close() ;
    }

    public static void run(final int port) throws UnknownHostException, IOException {
        ServerSocket tmp = new ServerSocket(port) ;
        System.out.println("At port " + port+ " ready to go.");
        while (true) {
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
    }

    
}
