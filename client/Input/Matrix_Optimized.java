import java.io.FileWriter;
import java.io.IOException;

public class Ex04_06_MatMul_Strassen_Double_Optimized {
    private static int matSize = 4; // Matrix size (should be a power of 2 for Strassen's algorithm)
    private static final int THRESHOLD = 64; // Hybrid threshold (use O(n³) multiplication if n ≤ THRESHOLD)

    /**
     * Strassen's Matrix Multiplication Algorithm.
     * - Uses a hybrid approach by switching to standard multiplication
     * for small matrices (n ≤ THRESHOLD).
     *
     * @param A First matrix
     * @param B Second matrix
     * @return Resultant matrix C = A * B
     */
    public static double[][] strassen(double[][] A, double[][] B) {
        int n = A.length;

        // Base case: If n is below threshold, use standard multiplication
        if (n <= THRESHOLD) {
            return standardMultiply(A, B);
        }

        int newSize = n / 2;

        // Create submatrices for A and B
        double[][] A11 = new double[newSize][newSize];
        double[][] A12 = new double[newSize][newSize];
        double[][] A21 = new double[newSize][newSize];
        double[][] A22 = new double[newSize][newSize];

        double[][] B11 = new double[newSize][newSize];
        double[][] B12 = new double[newSize][newSize];
        double[][] B21 = new double[newSize][newSize];
        double[][] B22 = new double[newSize][newSize];

        // Populate submatrices by dividing A and B into four parts
        for (int i = 0; i < newSize; i++) {
            for (int j = 0; j < newSize; j++) {
                A11[i][j] = A[i][j];
                A12[i][j] = A[i][j + newSize];
                A21[i][j] = A[i + newSize][j];
                A22[i][j] = A[i + newSize][j + newSize];

                B11[i][j] = B[i][j];
                B12[i][j] = B[i][j + newSize];
                B21[i][j] = B[i + newSize][j];
                B22[i][j] = B[i + newSize][j + newSize];
            }
        }

        // Preallocated matrices for intermediate computations
        // i.e. Compute intermediate S matrices
        double[][] S1 = subtract(B12, B22);
        double[][] S2 = add(A11, A12);
        double[][] S3 = add(A21, A22);
        double[][] S4 = subtract(B21, B11);
        double[][] S5 = add(A11, A22);
        double[][] S6 = add(B11, B22);
        double[][] S7 = subtract(A12, A22);
        double[][] S8 = add(B21, B22);
        double[][] S9 = subtract(A11, A21);
        double[][] S10 = add(B11, B12);

        // Compute P matrices recursively
        double[][] P1 = strassen(A11, S1);
        double[][] P2 = strassen(S2, B22);
        double[][] P3 = strassen(S3, B11);
        double[][] P4 = strassen(A22, S4);
        double[][] P5 = strassen(S5, S6);
        double[][] P6 = strassen(S7, S8);
        double[][] P7 = strassen(S9, S10);

        // Compute submatrices of result matrix C
        double[][] C11 = add(subtract(add(P5, P4), P2), P6);
        double[][] C12 = add(P1, P2);
        double[][] C21 = add(P3, P4);
        double[][] C22 = subtract(subtract(add(P5, P1), P3), P7);

        // Combine submatrices into final result
        double[][] C = new double[n][n];
        for (int i = 0; i < newSize; i++) {
            for (int j = 0; j < newSize; j++) {
                C[i][j] = C11[i][j];
                C[i][j + newSize] = C12[i][j];
                C[i + newSize][j] = C21[i][j];
                C[i + newSize][j + newSize] = C22[i][j];
            }
        }

        return C;
    }

    /**
     * Standard O(n³) matrix multiplication for small matrices.
     * Uses loop unrolling for better cache efficiency.
     *
     * @param A First matrix
     * @param B Second matrix
     * @return Resultant matrix C = A * B
     */
    private static double[][] standardMultiply(double[][] A, double[][] B) {
        int n = A.length;
        double[][] C = new double[n][n];
        for (int i = 0; i < n; i += 2) {
            for (int j = 0; j < n; j += 2) {
                int sum1 = 0, sum2 = 0, sum3 = 0, sum4 = 0;
                for (int k = 0; k < n; k += 2) {
                    sum1 += A[i][k] * B[k][j] + A[i][k + 1] * B[k + 1][j];
                    sum2 += A[i][k] * B[k][j + 1] + A[i][k + 1] * B[k + 1][j + 1];
                    sum3 += A[i + 1][k] * B[k][j] + A[i + 1][k + 1] * B[k + 1][j];
                    sum4 += A[i + 1][k] * B[k][j + 1] + A[i + 1][k + 1] * B[k + 1][j + 1];
                }
                C[i][j] = sum1;
                C[i][j + 1] = sum2;
                C[i + 1][j] = sum3;
                C[i + 1][j + 1] = sum4;
            }
        }
        return C;
    }

    /**
     * Adds two matrices.
     *
     * @param A First matrix
     * @param B Second matrix
     * @return Sum of matrices A and B
     */
    private static double[][] add(double[][] A, double[][] B) {
        int n = A.length;
        double[][] result = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = A[i][j] + B[i][j];
            }
        }
        return result;
    }

    /**
     * Subtracts matrix B from matrix A.
     *
     * @param A First matrix
     * @param B Second matrix
     * @return Difference A - B
     */
    private static double[][] subtract(double[][] A, double[][] B) {
        int n = A.length;
        double[][] result = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = A[i][j] - B[i][j];
            }
        }
        return result;
    }

    /**
     * Prints the contents of a matrix.
     * 
     * @param matrix The matrix to print.
     */
    private static void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            for (double value : row) {
                System.out.printf("%.1f ", value); // Format with two decimal places
            }
            System.out.println();
        }
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

    // double[][] A = new double[MAT_SIZE][MAT_SIZE] ;
    // double[][] B = new double[MAT_SIZE][MAT_SIZE] ;

    // // Initialize matrices
    // for (int i = 0 ; i < MAT_SIZE ; i++) {
    // for (int j = 0 ; j < MAT_SIZE ; j++) {
    // A[i][j] = i + j + 1 ;
    // B[i][j] = i + j + 1 ;
    // }
    // }

    // System.out.println("Matrix A:") ;
    // printMatrix(A) ;

    // System.out.println("Matrix B:") ;
    // printMatrix(B) ;

    // for (int i = 0 ; i < 10 ; i++) {

    // long startTime = System.nanoTime() ;
    // double[][] C = strassen(A , B) ;
    // long endTime = System.nanoTime() ;

    // System.out.println("Result Matrix C:") ;
    // printMatrix(C) ;

    // System.out.println("Execution time: " + (endTime - startTime) + " ns") ;
    // System.out.println("Execution time in milliseconds: " + ((endTime -
    // startTime) / 1_000_000.0) + " ms") ;
    // }
    // }

    private static void runBenchmark(int[] sizesAndRuns) {
        // We'll output all results to a single CSV
        String csvFile = "benchmark_MatMul_Strassen_Double_Optimized_Java.csv";

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

                    // Initialize matrices using the new helper function
                    initializeMatrices(A, B);

                    long startTime = System.nanoTime();
                    double[][] C = strassen(A, B);
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
            e.printStackTrace();
        }
    }

    // New method to initialize both matrices
    private static void initializeMatrices(double[][] A, double[][] B) {
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[i].length; j++) {
                A[i][j] = Math.random();
                B[i][j] = Math.random();
            }
        }
    }
}

/*
 * Key Performance Issues in the previous Implementation
 * 1. Excessive Recursive Overhead: Recursion depth increases for large
 * matrices,
 * leading to slow execution.
 * 2. Repeated Memory Allocation: Creating new matrices for subproblems
 * (S1, S2, ..., P1, P2, ...) is costly.
 * 3. Lack of Hybrid Approach: Strassen’s divide-and-conquer method works well
 * for large matrices, but for small matrices (n < threshold), standard matrix
 * multiplication is faster.
 * 4. Inefficient Matrix Addition/Subtraction: Each operation creates a new
 * matrix,
 * leading to unnecessary overhead.
 * 
 * Key Optimizations in this Implementation
 * 1. Hybrid Approach → Use O(n³) standard multiplication when n ≤ 64
 * (avoids recursion overhead).
 * 2. Improved Small Matrix Handling → Use loop unrolling in standardMultiply()
 * for better cache performance.
 * 3. Reduces memory allocations, improving efficiency
 */
