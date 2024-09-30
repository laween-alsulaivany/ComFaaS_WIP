package comfaas;

import java.io.*;

public class JavaProgramRunner {

    public boolean compileJavaProgram(String sourceFilePath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("javac", sourceFilePath);

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

    public void runJavaProgram(String className) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("java", className);

        Process process = processBuilder.start();

        BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String outputLine;
        while ((outputLine = outputReader.readLine()) != null) {
            System.out.println(outputLine);
        }

        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((outputLine = errorReader.readLine()) != null) {
            System.err.println(outputLine);
        }

        int exitCode = process.waitFor();
        System.out.println("Program exited with code: " + exitCode);
    }

    public String getClassName(String sourceFilePath) {
        // Remove the ".java" extension and return the base name
        return new File(sourceFilePath).getName().replace(".java", "");
    }

    public static void main(String[] args) {

        String javaSourceFilePath = "/path/to/your/program.java";

        JavaProgramRunner runner = new JavaProgramRunner();
        try {
            if (runner.compileJavaProgram(javaSourceFilePath)) {
                String className = runner.getClassName(javaSourceFilePath);
                runner.runJavaProgram(className);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
