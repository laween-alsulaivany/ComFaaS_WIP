package comfaas;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class ScriptTimer {

    /**
     * Runs the given script (or command) using bash with "time -p",
     * redirects the script’s stdout to "output.log", and captures the timing
     * information from stderr. The timing values are expected in the format:
     *
     * real <value>
     * user <value>
     * sys <value>
     *
     * @param script The command (or script file) to run.
     * @return A double array containing {user, real, sys} values.
     * @throws IOException          If an I/O error occurs.
     * @throws InterruptedException If the process is interrupted.
     */
    public static double[] runScript(String script) throws IOException, InterruptedException {
        // Build a command that runs: bash -c "time -p <script>"
        // We let ProcessBuilder redirect stdout to output.log.
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", "time -p " + script);
        // Redirect stdout to output.log.
        pb.redirectOutput(new File("output.log"));
        // We will capture stderr (where time prints its output).
        pb.redirectErrorStream(false);

        Process process = pb.start();

        // Read the error stream which should contain the timing info.
        BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        double user = 0.0, real = 0.0, sys = 0.0;
        while ((line = errReader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("user")) {
                user = parseTimeValue(line);
            } else if (line.startsWith("real")) {
                real = parseTimeValue(line);
            } else if (line.startsWith("sys")) {
                sys = parseTimeValue(line);
            }
        }
        errReader.close();

        // Wait for process to complete.
        process.waitFor();

        return new double[] { user, real, sys };
    }

    // Helper method to parse a line of the form "keyword value"
    private static double parseTimeValue(String line) {
        StringTokenizer st = new StringTokenizer(line);
        st.nextToken(); // skip the keyword (e.g., "real")
        if (st.hasMoreTokens()) {
            try {
                return Double.parseDouble(st.nextToken());
            } catch (NumberFormatException e) {
                // Log or rethrow as needed.
                return 0.0;
            }
        }
        return 0.0;
    }

    // For testing purposes.
    public static void main(String[] args) {
        try {
            // Example: run a simple command that prints "Hello" and sleeps 1 second.
            double[] timings = runScript("echo Hello && sleep 1");
            System.out.println("User: " + timings[0] + " sec");
            System.out.println("Real: " + timings[1] + " sec");
            System.out.println("Sys: " + timings[2] + " sec");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
