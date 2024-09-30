#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <math.h>

#define NUM_POINTS 360000000

int simulateMonteCarlo(int numPoints);
int isInsideCircle(double x, double y);
double calculatePiApproximation(int pointsInsideCircle, int numPoints);

int main() {
    srand(time(NULL));

    struct timespec start, end;
    clock_gettime(CLOCK_MONOTONIC_RAW, &start);

    int pointsInsideCircle = simulateMonteCarlo(NUM_POINTS);

    double piApproximation = calculatePiApproximation(pointsInsideCircle, NUM_POINTS);

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

    FILE *file = fopen("Output/montecarloResult.txt", "w");
    if (file == NULL) {
        perror("Error opening file");
        return 1;
    }

    fprintf(file, "%lf\n", piApproximation);

    fclose(file);

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
