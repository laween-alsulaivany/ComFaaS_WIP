package com.parallelsolvit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import com.parallelsolvit.TheProgramRunner;

public class Main {
     // Optional main method for testing purposes.
    public static void main(String[] args) {
        // Adjust these paths for your environment.
        String homeDirectory = System.getProperty("user.home");
        String venvBin = homeDirectory + "/Judahswork/ComFaaS_WIP/.serverVenv/bin";
        String progs = homeDirectory + "/Judahswork/ComFaaS_WIP/tests/SamplePrograms";

        AbstractProgramRunner runner = new TheProgramRunner(venvBin, progs);

        try {
            // ------------------------
            // Test 1: Serial Java Program
            // ------------------------
            Path tempInput1 = Files.createTempDirectory(Paths.get(homeDirectory), "tempInput1_");
            Path tempOutput1 = Files.createTempDirectory(Paths.get(homeDirectory), "tempOutput1_");
            System.out.println("Starting java test");
            runner.run("WaitFor3Seconds.java", tempInput1.toString(), tempOutput1.toString());
            deleteDirectory(tempInput1);
            deleteDirectory(tempOutput1);

            // ------------------------
            // Test 2: Parallel MPI C Program
            // ------------------------
            Path tempInput2 = Files.createTempDirectory(Paths.get(homeDirectory), "tempInput2_");
            Path tempOutput2 = Files.createTempDirectory(Paths.get(homeDirectory), "tempOutput2_");
            System.out.println("Starting test two");
            runner.run("mpich_pi_reduce.c", tempInput2.toString(), tempOutput2.toString(), 2);
            deleteDirectory(tempInput2);
            deleteDirectory(tempOutput2);

            // ------------------------
            // Test 3: Parallel MPI Python Script
            // ------------------------
            Path tempInput3 = Files.createTempDirectory(Paths.get(homeDirectory), "tempInput3_");
            Path tempOutput3 = Files.createTempDirectory(Paths.get(homeDirectory), "tempOutput3_");
            System.out.println("Starting test three");
            runner.run("mpiPython_test.py", tempInput3.toString(), tempOutput3.toString(), 4);
            deleteDirectory(tempInput3);
            deleteDirectory(tempOutput3);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recursively deletes a directory and all its contents.
     */
    private static void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) return;
        Files.walk(path)
             .sorted(Comparator.reverseOrder())
             .forEach(p -> {
                 try {
                     Files.delete(p);
                 } catch (IOException e) {
                     System.err.println("Failed to delete " + p + ": " + e.getMessage());
                 }
             });
    }
}