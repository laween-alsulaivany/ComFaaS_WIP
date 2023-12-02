#include <time.h>
#include <stdio.h>
#include <math.h>
#include <string.h>



int main(int argc, char *argv[])
{
    time_t currentTime ;
    time(&currentTime) ;

    char dateTime[50] ;
    char filename[200] ;
    char rankTmp[10] ;
    const char *outputDirectory = "Output" ;
    double radians ;
    int NUM_ITERATIONS = 10000 * 7;

    time(&currentTime) ;
    struct tm *localTime = localtime(&currentTime) ;
    strftime(dateTime, sizeof(dateTime),"%Y%m%d_%H%M%S", localTime) ;

    
    strcat(dateTime, ".txt") ;
    snprintf(filename, sizeof(filename), "%s/%s", outputDirectory, dateTime) ;
    FILE *file = fopen(filename, "w") ;
    if (file == NULL) {
        perror("Judah: Error opening file");
        return 1;
    }

    for (int i = 0; i < NUM_ITERATIONS; i++)
    {
        for (int j = 0; j < 360; j++)
        {
            radians = j * M_PI / 180.0;
            fprintf(file, "%.14f\n", radians) ;
        }
        
    }
    fclose(file) ;
    return 0;
}