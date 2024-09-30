package comfaas;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PythonRunner {

    public static void createVirtualEnv(String venvPath) throws IOException, InterruptedException {
        File venvDir = new File(venvPath);
        if (!venvDir.exists()) {
            // System.out.println("Creating virtual environment...");
            ProcessBuilder processBuilder = new ProcessBuilder("python3", "-m", "venv", venvPath);
            executeProcess(processBuilder);
        } else {
            System.out.println("Virtual environment already exists.");
        }
    }

    public static void installPackages(String venvPath, String[] packages) throws IOException, InterruptedException {
        System.out.println("Installing packages in the virtual environment...");
        String pipExecutable = venvPath + "/bin/pip";
        ProcessBuilder processBuilder = new ProcessBuilder(pipExecutable, "install");
        
        // Add packages to the command
        for (String pkg : packages) {
            processBuilder.command().add(pkg);
        }

        executeProcess(processBuilder);
    }

    public static void runPythonScriptInVenv(String venvPath, String scriptPath) throws IOException, InterruptedException {
        String pythonExecutable = venvPath + "/bin/python";
        System.out.println("Running Python script: " + scriptPath + " in the virtual environment...");
        ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutable, scriptPath);
        executeProcess(processBuilder);
    }

    public static void runPythonScriptWithMpi(String venvPath, String scriptPath, int numProcesses) throws IOException, InterruptedException {
        String pythonExecutable = venvPath + "/bin/python";
        System.out.println("Running Python script: " + scriptPath + " with mpirun (" + numProcesses + " processes)...");

        // Using mpirun with the virtual environment's Python interpreter
        ProcessBuilder processBuilder = new ProcessBuilder("mpirun", "-np", String.valueOf(numProcesses), pythonExecutable, scriptPath);
        executeProcess(processBuilder);
    }

    private static void executeProcess(ProcessBuilder processBuilder) throws IOException, InterruptedException {
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

    public static void runPythonScript(String scriptPath) throws IOException, InterruptedException {
    
        ProcessBuilder processBuilder = new ProcessBuilder("python3", scriptPath);
        
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
        System.out.println("Python script exited with code: " + exitCode);
    }

    public static void main(String[] args) {

        // PythonRunner runner = new PythonRunner();

        String scriptPath = "/path/to/your/script.py";
        
        try {
            PythonRunner.runPythonScript(scriptPath);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

