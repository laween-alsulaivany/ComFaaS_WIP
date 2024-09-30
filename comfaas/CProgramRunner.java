package comfaas;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class CProgramRunner {

    public boolean compileCProgram(String sourceFilePath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("gcc", sourceFilePath, "-o", getExecutableName(sourceFilePath));

        Process process = processBuilder.start();

        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String errorLine;
        boolean hasErrors = false;
        while ((errorLine = errorReader.readLine()) != null) {
            hasErrors = true;
            System.err.println(errorLine);
        }

        int exitCode = process.waitFor();
        if (exitCode == 0 && !hasErrors) {
            // System.out.println("Compilation successful.");
            return true;
        } else {
            System.out.println("Compilation failed.");
            return false;
        }
    }

    public boolean compileMPICHProgram(String sourceFilePath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("mpicc", sourceFilePath, "-o", getExecutableName(sourceFilePath));

        Process process = processBuilder.start();

        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String errorLine;
        boolean hasErrors = false;
        while ((errorLine = errorReader.readLine()) != null) {
            hasErrors = true;
            System.err.println(errorLine);
        }

        int exitCode = process.waitFor();
        if (exitCode == 0 && !hasErrors) {
            // System.out.println("Compilation successful.");
            return true;
        } else {
            System.out.println("Compilation failed.");
            return false;
        }
    }

    public void runCProgram(String sourceFilePath) throws IOException, InterruptedException {
        String executablePath = "./" + getExecutableName(sourceFilePath);
        ProcessBuilder processBuilder = new ProcessBuilder(executablePath);
        
        executeProcess(processBuilder) ;
    }

    public void runMPICHProgram(String sourceFilePath, int numProcesses) throws IOException, InterruptedException {
        String executablePath = "./" + getExecutableName(sourceFilePath);
        // System.out.println("Running MPICH program: " + executablePath + " with " + numProcesses + " processes...");

        ProcessBuilder processBuilder = new ProcessBuilder("mpirun", "-np", String.valueOf(numProcesses), executablePath);
        executeProcess(processBuilder);
    }

    private void executeProcess(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = errorReader.readLine()) != null) {
            System.err.println(line);
        }

        int exitCode = process.waitFor();
        if (exitCode == 0) {
            // System.out.println("Process completed successfully.");
        } else {
            System.out.println("Process exited with code: " + exitCode);
        }
    }

    public void deleteExecutable(String sourceFilePath) throws IOException {
        String executablePath = "./" + getExecutableName(sourceFilePath);
        Path executableFile = Path.of(executablePath);
        if (Files.exists(executableFile)) {
            Files.delete(executableFile);
            // System.out.println("Executable deleted: " + executablePath);
        } else {
            System.out.println("Executable not found: " + executablePath);
        }
    }

    private String getExecutableName(String sourceFilePath) {
        // Remove the ".c" extension and return the base name
        return new File(sourceFilePath).getName().replace(".c", "");
    }

    public static void main(String[] args) {
        String cSourceFilePath = "/path/to/your/program.c";

        CProgramRunner runner = new CProgramRunner();
        try {
            if (runner.compileCProgram(cSourceFilePath)) {
                runner.runCProgram(cSourceFilePath);
                runner.deleteExecutable(cSourceFilePath);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
