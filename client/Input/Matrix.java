import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Ex03_06_MatMul_Double {

    public static int matSize = 500; // Instance variable for matrix size

    private static void initializeMatrices(double[][] A, double[][] B) {

        for (int i = 0; i < matSize; i++) {
            for (int j = 0; j < matSize; j++) {
                A[i][j] = Math.random();
                B[i][j] = Math.random();
            }
        }
    }

    private static double[][] matrixMultiplication(double[][] A, double[][] B) {

        double[][] C = new double[matSize][matSize];

        for (int i = 0; i < matSize; i++) {
            for (int j = 0; j < matSize; j++) {
                C[i][j] = 0; // Ensuring proper initialization before summation
                for (int k = 0; k < matSize; k++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }

        return C;
    }

    public static void main(String[] args) {

        if (args.length % 2 != 0) {
            System.out.println("Please provide an even number of arguments for matrix size and file name pairs.");
            return;
        }

        int[] sizesAndRuns = new int[args.length];
        for (int i = 0; i < args.length; i++) {
            sizesAndRuns[i] = Integer.parseInt(args[i]);
        }

        runBenchmark(sizesAndRuns);

    }

    private static void runBenchmark(int[] sizesAndRuns) {
        // We'll output all results to a single CSV
        String csvFile = "benchmark_MatMul_Double_Java.csv";

        try (FileWriter writer = new FileWriter(csvFile)) {
            // Write CSV header
            writer.write("Matrix Size,Min Time (ms),Max Time (ms),Avg Time (ms)\n");

            for (int i = 0; i < sizesAndRuns.length; i += 2) {
                int size = sizesAndRuns[i];
                int runs = sizesAndRuns[i + 1];

                matSize = size;

                double[] times = new double[runs];

                for (int r = 0; r < runs; r++) {
                    double[][] A = new double[size][size];
                    double[][] B = new double[size][size];

                    initializeMatrices(A, B);

                    long startTime = System.nanoTime();
                    double[][] C = matrixMultiplication(A, B);
                    long endTime = System.nanoTime();

                    // convert ns to ms
                    double durationMs = (endTime - startTime) / 1_000_000.0;
                    times[r] = durationMs;
                }

                // Calculate min, max, average
                double minTime = times[0];
                double maxTime = times[0];
                double sum = 0.0;
                for (double t : times) {
                    if (t < minTime)
                        minTime = t;
                    if (t > maxTime)
                        maxTime = t;
                    sum += t;
                }
                double avgTime = sum / runs;

                // Print results to console
                System.out.printf("Matrix Size: %d, Runs: %d -> "
                        + "Min: %.3f ms, Max: %.3f ms, Avg: %.3f ms%n",
                        size, runs, minTime, maxTime, avgTime);

                // Write a CSV row
                writer.write(String.format("%d,%.3f,%.3f,%.3f\n", size, minTime, maxTime, avgTime));
            }
            System.out.println("Results saved in " + csvFile);

        } catch (IOException e) {
        }
    }
}
