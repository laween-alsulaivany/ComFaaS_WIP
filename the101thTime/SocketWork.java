import java.io.*;
import java.net.ServerSocket;
import java.net.Socket ;
import java.net.SocketException;
import java.net.UnknownHostException;


/*
 * The point of this code is to abstract the 
 * initiation and closing of the streams
 * of the socket connections.
 * 
 * Warning, it seems the setSoTimeout is not working.
 * 
 * SocketWork(ServerSocket serverSocket)
 *  for the server to set up
 * SocketWork(String ip, int port)
 *  for the client to set up
 * public void close()
 *  for closing a SocketWork
 */
public class SocketWork {
    public Socket socket ;
    public InputStream inputStream ;
    public OutputStream outputStream ;
    public DataInputStream dataInputStream ;
    public DataOutputStream dataOutputStream ;


    SocketWork(ServerSocket serverSocket) throws IOException, SocketException {
        try {
            this.socket = serverSocket.accept() ;
            // this.socket.setSoTimeout(10000) ;
            this.inputStream = this.socket.getInputStream() ;
            this.outputStream = this.socket.getOutputStream() ;
            this.dataInputStream = new DataInputStream(inputStream) ;
            this.dataOutputStream = new DataOutputStream(outputStream) ;
        } catch (SocketException e) {
            e.printStackTrace() ;
            throw new SocketException() ;
        } catch (IOException e) {
            e.printStackTrace() ;
            throw new IOException() ;
        }
        
    }
    SocketWork(String ip, int port) throws UnknownHostException, IOException, SocketException {
        try {
            this.socket = new Socket(ip, port) ;
            // this.socket.setSoTimeout(10000) ;
            this.inputStream = this.socket.getInputStream() ;
            this.outputStream = this.socket.getOutputStream() ;
            this.dataInputStream = new DataInputStream(inputStream) ;
            this.dataOutputStream = new DataOutputStream(outputStream) ;
        } catch (SocketException e) {
            e.printStackTrace() ;
            throw new SocketException() ;
        } catch (IOException e) {
            e.printStackTrace() ;
            throw new IOException() ;
        } 
    }

    public void close() {
        try {
            dataOutputStream.close() ;
            dataInputStream.close() ;
            outputStream.close() ;
            inputStream.close() ;
            socket.close() ;
        }   
        catch (IOException e) {
            e.printStackTrace() ;
        }
    }


    /*
     * Use: java SocketWork server port
     * or
     * Use: java SocketWork client port ip
     * 
     * I used
     *  ~/jdk-21.0.2/bin/javac SocketWork.java 
     *  ~/jdk-21.0.2/bin/java SocketWork server 4444
     * ~/jdk-21.0.2/bin/java SocketWork client 4444 192.168.68.
     */
    public static void main(String[] args) {
        String testMsg ;
        // for (int i = 0; i < args.length ; i ++)
        //     System.out.println(args[i]);
        

        int port = Integer.parseInt(args[1]) ;

        

        if (args[0].equals("server")) {
            try {
                ServerSocket serverSocket = new ServerSocket(port) ;
                SocketWork socketWork = new SocketWork(serverSocket)  ;
                
                testMsg = socketWork.dataInputStream.readUTF() ;
                socketWork.dataOutputStream.writeUTF("Sending from server") ;
                System.out.println(testMsg);

                socketWork.close() ;
                serverSocket.close() ;
            }
            catch (IOException e) {
                e.printStackTrace() ;
            }
        }
        else if (args[0].equals("client")) {
            String ip = args[2] ;

            try {
                SocketWork socketWork = new SocketWork(ip, port) ;

                System.out.println("Currently waiting") ;
                try {
                    Thread.sleep(2000) ;
                }
                catch (InterruptedException e) {
                    e.printStackTrace() ;
                }
                System.out.println("Waiting Done") ;

                socketWork.dataOutputStream.writeUTF("Sending from client") ;
                testMsg = socketWork.dataInputStream.readUTF() ;
                 System.out.println(testMsg);

                socketWork.close() ;
            }
            catch (IOException e) {
                e.printStackTrace() ;
            }
       }
       else {
        System.out.println("Read the source code") ;
       }
    }
}
