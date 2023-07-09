// to compile: javac MainC.java
// to run: java MainC judahsbase.tplinkdns.com 51820
//51820 is the reserved port at this time.
// javac MainC.java ; java MainC judahsbase.tplinkdns.com 51820

//make folder to dynamically store file

import java.io.* ;
import java.net.* ;
import java.util.Base64;
import java.util.Scanner;
import java.awt.image.*;

import javax.imageio.ImageIO;


public class MainC {

    private static Socket socket ;
    private static BufferedReader bufferedReader ;
    private static BufferedWriter bufferedWriter ;
    private static Scanner scanner ;
    public static void main(String[] args) {
        // Note to judah, args[0] is first argument after MainC.
        if (args.length < 2) {
            System.out.println("Not enough arguments, needs two");
            System.exit(-1) ;
        }
        
        String serverIP = args[0] ;
        int serverPort = Integer.parseInt(args[1]) ;

        try {
            int check = 1 ;
            socket = new Socket(serverIP, serverPort) ;
            System.out.println("Connected to Server (server stops after this program closes)") ;
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream())) ;
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())) ;
            scanner = new Scanner(System.in) ; //This is in main just in case we need scanner again.
            // Assume one client with one socket for now

            while(check == 1) {
                check = baseMenu();
            }
            bufferedWriter.write("4") ;
            bufferedWriter.newLine() ;
            bufferedWriter.flush() ;
            endit() ;
        } catch (IOException e) {
            System.out.println("possibly connection refused.");
            e.printStackTrace() ;
            endit() ;
        }

    }

    public static int baseMenu() throws IOException {
        int p_choice ;
        int result = menu(scanner) ; //assume 3 for now
            switch (result) {
                case 2:
                    //cloud() ;
                    pullOptions() ;
                    System.out.print("Which program to run?: ");
                    scanner.nextLine() ;
                    p_choice = scanner.nextInt() ;  //assume first for now.
                    bufferedWriter.write("" + result) ;
                    bufferedWriter.newLine() ;
                    bufferedWriter.flush() ;
                    cloud(p_choice) ;
                    return 1;
                case 3:
                    pullOptions() ;
                    System.out.print("Which program to run?: ");
                    p_choice = scanner.nextInt() ;  //assume first for now.
                    scanner.nextLine() ;    
                    bufferedWriter.write("" + result) ;
                    bufferedWriter.newLine() ;
                    bufferedWriter.flush() ;
                    edge(p_choice) ;
                    return 1 ;
                case 4: //close program.
                    return 0 ; // should not return
                default:
                    System.out.println("incorrect input, restarts program.");
                    return 1 ;

            }
    }

    public static void endit() {
        try {
            if (scanner != null) {
                scanner.close() ; 
            }
            if (bufferedWriter != null) {
                bufferedWriter.close() ;
            }       
            if (bufferedReader != null) {
                bufferedReader.close();
            }        
            if(socket != null) {
                socket.close();
            } 
            System.exit(1) ;
        } catch (IOException e) {
            System.out.println("This broke bad.");
            System.exit(-1) ;
        }

    }

    public static int menu(Scanner scanner) {
        //Assume Program list is static for now.
        // 1 = Cloud
        // 2 = Edge
        System.out.print("Enter 2 for Cloud, 3 for Edge computing, or 4 to end program: ");
        return scanner.nextInt() ;
    }

    public static void pullOptions() {
        // Order of Operaitons: sends a 1 to server
         try {
            bufferedWriter.write("1") ;
            bufferedWriter.newLine() ;
            bufferedWriter.flush() ;
            int length = Integer.parseInt(bufferedReader.readLine());
            System.out.println("Recieved length: " + length);
            int tmpLength = 0 ;
            StringBuilder recievedString = new StringBuilder() ;
            char[] buffer = new char[8192] ;
            int bytesRead ;
            int left = 8192 ;


            while (tmpLength < length) {
                left = length - tmpLength ;
                if (left > 8192) left = 8192 ;
                bytesRead = bufferedReader.read(buffer, 0, left) ;
                if (bytesRead == -1) endit();
                recievedString.append(buffer, 0, bytesRead) ;
                tmpLength = tmpLength + bytesRead ;
            }
            bufferedWriter.write("ok\n") ;
            bufferedWriter.flush() ;

            String[] lines = recievedString.toString().split("\n") ;
            System.out.println(lines[0]);
            for (int i = 1; i < lines.length; i ++) {
                System.out.println(i + " " + lines[i]);
            }
        } catch (IOException e) {
            e.printStackTrace() ;
            endit();
        }
    }

    public static void edge(int p_choice) throws IOException {
        //start timer
        long startTime = System.nanoTime() ;

        // send request
        bufferedWriter.write("" + p_choice) ;
        bufferedWriter.newLine() ;
        bufferedWriter.flush() ;
        String folderPath = "ProgramsC/" ;

        //saving and storing the file.
        System.out.println("going into recv");
        String p_name = recvFile(folderPath) ;
        System.out.println("recv good");

        //run process. 
        try {
            String programName = folderPath + p_name ;
            String command = "java " + programName ;
            Process process = Runtime.getRuntime().exec(command) ;
            int exitCode = process.waitFor() ;
            if (exitCode == 0) {
                System.out.println(programName + " executed successfully.") ;
            } else {
                System.out.println(programName + " execution failed with exit code: " + exitCode) ;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace() ;
        }

        //stop timer
        long endtime = System.nanoTime() ;
        long elapsedTime = endtime - startTime ;
        double dur = (double) elapsedTime/1_000_000_000 ;
        System.out.println("Elapsed time: " + String.format("%.6f", dur) + " seconds") ;

        //delete
        defFile(folderPath, p_name);
        
    }

    public static void defFile(String folderPath, String filename) {
        File fileToDelte = new File(folderPath, filename) ;
        
        if (fileToDelte.exists()) {
            boolean deletionStatus = fileToDelte.delete() ;

         if (deletionStatus) {
                System.out.println("File " + filename + " deleted successfully.");
            } else {
                System.out.println("Unable to delete the file " + filename);
            }
        } else {
            System.out.println("File " + filename + " does not exist.");
        }   
    }

    public static String recvFile( String directory) {
        String filePath ;
        try {
            filePath = bufferedReader.readLine() ; //
            System.out.println("Revieved name.");
            // Read the file content from the server
            StringBuilder fileContent = new StringBuilder();
            String line;

            line = bufferedReader.readLine() ;
            while (line.equals("1")) {
                line = bufferedReader.readLine() ;
                fileContent.append(line) ;
                fileContent.append(System.lineSeparator()) ;
                line = bufferedReader.readLine() ;
            }
            System.out.println("Recieved file.");

            // Determine the file name based on the sent file path
            String sentFilePath = null ;
            if (fileContent.length() > 0) {
                sentFilePath = fileContent.toString().trim() ;
            }

            // Save the received file in the ProgramC folder
            if (sentFilePath != null) {
                String receivedFilePath = directory + filePath ;
                File receivedFile = new File(receivedFilePath) ;
                BufferedWriter fileWriter = new BufferedWriter(new FileWriter(receivedFile)) ;
                fileWriter.write(fileContent.toString()) ;
                fileWriter.close() ;
                System.out.println("File received and saved: " + receivedFilePath) ;
            } else {
                System.out.println("Invalid file content. No file received.") ;
            }
            return filePath ;
        } catch (IOException e) {
            e.printStackTrace() ;
        }
        return "!!Did not recieve file!!" ;
    }

    public static void cloud(int p_choice) throws IOException {
        //start timer
        long startTime = System.nanoTime() ;

        //send choice
        bufferedWriter.write("" + p_choice) ;
        bufferedWriter.newLine() ;
        bufferedWriter.flush() ;

        //send input
        bufferedReader.readLine(); 
        sendFolder(); //sends inputFolder
        

        //recieve output
        recvFolder() ;

        //stop timer
        long endtime = System.nanoTime() ;
        long elapsedTime = endtime - startTime ;
        double dur = (double) elapsedTime/1_000_000_000 ;
        System.out.println("Elapsed time: " + String.format("%.6f", dur) + " seconds") ;


        //go back to menu
    }

    public static void sendFolder() {
        String sourceFolder = "Input";

        try {
            File folder = new File(sourceFolder);
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        bufferedWriter.write(file.getName());
                        bufferedWriter.newLine();
                        bufferedWriter.flush();

                        // Transfer the image from sourceFolder to destinationFolder
                        try (InputStream fileReader = new FileInputStream(file)) {
                            BufferedImage image = ImageIO.read(fileReader);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write((RenderedImage) image, "jpg", baos);
                            baos.flush();

                            byte[] imageData = baos.toByteArray();
                            bufferedWriter.write(Base64.getEncoder().encodeToString(imageData));
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
        String destinationFolder = "Output";
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
}

