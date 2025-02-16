package comfaas.theAlgoTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class ScriptTimer {

    /**
     * Runs the given script (or command) using /usr/bin/time -p,
     * redirects the script’s stdout to output.log, and captures the timing
     * information from stderr. The timing output is expected in the format:
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
        // Use the external time command.
        // ProcessBuilder venv = new ProcessBuilder(venvPath + "/bin/python" + script);
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", "/usr/bin/time -p " + script);
        // Redirect stdout to output.log.
        pb.redirectOutput(new File("output.log"));
        // We want to capture stderr (where /usr/bin/time writes its output).
        pb.redirectErrorStream(false);

        Process process = pb.start();

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
            } else {
                // Print any unexpected lines (for debugging)
                // System.err.println("Time output: " + line);
            }
        }
        errReader.close();

        process.waitFor();

        return new double[] { user, real, sys };
    }

    private static double parseTimeValue(String line) {
        StringTokenizer st = new StringTokenizer(line);
        st.nextToken(); // skip the keyword ("user", "real", or "sys")
        if (st.hasMoreTokens()) {
            try {
                return Double.parseDouble(st.nextToken());
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    // For testing purposes.
    public static void main(String[] args) {
        try {
            // Test with a command that takes measurable time.
            double[] timings = runScript("sleep 2");
            System.out.println("User: " + timings[0] + " sec");
            System.out.println("Real: " + timings[1] + " sec");
            System.out.println("Sys: " + timings[2] + " sec");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
