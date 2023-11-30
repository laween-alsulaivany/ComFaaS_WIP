// to compile: javac MainS.java
// to run: java MainS 51820
//51820 is the reserved port at this time.
// rm -r *.class ; javac MainS.java ; java MainS 51820
/*
 * is controlled by the internal menu from client.
 * 
 * 
 */

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.* ;
import java.net.* ;
import java.util.Base64;

import javax.imageio.ImageIO;

public class MainS {

    private static ServerSocket  serverSocket ;
    private static Socket socket ;
    private static BufferedReader bufferedReader ;
    private static BufferedWriter bufferedWriter ;

    private static File[] files ;
    private static char[] zeros = new char[8192] ;
    private static String[] arg ;
    
    public static void main(String[] args) {

        if (args.length < 1) { //Argument check
            System.out.println("Not enough arguments.") ;
            System.exit(-1) ;
        }
        arg = args ;
        
        try {
            initalize() ;

            String option = "1";
            while ((option = bufferedReader.readLine()) != null) {
                baseMenu(Integer.parseInt(option));
            }
            System.out.println("socket closed.");
            endit();
        } catch (IOException e) {
            System.out.println("Broke in main try block.");
            e.printStackTrace() ;
        } catch (NumberFormatException e) {
            e.printStackTrace() ;
        }
    }

    public static void baseMenu(int op) throws IOException {
        switch(op) {
            case 1:
                System.out.println("Starting option 1: sending list of ");
                options() ;
                System.out.println("Option 1 Completed: back to menu.");
                break ;
            case 2:
                // We are going to first run the cloud option.
                // 
                System.out.println("option 2");
                cloud() ;
                break ;
            case 3:
                System.out.println("option 3");
                edge() ;
                break ;
            case 4:
                System.out.println("option 4");
                endit() ;
                break ;
            default:
                System.out.println("option vaiable did not give a proper input.") ;
                System.out.println("Closing out server.") ;
                endit() ; 
        }
    }

    public static void endit() {
        try {
            if (bufferedWriter != null) {
                bufferedWriter.close() ;
            }       
            if (bufferedReader != null) {
                bufferedReader.close();
            }        
            if(!socket.isClosed()) {
                socket.close();
            } 
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
            System.exit(1) ;
        } catch (IOException e) {
            System.out.println("Broke at endit(), not known where in main.");
            e.printStackTrace() ;
            System.exit(-1) ;
        }
    }
    
    public static String pullFiles() {
        String folderPath = "Programs" ;
        String ops = "" ;
        File folder = new File(folderPath) ;
        if (folder.exists() && folder.isDirectory()) {
            files = folder.listFiles() ;
            if (files != null && files.length > 0) {
                ops += "Programs to run: \n" ;
                for (File file: files) {
                    if (file.isFile()) {
                        ops += file.getName() + "\n" ;
                    }
                }
            } else {
                ops += "Error: No files to use. \n";
            }
        } else {
            ops += "Invalid folder path or the folder does not exist.\n" ;
        }
        return ops ;
    }
    
    public static void options() {
        String strFiles = pullFiles() ;
        int length = strFiles.length() ;
        int offset = 0 ;
        int chunksize = 8192 ;
        try {
            bufferedWriter.write(String.valueOf(length)); //non blocking
            bufferedWriter.newLine() ;
            bufferedWriter.flush() ;
            while (offset < length) {
                int endIndex = offset + chunksize;
                if (endIndex > length) {
                    endIndex = length;
                }

                String chunk = strFiles.substring(offset, endIndex) ;
                try {
                    System.out.println("sending");
                    bufferedWriter.write(chunk) ;
                    bufferedWriter.flush();
                    System.out.println("sent");
                } catch (IOException e) {
                    System.out.println("Broke at optinos -> while sending loop.");
                    e.printStackTrace();
                    endit() ;
                }
                offset += chunksize;
            }
            bufferedReader.readLine() ;
            System.out.println("Done");
        }
        catch (IOException e) {
            e.printStackTrace() ;
        }
    }

    public static void initalize() throws NumberFormatException, IOException {
        //Initializes the socket and buffers
        serverSocket = new ServerSocket(Integer.parseInt(arg[0])) ;
        socket = serverSocket.accept() ; // this will block until it recieves a connection and it will accept.
        System.out.println("Connected to client");
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream())) ;
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())) ;

    }

    public static void edge() {

        try {
        // Recieving the choice
        String recv = bufferedReader.readLine() ;
        //pull and send file.
        sendFile(Integer.parseInt(recv));
        } catch (IOException e) {
            e.printStackTrace() ;
            endit() ;
        }

        // go back to start.
        System.out.println("edge responcibility over.");
    }

    public static void sendFile(int n) {
    // Based on n, choose the file to send
        String filePath ;
        switch (n) {
            case 1:
                filePath = "ImageSharpenerBenchmark.java";
                break;
            case 2:
                filePath = "ImageFilterBenchmark.java";
                break;
            case 3:
                filePath = "FPOSineBenchmark.java";
                break;
            case 4:
                filePath = "ImageResizeBenchmark.java";
                break;
            case 5:
                filePath = "FPOSquareRootBenchmark.java" ;
                break;
            case 6:
                filePath = "MonteCarloSimulationBenchmark.java" ;
                break ;
            case 7:
		filePath = "ColorToGrayImage.java" ;
		break ;
            default:
                System.out.println(n);
                System.out.println("Invalid file number. No file received.");
                return;
        }
        try {
            bufferedWriter.write(filePath) ; //
            bufferedWriter.newLine() ; //
            bufferedWriter.flush() ; //
            File file = new File("Programs/" + filePath);
            if (file.exists()) {
                BufferedReader fileReader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = fileReader.readLine()) != null) {
                    bufferedWriter.write("1") ;
                    bufferedWriter.newLine() ;
                    bufferedWriter.flush() ;
                    bufferedWriter.write(line) ;
                    bufferedWriter.newLine() ;
                    bufferedWriter.flush() ;
                }
                fileReader.close();
                bufferedWriter.write("0") ;
                bufferedWriter.newLine() ;
                bufferedWriter.flush();
                System.out.println("File sent to the client.");
            } else {
                System.out.println("File does not exist: " + filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void cloud() {

        try {
            // Recieving the choice
            System.out.println("collecting filename");
            String recv = bufferedReader.readLine() ;
            System.out.println("got: " + recv);
            String filePath ;
            switch (Integer.parseInt(recv)) {
                case 1:
                    filePath = "ImageSharpenerBenchmark.java";
                    break;
                case 2:
                    filePath = "ImageFilterBenchmark.java";
                    break;
                case 3:
                    filePath = "FPOSineBenchmark.java";
                    break;
                case 4:
                    filePath = "ImageResizeBenchmark.java";
                    break;
                case 5:
                    filePath = "FPOSquareRootBenchmark.java" ;
                    break;
                case 6:
                    filePath = "MonteCarloSimulationBenchmark.java" ;
                    break ;
		case 7:
		    filePath = "ColorToGrayImage.java" ;
		    break ;
                default:
                    System.out.println(recv);
                    System.out.println("Invalid file number. No file received.");
                    return;
            }
            
            //recives the input.
            System.out.println("sending ok");
            bufferedWriter.write("ok") ;
            bufferedWriter.newLine() ;
            bufferedWriter.flush() ;
            System.out.println("recieving files");
            recvFolder();

            
            //run the program
            System.out.println("starting work");
            String folderPath = " Programs/" ;
            try {
                String programName = folderPath + filePath ;
                String command = "java" + programName ;
                Process process = Runtime.getRuntime().exec(command) ;
                int exitCode = process.waitFor();
                if(exitCode == 0 ) {
                    System.out.println(programName + " executed successfully.") ;
                }
                else {
                    System.out.println(programName + " execution failed with exit code: " + exitCode);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            //send the folder
            sendFolder() ;

            defFolder("Input");
            defFolder("Output");
            System.out.println("Exiting cloud");

        } catch (IOException e) {
            e.printStackTrace() ;
            endit() ;
        }
    }

    public static void sendFolder() {
        String sourceFolder = "Output";

        try {
            String ext ;
            File folder = new File(sourceFolder);
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        bufferedWriter.write(file.getName());
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                        System.out.println(file.getName());
                        ext = file.getName() ;
                        ext = ext.substring(ext.length()-4, ext.length());
                        System.out.println(ext);

                        // Transfer the image from sourceFolder to destinationFolder
                        try (InputStream fileReader = new FileInputStream(file)) {
                            if (ext.equals(".jpg")){
                            BufferedImage image = ImageIO.read(fileReader);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write((RenderedImage) image, "jpg", baos);
                            baos.flush();
                            byte[] imageData = baos.toByteArray();
                            bufferedWriter.write(Base64.getEncoder().encodeToString(imageData));
                            }
                            else if (ext.equals(".txt")){
                                byte[] fileData = fileReader.readAllBytes();
                                String base64Data = Base64.getEncoder().encodeToString(fileData);
                                bufferedWriter.write(base64Data);
                            }

                            
                            
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                        }
                        bufferedWriter.write("END");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
                }
                bufferedWriter.write("END");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
            System.out.println("Image files sent to the client.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void recvFolder() {
        String destinationFolder = "Input";
        try {
            String fileName;
            while ((fileName = bufferedReader.readLine()) != null) {
                if (fileName.equals("END")) {
                    break;
                }

                File file = new File(destinationFolder, fileName);
                try (OutputStream fileWriter = new FileOutputStream(file)) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        if (line.equals("END")) {
                            break;
                        }
                        byte[] imageData = Base64.getDecoder().decode(line);
                        fileWriter.write(imageData);
                        fileWriter.flush();
                    }
                }
            }
            System.out.println("Image files received and saved in the destination folder: " + destinationFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void defFolder(String folderPath) {
         File folder = new File(folderPath);

        // Check if the folder exists
        if (folder.exists()) {
            // Get all the files and subfolders inside the folder
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        // Recursive call to delete subfolder contents
                        defFolder(file.getAbsolutePath());
                    } else {
                        // Delete file
                        boolean deletionStatus = file.delete();
                        if (deletionStatus) {
                            System.out.println("Deleted file: " + file.getAbsolutePath());
                        } else {
                            System.out.println("Failed to delete file: " + file.getAbsolutePath());
                        }
                    }
                }
            }
        } else {
            System.out.println("Folder does not exist: " + folderPath);
        }
    }
}

    
