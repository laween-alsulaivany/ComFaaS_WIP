 #include "mpi.h"
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <time.h>
#include <string.h>
#include <math.h>

//to compile: mpicc.mpich MPICH_sqrtBenchmark.c -lm
//to run: time mpirun.mpich -np 4 ./a.out

int NUM_ITERATIONS ;
const int MIN_VALUE = 20000 ;
const int MAX_VALUE = 40000 ;

int main(int argc, char *argv[])
{
time_t currentTime ;
time_t timetravel_v_rank ;
int numtasks, rank, randomNumber ;
char dateTime[50] ;
char filename[200] ;
char rankTmp[10] ;
const char *outputDirectory = "Output" ;
const int MIN_MAX = (MAX_VALUE - MIN_VALUE + 1) ;
MPI_Status status ;

MPI_Init(&argc, &argv) ;
MPI_Comm_size(MPI_COMM_WORLD, &numtasks) ;
MPI_Comm_rank(MPI_COMM_WORLD, &rank) ;

int step1 = (3600000 * 7) / numtasks ;
int step2 = (3600000 * 7) % numtasks ;

NUM_ITERATIONS = step1 + (rank<step2) ;
timetravel_v_rank = (time_t)rank ;

printf("My rank: %d.\n", rank) ;
MPI_Barrier(MPI_COMM_WORLD) ;
time(&currentTime) ;
srand(time(NULL)+timetravel_v_rank);
struct tm *localTime = localtime(&currentTime) ;
strftime(dateTime, sizeof(dateTime), "%Y%m%d_%H%M%S", localTime) ;
sprintf(rankTmp, "_%d", rank) ;
strcat(dateTime, rankTmp) ;
strcat(dateTime, ".txt") ;
snprintf(filename, sizeof(filename), "%s/%s", outputDirectory, dateTime) ;
FILE *file = fopen(filename, "w") ;
if (file == NULL)
{
	perror("Judah: Error opening file") ;
	MPI_Finalize() ;
	return -1 ;
}
for (int i = 0; i < NUM_ITERATIONS; i++)
{
	randomNumber = rand() % MIN_MAX + MIN_VALUE ;
	fprintf(file, "%.14f\n", sqrt((double)randomNumber)) ;
}
fclose(file) ;
MPI_Barrier(MPI_COMM_WORLD) ;
MPI_Finalize() ;
return 0 ;
}

