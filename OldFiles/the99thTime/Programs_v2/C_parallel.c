#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <dirent.h>

void upCount(int*, struct dirent***) ;

int main(int argc, char *argv[])
{
    time_t currentTime ;
    char dateTime[50] ;
    const char *outputDirectory = "Output" ;
    char filename[200] ;
    //int count = 1;
    char tmp_count[10] ;
    const char* folder_path = "Input" ;
    char filename2[200] ;
    

    DIR* directory = opendir(folder_path) ;
    if (directory == NULL)
    {
        perror("Unable to open the directory") ;
        return 1 ;
    }

    time(&currentTime) ;
    struct tm *localTime = localtime(&currentTime) ;

    struct dirent *entry ;
    struct dirent **entries = (struct dirent**) malloc(sizeof(struct dirent*) * 64) ;
    int count = 0 ; 

    while ((entries[count] = readdir(directory)) != NULL)
    {   
        
        if (entries[count]->d_name[0] == '.')
                
                continue;
        else
            upCount(&count, &entries) ;
    }
    
    
        
    free(entries) ;
    return 0 ;
}

void upCount(int* count, struct dirent*** entries) //I need it because I want to affect the original 2d array.
{
    static int size = 64 ;
    struct dirent **tmp = *entries ;
    *count = *count + 1 ;
    if (*count >= size)
    {
        tmp = (struct dirent**) realloc(tmp, sizeof(struct dirent*) * 64 * 2) ;
        size = size * 2 ;
    } 
}