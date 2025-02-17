package comfaas ;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of AbstractProgramRunner.
 * Supports running Java, C, and Python programs (both serial and parallel using MPICH).
 */
public class TheProgramRunner extends AbstractProgramRunner {

    public TheProgramRunner(String venvBinFolder, String programsFolder) {
        super(venvBinFolder, programsFolder);
    }

    @Override
    public void run(String fileName, String inputFolder, String outputFolder)
            throws IOException, InterruptedException {
        runInternal(fileName, inputFolder, outputFolder, 1);
    }

    @Override
    public void run(String fileName, String inputFolder, String outputFolder, int numberOfProcesses)
            throws IOException, InterruptedException {
        runInternal(fileName, inputFolder, outputFolder, numberOfProcesses);
    }

    /**
     * Internal method that handles creating a temporary directory, copying files,
     * compiling (if needed), running the program, and cleaning up.
     */
    private void runInternal(String fileName, String inputFolder, String outputFolder, int numberOfProcesses)
            throws IOException, InterruptedException {

        // Create a temporary directory inside the programs folder.
        Path parentPath = Paths.get(programsFolder);
        if (!Files.isDirectory(parentPath)) {
            throw new IOException("Programs folder does not exist or is not a directory: " + programsFolder);
        }
        Path tempDir = Files.createTempDirectory(parentPath, "tempRun_");
        System.out.println("[INFO] Temporary directory created: " + tempDir);

        try {
            // Copy the program file from programsFolder to the temporary directory.
            Path srcProgram = parentPath.resolve(fileName);
            Path destProgram = tempDir.resolve(fileName);
            Files.copy(srcProgram, destProgram, StandardCopyOption.REPLACE_EXISTING);

            // If input data is provided, copy it into the temporary directory.
            if (inputFolder != null && !inputFolder.isBlank()) {
                copyFolder(Paths.get(inputFolder), tempDir);
            }

            // Determine the file type based on the extension.
            String extension = getFileExtension(fileName).toLowerCase();
            if (extension.equals("java")) {
                runJava(tempDir, fileName, numberOfProcesses);
            } else if (extension.equals("c")) {
                runC(tempDir, fileName, numberOfProcesses);
            } else if (extension.equals("py")) {
                runPython(tempDir, fileName, numberOfProcesses);
            } else {
                throw new IOException("Unsupported file type: " + fileName);
            }

            // After the run, if outputFolder is specified, copy output data from tempDir to outputFolder.
            if (outputFolder != null && !outputFolder.isBlank()) {
                copyFolder(tempDir, Paths.get(outputFolder));
            }
        } finally {
            // Clean up by deleting the temporary directory and its contents.
            deleteDirectory(tempDir);
            System.out.println("[INFO] Temporary directory deleted: " + tempDir);
        }
    }

    /**
     * Runs a Java program. Compiles the .java file, then runs it.
     */
    private void runJava(Path workingDir, String fileName, int numberOfProcesses)
            throws IOException, InterruptedException {
        // Assume the main class name is the file name without the ".java" extension.
        String className = fileName.replaceFirst("\\.java$", "");

        // Compile the Java source file.
        ProcessBuilder javacBuilder = new ProcessBuilder("javac", fileName);
        javacBuilder.directory(workingDir.toFile());
        System.out.println("[INFO] Compiling Java: " + String.join(" ", javacBuilder.command()));
        Process javacProcess = javacBuilder.start();
        captureProcessOutput(javacProcess);
        int javacExit = javacProcess.waitFor();
        if (javacExit != 0) {
            throw new IOException("javac compilation failed for " + fileName);
        }

        // Run the compiled Java class.
        List<String> runCommand = new ArrayList<>();
        if (numberOfProcesses > 1) {
            // For parallel Java, we assume an MPI-enabled Java environment.
            runCommand.add("mpirun");
            runCommand.add("-np");
            runCommand.add(String.valueOf(numberOfProcesses));
            runCommand.add("java");
        } else {
            runCommand.add("java");
        }
        runCommand.add(className);

        ProcessBuilder runBuilder = new ProcessBuilder(runCommand);
        runBuilder.directory(workingDir.toFile());
        System.out.println("[INFO] Running Java: " + String.join(" ", runBuilder.command()));
        Process runProcess = runBuilder.start();
        captureProcessOutput(runProcess);
        int runExit = runProcess.waitFor();
        if (runExit != 0) {
            throw new IOException("Java execution failed for " + className);
        }
    }

    /**
     * Runs a C program. Compiles with gcc (serial) or mpicc (parallel) and runs it.
     */
    private void runC(Path workingDir, String fileName, int numberOfProcesses)
            throws IOException, InterruptedException {
        String outputName = fileName.replaceFirst("\\.c$", "");
        if (numberOfProcesses > 1) {
            // Compile with mpicc.
            ProcessBuilder mpiccBuilder = new ProcessBuilder("mpicc", "-o", outputName, fileName);
            mpiccBuilder.directory(workingDir.toFile());
            System.out.println("[INFO] Compiling C (mpicc): " + String.join(" ", mpiccBuilder.command()));
            Process mpiccProcess = mpiccBuilder.start();
            captureProcessOutput(mpiccProcess);
            int mpiccExit = mpiccProcess.waitFor();
            if (mpiccExit != 0) {
                throw new IOException("mpicc compilation failed for " + fileName);
            }
            // Run using mpirun.
            ProcessBuilder mpirunBuilder = new ProcessBuilder("mpirun", "-np", String.valueOf(numberOfProcesses), "./" + outputName);
            mpirunBuilder.directory(workingDir.toFile());
            System.out.println("[INFO] Running C (mpirun): " + String.join(" ", mpirunBuilder.command()));
            Process mpirunProcess = mpirunBuilder.start();
            captureProcessOutput(mpirunProcess);
            int mpirunExit = mpirunProcess.waitFor();
            if (mpirunExit != 0) {
                throw new IOException("mpirun execution failed for " + outputName);
            }
        } else {
            // Compile with gcc.
            ProcessBuilder gccBuilder = new ProcessBuilder("gcc", "-o", outputName, fileName);
            gccBuilder.directory(workingDir.toFile());
            System.out.println("[INFO] Compiling C (gcc): " + String.join(" ", gccBuilder.command()));
            Process gccProcess = gccBuilder.start();
            captureProcessOutput(gccProcess);
            int gccExit = gccProcess.waitFor();
            if (gccExit != 0) {
                throw new IOException("gcc compilation failed for " + fileName);
            }
            // Run the compiled program.
            ProcessBuilder runBuilder = new ProcessBuilder("./" + outputName);
            runBuilder.directory(workingDir.toFile());
            System.out.println("[INFO] Running C: " + String.join(" ", runBuilder.command()));
            Process runProcess = runBuilder.start();
            captureProcessOutput(runProcess);
            int runExit = runProcess.waitFor();
            if (runExit != 0) {
                throw new IOException("C execution failed for " + outputName);
            }
        }
    }

    /**
     * Runs a Python program. Uses the virtualenv's Python interpreter for serial runs or mpirun for parallel runs.
     */
    private void runPython(Path workingDir, String fileName, int numberOfProcesses)
            throws IOException, InterruptedException {
        String pythonExec = Paths.get(venvBinFolder, "python").toString();
        List<String> command = new ArrayList<>();
        if (numberOfProcesses > 1) {
            command.add("mpirun");
            command.add("-np");
            command.add(String.valueOf(numberOfProcesses));
        }
        command.add(pythonExec);
        command.add(fileName);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workingDir.toFile());
        System.out.println("[INFO] Running Python: " + String.join(" ", pb.command()));
        Process process = pb.start();
        captureProcessOutput(process);
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Python execution failed for " + fileName);
        }
    }

    /**
     * Helper method to capture and print both stdout and stderr from a process.
     */
    private void captureProcessOutput(Process process) throws IOException {
        try (BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = stdOut.readLine()) != null) {
                System.out.println(line);
            }
            while ((line = stdErr.readLine()) != null) {
                System.err.println(line);
            }
        }
    }

    /**
     * Recursively copies the contents of the source folder to the destination folder.
     */
    private void copyFolder(Path source, Path destination) throws IOException {
        Files.walk(source).forEach(sourcePath -> {
            try {
                Path targetPath = destination.resolve(source.relativize(sourcePath));
                if (Files.isDirectory(sourcePath)) {
                    if (!Files.exists(targetPath)) {
                        Files.createDirectories(targetPath);
                    }
                } else {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Recursively deletes a directory.
     */
    private void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) return;
        Files.walk(path)
             .sorted((a, b) -> b.compareTo(a))
             .forEach(p -> {
                 try {
                     Files.delete(p);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             });
    }

    /**
     * Returns the file extension from a file name.
     */
    private String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return (index > 0 && index < fileName.length() - 1) ? fileName.substring(index + 1) : "";
    }

    // Optional main method for testing purposes.
    public static void main(String[] args) {
        // Adjust these paths for your environment.
        String venvBin = "~/Judahswork/ComFaaS_WIP/.serverVenv/bin";
        String progs = "~/Judahswork/ComFaaS_WIP/tests/SamplePrograms";

        TheProgramRunner runner = new TheProgramRunner(venvBin, progs);
        try {
            // Example: run a serial Java program.
            runner.run("WaitFor3Seconds.java", "~", "~");

            // Example: run a parallel MPI C program.
            runner.run("mpich_pi_reduce.c", "~", "~", 4);

            // Example: run a parallel MPI Python script.
            runner.run("mpiPython_test.py", "~", "~", 8);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
