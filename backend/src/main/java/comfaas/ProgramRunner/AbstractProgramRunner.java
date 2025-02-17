package comfaas.ProgramRunner;


import java.io.IOException;

/**
 * Abstract base class that declares the functions for running programs.
 */
public abstract class AbstractProgramRunner {
    protected final String venvBinFolder;
    protected final String programsFolder;

    /**
     * Constructor.
     *
     * @param venvBinFolder  the folder where the Python virtualenv's bin directory is located.
     * @param programsFolder the folder where all the programs are stored.
     */
    public AbstractProgramRunner(String venvBinFolder, String programsFolder) {
        this.venvBinFolder = venvBinFolder;
        this.programsFolder = programsFolder;
    }

    /**
     * Runs a program in serial mode.
     *
     * @param fileName     the program file name (e.g. "WaitFor3Seconds.java", "myProg.c", "script.py").
     * @param inputFolder  the folder containing input data.
     * @param outputFolder the folder where output data should be stored.
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the execution is interrupted.
     */
    public abstract void run(String fileName, String inputFolder, String outputFolder)
            throws IOException, InterruptedException;

    /**
     * Runs a program in parallel mode (MPI).
     *
     * @param fileName          the program file name.
     * @param inputFolder       the folder containing input data.
     * @param outputFolder      the folder where output data should be stored.
     * @param numberOfProcesses the number of processes to launch.
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the execution is interrupted.
     */
    public abstract void run(String fileName, String inputFolder, String outputFolder, int numberOfProcesses)
            throws IOException, InterruptedException;
}
