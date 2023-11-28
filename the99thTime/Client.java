import java.io.*;
import java.net.*;
import java.time.Duration;
import java.time.Instant;

// to run: java Client edge java 1 1

public class Client extends CoreOperations{
    public Socket socket ;
    public Object[] metaData ;
    //public ObjectOutputStream objectOutStream ;

    public Client(String address, int port) throws UnknownHostException, IOException {
        this.socket = new Socket(address, port) ;
        this.inStream = this.socket.getInputStream();
        this.dis = new DataInputStream(this.inStream) ;
        this.outStream = socket.getOutputStream() ;
        this.dos = new DataOutputStream(this.outStream) ;
        //this.objectOutStream = new ObjectOutputStream(this.outStream) ;
    };

    public void close() throws IOException {
        //this.objectOutStream.close() ;
        this.dis.close() ;
        this.dos.close() ;
        this.inStream.close() ;
        this.outStream.close() ;
        this.socket.close() ;
    }

    public static void main(String[] args) {
        String time = "Time broke :/" ;
        Instant start = Instant.now() ;
        final String serverAddress = "judahsbase.tplinkdns.com"; //"localhost"; // Change this to the server's IP address or hostname
        final int serverPort = 12350; // Change this to the server's port
        if (args.length < 4) {
            System.out.println("Invalid arguments");
            System.exit(1);
        }
        String type = args[0] ; //"edge" ; //This is edge or cloud depending on argument.
        String language = args[1] ;//"java" ;//This is java, python, or C
        int processes = Integer.parseInt(args[2]); //1 ;//1 is sequencial, any more and the parallel version is used.
        int test = Integer.parseInt(args[3]); //each number is assosiated with a relative test.
        int kill = 0 ;
        if (args.length == 5)
            if(args[4].equals("kill"))
            kill = 1 ; //this means the client would like to kill the server completly.
        try {
            Client client = new Client(serverAddress, serverPort) ;
            client.metaData = new Object[5];
            client.metaData[0] = type ;
            client.metaData[1] = language ; //
            client.metaData[2] = processes ;
            client.metaData[3] = test ;
            client.metaData[4] = kill ;

            if (type.equals("cloud")) { //Only works for C_ColorToGrayImage at this time.
                if (processes == 1) {
                    client.loadFiles("Input/", "Input/");
                    client.initialiseRemoteTest("gcc -o C_ColorToGrayImage Programs/C_ColorToGrayImage.c -ljpeg", "./C_ColorToGrayImage");
                    client.requestFolder("Output/","Output/") ;
                    Instant end = Instant.now() ;
                    time = "time: " +  String.format("%.3f",Duration.between(start, end).toMillis()/1000.0) + " seconds.";
                }
                
            }
            else { //Only works for C_ColorToGrayImage at this time.
                if ((!type.equals("cloud"))&(!type.equals("edge"))) System.out.println("warning, did not recognise type. defaulting to edge") ;
                if (processes == 1) {
                    client.requestfile("Programs/", language, "ProgramsClient/");
                    try {
                        client.runTest("gcc -o C_ColorToGrayImage ProgramsClient/C_ColorToGrayImage.c -ljpeg","./C_ColorToGrayImage");
                        Instant end = Instant.now() ;
                        time = "time: " +  String.format("%.3f",Duration.between(start, end).toMillis()/1000.0) + " seconds.";
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    
                    client.deleteFile("ProgramsClient/"+language) ;
                    client.deleteFile("C_ColorToGrayImage");
                    
                }
                else if (processes >= 1){

                }
                
                //run program
                //delete program
                

            }
            //client.sendInt(9);
            //System.out.println("I just got: " + client.recieveInt());
            //client.recieveFolder("Output/");
            //client.recieveFile("Output/") ;
            client.emptyFolder("Output") ;
            System.out.println(time);
            client.dos.writeUTF("done");
            client.dos.flush() ;
            client.close() ;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    

    

}
