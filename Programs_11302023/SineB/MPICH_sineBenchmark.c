#include "mpi.h"
#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>

#define NUM_ITERATIONS 10000 * 7
#define DEGREES_IN_CIRCLE 360

int main(int argc, char *argv[]) {
    MPI_Init(&argc, &argv);

    int rank, size;
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);

    char dateTime[50];
    char filename[200];
    double radians;

    time_t currentTime;
    time(&currentTime);
    struct tm *localTime = localtime(&currentTime);
    strftime(dateTime, sizeof(dateTime), "%Y%m%d_%H%M%S", localTime);

    sprintf(dateTime + strlen(dateTime), "_%d", rank);

    snprintf(filename, sizeof(filename), "Output/%s.txt", dateTime);
    FILE *file = fopen(filename, "w");
    if (file == NULL) {
        perror("Error opening file");
        MPI_Abort(MPI_COMM_WORLD, 1);
    }

    int iterations_per_process = NUM_ITERATIONS / size;
    for (int i = 0; i < iterations_per_process; i++) {
        for (int j = 0; j < DEGREES_IN_CIRCLE; j++) {
            radians = j * M_PI / 180.0;
            fprintf(file, "%.14f\n", radians);
        }
    }

    fclose(file);

    MPI_Finalize();
    return 0;
}
