
/*
 * At the name implies, 
 * the point of this program is to 
 * run python programs.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PythonRunner {

    public void simpleRun(String filename) {
        String[] command = {"python3", filename} ;
        int exitCode = 0 ;

        try {
            Process process = Runtime.getRuntime().exec(command);

            // Capture standard output and error streams for display
            BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;  

            // Print standard output line by line
            while ((line = stdOut.readLine()) != null) {
                System.out.println("Standard Output: " + line);
            }

            // Print standard error if any
            while ((line = stdErr.readLine()) != null) {
                System.out.println("Standard Error: " + line);
            }

            exitCode = process.waitFor();

            // Check exit code and handle potential errors
            if (exitCode == 0) {
                System.out.println("Python script execution successful.");
            } else {
                System.out.println("Python script execution failed with exit code: " + exitCode);
            }

            stdOut.close();
            stdErr.close();
        } catch (IOException e) {
            System.err.println("Error while executing Python script: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Interrupted while waiting for process: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        PythonRunner pythonRunner = new PythonRunner() ;
        pythonRunner.simpleRun("test.py") ;
    }
    
}
