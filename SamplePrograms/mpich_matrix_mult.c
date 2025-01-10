#include <stdio.h>
#include <stdlib.h>
#include <mpi.h>

#define N 1000 // matrix size NxN

int main(int argc, char *argv[]) {
    MPI_Init(&argc, &argv);

    int rank, size;
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);

    // For demonstration: each rank just sleeps or does partial computation
    // A full matrix multiplication example is more verbose, but you get the idea.
    double start = MPI_Wtime();

    // Suppose rank 0 does initialization, etc.
    // Then each rank multiplies a slice of the matrix.

    // For simplicity, just do a busy loop to simulate some work:
    long sum = 0;
    for (long i = 0; i < 100000000; i++) {
        sum += i;
    }

    double end = MPI_Wtime();
    if (rank == 0) {
        printf("MPI Matrix Mult Emulation done. Total time: %.2f sec\n", end - start);
    }

    MPI_Finalize();
    return 0;
}
