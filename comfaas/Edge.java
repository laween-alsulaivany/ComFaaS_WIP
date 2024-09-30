package comfaas;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Edge extends CoreOperations{

    public Socket socket ;
    public Object[] metaData ;

    public String server = null ;
    public Integer port = -1 ;
    public String type = null ;
    public Integer np = -1 ;
    public Integer tid = -1 ;
    public String tn = null ;
    public boolean kFlag = false ;
    public String lang = null ;
    public String[] args ;

    // folderDirectorie for Edge
    
    public Edge() {

    }

    public void init(String[] args) {
        if (args.length < 6) {
            System.out.println("Invalid arguments");
            System.exit(1);
        }
        this.args = args ;
        CommandLineProcessor.processArguments(this) ;

        try {
            this.socket = new Socket(this.server, this.port) ;
            this.inStream = this.socket.getInputStream();
            this.dis = new DataInputStream(this.inStream) ;
            this.outStream = socket.getOutputStream() ;
            this.dos = new DataOutputStream(this.outStream) ;

            metaData = new Object[5];
            metaData[0] = type ; //Cloud, edge
            metaData[1] = lang ; //Python, Java, C
            metaData[2] = np ; //Number of Processes, 1 is sequential
            // metaData[3] = tid ; //Test ID
            metaData[3] = tn ; //name of program
            metaData[4] = kFlag ; //the Kill flag, true means kill the server.
        } catch (UnknownHostException e) {
            e.printStackTrace() ;
        } catch (IOException e) {
            e.printStackTrace() ;
        }

    }

    public void close() 
            throws IOException {
    /*
     * Closes all data streams and main socket.
     */

        this.dis.close() ;
        this.dos.close() ;
        this.inStream.close() ;
        this.outStream.close() ;
        this.socket.close() ;
    }

    public void run() throws IOException {
        this.emptyFolder(edgeOutputFolder) ;
        
        if(this.type.equals("cloud")) {
            this.loadFiles(serverInputFolder, edgeInputFolder);
            
            runProgramOnCloud(this.lang, this.tn, this.np) ;
            this.requestFolder(serverOutputFolder,edgeOutputFolder) ;
            // System.out.println("File Recieved.");
            
        }
        else if(this.type.equals("edge")) {
            this.requestfile(serverProgramsFolder, this.tn, edgeProgramsFolder) ;
            if (this.np == 1) {
                if (this.lang.equals("Python")||this.lang.equals("python")) {
                    try {
                        // System.out.println("----before----");
                        // PythonRunner.runPythonScript(edgeProgramsFolder+"/"+this.tn);
                        PythonRunner.runPythonScriptInVenv(edgeVenv, edgeProgramsFolder+"/"+this.tn);
                        // System.out.println("----after----");
                    } catch (IOException | InterruptedException e) {
                        this.deleteFile(edgeProgramsFolder+"/"+this.tn) ;
                        e.printStackTrace();
                        this.close() ;
                        System.exit(1) ;
                    }
                    this.deleteFile(edgeProgramsFolder+"/"+this.tn) ;
                    
                } else if (this.lang.equals("java")) {
                    this.moveFiles(edgeProgramsFolder+"/", ".");
                    JavaProgramRunner runner = new JavaProgramRunner();
                    try {
                        if (runner.compileJavaProgram(this.tn)) {
                            // System.out.println("----before----");
                            String className = runner.getClassName(this.tn);
                            // System.out.println("Class Name: "+className) ;
                            runner.runJavaProgram(className);
                            // System.out.println("----after----");
                            this.deleteFile(this.tn) ;
                            this.deleteFile(className+".class") ; // This needs to be changed out of the if scope in the future.
                        }
                        
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                        this.close() ;
                        System.exit(1) ;
                    }
                    // this.deleteFile(edgeProgramsFolder+"/"+this.tn) ;
                } else if (this.lang.equals("C")) {
                    CProgramRunner runner = new CProgramRunner();
                    String cSourceFilePath = edgeProgramsFolder +"/"+ this.tn ;
                    try {
                        if (runner.compileCProgram(cSourceFilePath)) {
                            // System.out.println("----before----");
                            runner.runCProgram(cSourceFilePath);
                            // System.out.println("----after----");
                            runner.deleteExecutable(cSourceFilePath);
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                        this.close() ;
                        System.exit(1) ;
                    }
                    this.deleteFile(edgeProgramsFolder+"/"+this.tn) ;
                }
            } else if (this.np > 1) {
                if (this.lang.equals("Python")||this.lang.equals("python")) {
                    try {
                        // System.out.println("----before----") ;
                        // PythonRunner.runPythonScript(serverProgramsFolder+"/"+program) ;
                        // PythonRunner.runPythonScriptInVenv(serverVenv, serverProgramsFolder+"/"+program) ;
                        PythonRunner.runPythonScriptWithMpi(edgeVenv, edgeProgramsFolder+"/"+this.tn, np) ;
                        // System.out.println("----after----") ;
                    } catch (IOException | InterruptedException e) {
                        
                        e.printStackTrace() ;
                        throw new IOException() ;
                        
                    }
                    this.deleteFile(this.tn) ;
                } else if (this.lang.equals("C")) {
                    CProgramRunner runner = new CProgramRunner();
                    String cSourceFilePath = edgeProgramsFolder +"/"+ this.tn ;
                    try {
                        if (runner.compileMPICHProgram(cSourceFilePath)) {
                            // System.out.println("----before----");
                            runner.runMPICHProgram(cSourceFilePath, this.np);
                            // System.out.println("----after----");
                            runner.deleteExecutable(cSourceFilePath);
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                        this.close() ;
                        System.exit(1) ;
                    }
                    this.deleteFile(edgeProgramsFolder+"/"+this.tn) ;
                }

            }
            
            // this.deleteFile("C_ColorToGrayImage");
        }
        else {
            System.err.println("client type is not valid. Only cloud or edge are allowed.");
            System.exit(1);
        }
        this.dos.writeUTF("done");
        this.dos.flush() ;
        this.close() ;
    }
}
