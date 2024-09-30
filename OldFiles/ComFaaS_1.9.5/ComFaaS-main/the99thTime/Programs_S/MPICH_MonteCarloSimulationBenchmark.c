#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <math.h>
#include <mpi.h>

// mpicc.mpich MPICH_MonteCarloSimulationBenchmark.c
// mpirun.mpich -np 4 ./a.out

#define NUM_POINTS 360000000

int simulateMonteCarlo(int numPoints);
int isInsideCircle(double x, double y);
double calculatePiApproximation(int pointsInsideCircle, int numPoints);

int main(int argc, char **argv) {
    MPI_Init(&argc, &argv);

    int rank, size;
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);

    srand(time(NULL) + rank); // Adjust the seed based on rank for different random sequences

    struct timespec start, end;
    MPI_Barrier(MPI_COMM_WORLD); // Synchronize all processes

    if (rank == 0) {
        clock_gettime(CLOCK_MONOTONIC_RAW, &start);
    }

    int pointsInsideCircle = simulateMonteCarlo(NUM_POINTS / size);

    int totalPointsInsideCircle;
    MPI_Reduce(&pointsInsideCircle, &totalPointsInsideCircle, 1, MPI_INT, MPI_SUM, 0, MPI_COMM_WORLD);

    if (rank == 0) {
        struct timespec temp;
        clock_gettime(CLOCK_MONOTONIC_RAW, &end);

        if ((end.tv_nsec - start.tv_nsec) < 0) {
            temp.tv_sec = end.tv_sec - start.tv_sec - 1;
            temp.tv_nsec = 1000000000 + end.tv_nsec - start.tv_nsec;
        } else {
            temp.tv_sec = end.tv_sec - start.tv_sec;
            temp.tv_nsec = end.tv_nsec - start.tv_nsec;
        }

        printf("Time: %ld.%09ld seconds\n", temp.tv_sec, temp.tv_nsec);

        double piApproximation = calculatePiApproximation(totalPointsInsideCircle, NUM_POINTS);

        FILE *file = fopen("Output/montecarloResult.txt", "w");
        if (file == NULL) {
            perror("Error opening file");
            return 1;
        }

        fprintf(file, "%lf\n", piApproximation);

        fclose(file);
    }

    MPI_Finalize();

    return 0;
}

int simulateMonteCarlo(int numPoints) {
    int pointsInsideCircle = 0;

    for (int i = 0; i < numPoints; i++) {
        double x = (double) rand() / RAND_MAX;
        double y = (double) rand() / RAND_MAX;

        if (isInsideCircle(x, y)) {
            pointsInsideCircle++;
        }
    }

    return pointsInsideCircle;
}

int isInsideCircle(double x, double y) {
    double distance = sqrt(x * x + y * y);
    return distance <= 1.0;
}

double calculatePiApproximation(int pointsInsideCircle, int numPoints) {
    return 4.0 * pointsInsideCircle / numPoints;
}
