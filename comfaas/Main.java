package comfaas;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

public class Main {

    public static void main(String[] args) {

        if (args.length > 1) {
            // These first two if's just are for setting up the init's
            if (args[0].equals("server") && args[1].equals("init")) {
                // make the directories for server
                Path directory1 = Paths.get("ServerPrograms") ;
                Path directory2 = Paths.get("ServerInput") ;
                Path directory3 = Paths.get("ServerOutput") ;
                String[] arguments = new String[1] ;
                arguments[0] = "mpiPython" ;
                try {
                    PythonRunner.createVirtualEnv(".serverVenv") ;
                    PythonRunner.installPackages(".serverVenv", arguments) ;
                    if (!Files.exists(directory1)) {
                        Files.createDirectories(directory1) ;
                    }
                    if (!Files.exists(directory2)) {
                        Files.createDirectories(directory2) ;
                    }
                    if (!Files.exists(directory3)) {
                        Files.createDirectories(directory3) ;
                    }
                } catch (IOException e) {
                    e.printStackTrace() ;
                    System.exit(1);
                } catch (InterruptedException e) {
                    e.printStackTrace() ;
                    System.exit(1);
                }
            }
            else if (args[0].equals("edge") && args[1].equals("init")) {
                // make the directories for edge
                Path directory1 = Paths.get("EdgePrograms") ;
                Path directory2 = Paths.get("EdgeInput") ;
                Path directory3 = Paths.get("EdgeOutput") ;
                String[] arguments = new String[1] ;
                arguments[0] = "mpiPython" ;
                try {
                    PythonRunner.createVirtualEnv(".edgeVenv") ;
                    PythonRunner.installPackages(".edgeVenv", arguments) ;
                    if (!Files.exists(directory1)) {
                        Files.createDirectories(directory1) ;
                    }
                    if (!Files.exists(directory2)) {
                        Files.createDirectories(directory2) ;
                    }
                    if (!Files.exists(directory3)) {
                        Files.createDirectories(directory3) ;
                    }
                } catch (IOException e) {
                    e.printStackTrace() ;
                    System.exit(1);
                } catch (InterruptedException e) {
                    e.printStackTrace() ;
                    System.exit(1);
                }
            }
            else {
                // The main code. 
                if (args[0].equals("server") && args[1].equals("run")) {
                    Path directory1 = Paths.get("ServerPrograms") ;
                    Path directory2 = Paths.get("ServerInput") ;
                    Path directory3 = Paths.get("ServerOutput") ;
                    try {
                        // PythonRunner.createVirtualEnv(".serverVenv") ;
                        if (!Files.exists(directory1)) {
                            Files.createDirectories(directory1) ;
                        }
                        if (!Files.exists(directory2)) {
                            Files.createDirectories(directory2) ;
                        }
                        if (!Files.exists(directory3)) {
                            Files.createDirectories(directory3) ;
                        }
                    } catch (IOException e) {
                        e.printStackTrace() ;
                        System.exit(1);
                    // } catch (InterruptedException e) {
                    //     e.printStackTrace() ;
                    //     System.exit(1);
                    }

                    final int port = 12353 ;
                    try {
                        Server.run(port) ;
                    } catch (UnknownHostException e) {
                        e.printStackTrace() ;
                    } catch (IOException e) {
                        e.printStackTrace() ;
                    }
                    
                    
                }
                else if (args[0].equals("edge") && args[1].equals("run")) {
                    Path directory1 = Paths.get("EdgePrograms") ;
                    Path directory2 = Paths.get("EdgeInput") ;
                    Path directory3 = Paths.get("EdgeOutput") ;
                    try {
                        // PythonRunner.createVirtualEnv(".edgeVenv") ;
                        if (!Files.exists(directory1)) {
                            Files.createDirectories(directory1) ;
                        }
                        if (!Files.exists(directory2)) {
                            Files.createDirectories(directory2) ;
                        }
                        if (!Files.exists(directory3)) {
                            Files.createDirectories(directory3) ;
                        }
                    } catch (IOException e) {
                        e.printStackTrace() ;
                        System.exit(1);
                    // } catch (InterruptedException e) {
                    //     e.printStackTrace() ;
                    //     System.exit(1);
                    }

                    Instant start = Instant.now() ;
                    String time ;
                    
                    try {
                        Edge edge = new Edge() ;
                        edge.init(args) ;
                        edge.run() ;

                        Instant end = Instant.now() ;
                        time = "time: " +  String.format("%.3f",Duration.between(start, end).toMillis()/1000.0) + " seconds.";
                        System.out.println(time) ;

                    } catch (IOException e) {
                        e.printStackTrace() ;
                        
                    }
                    }
            }
        }
    }

}
