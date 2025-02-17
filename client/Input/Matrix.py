import random
import time
import sys
import csv


class MatMulDouble:
    mat_size = 512  # Fixed matrix size to 512

    @staticmethod
    def initialize_matrices(A, B):
        """Initialize matrices A and B with random floating-point numbers between 1.0 and 9.9"""
        for row_index in range(MatMulDouble.mat_size):
            for col_index in range(MatMulDouble.mat_size):
                A[row_index][col_index] = random.uniform(1.0, 9.9)
                B[row_index][col_index] = random.uniform(1.0, 9.9)

    @staticmethod
    def matrix_multiplication(A, B, C):
        """Perform floating-point matrix multiplication: C = A * B"""
        for row_index in range(MatMulDouble.mat_size):
            for col_index in range(MatMulDouble.mat_size):
                C[row_index][col_index] = 0.0  # Initialize to zero
                for k_index in range(MatMulDouble.mat_size):
                    C[row_index][col_index] += A[row_index][k_index] * \
                        B[k_index][col_index]

    @staticmethod
    def main():
        # Commented out CLI arguments
        # args = sys.argv[1:]
        # if not args:
        #     print("Error: No arguments provided.")
        #     sys.exit(1)
        # else:
        #     if len(args) % 2 != 0:
        #         print("Error: Provide pairs like: <size1> <runs1> <size2> <runs2> ...")
        #         sys.exit(1)

        # Fixed values: matrix size = 512, runs = 5
        sizes_and_runs = [(512, 5)]

        # Open CSV file to store results
        csv_filename = "benchmark_MatMul_Double_python.csv"
        with open(csv_filename, mode='w', newline='') as csvfile:
            csv_writer = csv.writer(csvfile)
            csv_writer.writerow(
                ["Matrix Size", "Min Time (ms)", "Max Time (ms)", "Avg Time (ms)"])

            for (size, runs) in sizes_and_runs:
                MatMulDouble.mat_size = size  # Set fixed matrix size

                times_ms = []
                for _ in range(runs):
                    A = [[0.0] * size for _ in range(size)]
                    B = [[0.0] * size for _ in range(size)]
                    C = [[0.0] * size for _ in range(size)]

                    MatMulDouble.initialize_matrices(A, B)

                    # Start timing
                    start_time = time.perf_counter_ns()
                    MatMulDouble.matrix_multiplication(A, B, C)
                    end_time = time.perf_counter_ns()

                    duration_ms = (end_time - start_time) / 1_000_000.0
                    times_ms.append(duration_ms)

                # Compute stats
                min_time = min(times_ms)
                max_time = max(times_ms)
                avg_time = sum(times_ms) / len(times_ms)

                print(f"Matrix Size = {size}, Runs = {runs} -> "
                      f"Min: {min_time:.3f} ms, Max: {max_time:.3f} ms, Avg: {avg_time:.3f} ms")

                # Write row to CSV
                csv_writer.writerow(
                    [size, f"{min_time:.3f}", f"{max_time:.3f}", f"{avg_time:.3f}"])

        print(f"Results saved in {csv_filename}")


if __name__ == "__main__":
    MatMulDouble.main()
