#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <time.h>
#include <string.h>
#include <math.h>

// double random_double();
//to compile: gcc C_sqrtBenchmark.c -lm
//to run: time ./a.out

const int META = 4096 ;
const int NUM_ITERATIONS = (3600000 * 7) ;
const int MIN_VALUE = 20000 ;
const int MAX_VALUE = 40000 ;

int main(int argc, char *argv[]) 
{
time_t currentTime ;
char dateTime[50] ;
const char *outputDirectory = "Output" ;
char filename[200] ;
int randomNumber ;
// char buff[META] ;
int MIN_MAX = (MAX_VALUE - MIN_VALUE + 1) ;

// memset(buff, '\0', sizeof(buff)) ;
//This sets up the dateTime name for the file.
time(&currentTime) ;
srand(time(NULL)) ;
struct tm *localTime = localtime(&currentTime) ;
strftime(dateTime, sizeof(dateTime), "%Y%m%d_%H%M%S", localTime) ;
strcat(dateTime, ".txt") ;
snprintf(filename, sizeof(filename), "%s/%s", outputDirectory, dateTime) ;
//Open the file
FILE *file = fopen(filename, "w") ;
if (file == NULL) {
	perror("Judah: Error opening file");
	return 1;
}
// setvbuf(file, buff, _IOFBF, META) ;
//fprintf(file, "Current local date and time: %s\n", dateTime) ;
for (int i = 0; i < NUM_ITERATIONS; i++){
	randomNumber = rand() % MIN_MAX + MIN_VALUE;
	fprintf(file, "%.14f\n", sqrt((double)randomNumber)) ;
}
fclose(file) ;
return 0;
}

// double random_double() {
//     // Generate a random 32-bit integer
//     uint32_t random_int = (uint32_t)rand() | ((uint32_t)rand() << 15) | ((uint32_t)rand() << 30);

//     // Map the 32-bit integer to a double in [0, 1]
//     return (double)random_int / ((uint32_t)UINT32_MAX);
// }