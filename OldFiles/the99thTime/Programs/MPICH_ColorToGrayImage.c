#include "mpi.h"
#include <stdio.h>
#include <jpeglib.h>
#include <stdlib.h>
#include <string.h>
#include <setjmp.h>
#include <stdint.h>
#include <time.h>
#include <string.h>
#include <dirent.h>
#include <ctype.h>

// #include <math.h>

//to compile: mpicc.mpich -o MPICH_ColorToGrayImage MPICH_ColorToGrayImage.c -ljpeg
//to run: mpirun.mpich -np 3 ./MPICH_ColorToGrayImage

int operate(const char*, const char*) ;
void quickFlip(char*) ;

int main(int argc, char *argv[])
{
    time_t currentTime ;
    char dateTime[50] ;
    const char *outputDirectory = "Output" ;
    char filename[200] ;
    int count = 1;
    char tmp_count[10] ;
    const char* folder_path = "Input" ;
    char filename2[200] ;
    int numtasks, rank ;
    int task_cursor = 0;
    int signal ;

    MPI_Status status ;
    MPI_Init(&argc, &argv) ;
    MPI_Comm_size(MPI_COMM_WORLD, &numtasks) ;
    MPI_Comm_rank(MPI_COMM_WORLD, &rank) ;
    printf("My rank: %d.\n", rank) ;

    if (rank == 0) {
        DIR* directory = opendir(folder_path) ;
        if (directory == NULL) {
            perror("Unable to open the directory");
            return 1;
        }

        time(&currentTime) ;
        struct tm *localTime = localtime(&currentTime) ;
    
        struct dirent *entry ;
        signal = 1 ;
        // printf("got to while\n") ;
       
        while ((entry = readdir(directory)) != NULL) {
            if (entry->d_name[0] == '.') // Igonres . files
            continue;
            //printf("in while \n") ;
            strftime(dateTime, sizeof(dateTime), "%Y%m%d_", localTime) ;
            sprintf(tmp_count, "%d", count) ;
            count++ ;
            strcat(dateTime, tmp_count) ;
            strcat(dateTime, ".jpg") ;
            snprintf(filename, sizeof(filename), "%s/%s", outputDirectory, dateTime) ;

            strcpy(filename2, folder_path) ;
            strcat(filename2, "/") ;
            strcat(filename2, entry->d_name) ;

            //operate(filename2, filename) ;
            //send the file names to each person.
            //first send a continue signal
            signal = 1 ;
            // printf("task_cursor: %d\n", task_cursor) ;
            MPI_Send(&signal,1, MPI_INT,task_cursor+1,1, MPI_COMM_WORLD) ;
            MPI_Send(&filename2[0], 200, MPI_CHAR,task_cursor+1,2, MPI_COMM_WORLD) ;
            MPI_Send(&filename[0], 200, MPI_CHAR,task_cursor+1,3, MPI_COMM_WORLD) ;
            task_cursor = (task_cursor + 1) % (numtasks-1) ;
            // sleep(1) ;
        }
        printf("out of while.") ;
        closedir(directory) ;
        signal = 0 ;
        for(int i = 0; i < numtasks-1; i++ )
        {
            MPI_Send(&signal,1, MPI_INT,task_cursor+1,1, MPI_COMM_WORLD) ;
            task_cursor = (task_cursor + 1) %(numtasks-1) ;
        }
    }
    else {
        // printf("im worker\n") ;
        MPI_Recv(&signal,1, MPI_INT,0,1, MPI_COMM_WORLD, &status) ;
        // printf("got signal\n") ;
        while (signal)
        {   
       
            MPI_Recv(&filename2[0], 200, MPI_CHAR,0,2, MPI_COMM_WORLD, &status) ;
            MPI_Recv(&filename[0], 200, MPI_CHAR,0,3, MPI_COMM_WORLD, &status) ;
            // printf("got files\n") ;
            printf("got %s and %s\n", filename2, filename) ;

            operate( filename2, filename) ; 
            MPI_Recv(&signal,1, MPI_INT, 0,1, MPI_COMM_WORLD, &status) ;
        }

    }
    MPI_Barrier(MPI_COMM_WORLD) ;
    MPI_Finalize() ;

    return 0 ;
}

void quickFlip(char* str) {
  for (int i = 0; str[i] != '\0'; i++) {
    if (str[i] == ' ') {
      str[i] = '/' ;
      return ;
    }
  }
}

int operate(const char *fileIn, const char *fileOut)
{
    struct jpeg_decompress_struct deco_ob ;
    struct jpeg_compress_struct com_ob ;
    struct jpeg_error_mgr de_err2 ;
    struct jpeg_error_mgr com_err ;
    jmp_buf setjmp_buffer;
    FILE *infile ;
    FILE *outfile ;
    JSAMPARRAY buffer ;
    JSAMPROW row_pointer[1];
    int row_stride ;

    if ((infile = fopen(fileIn, "rb")) == NULL) {
        fprintf(stderr, "can't open %s\n", fileIn);
        return 0;
    } 
    deco_ob.err = jpeg_std_error(&de_err2);
    com_ob.err = jpeg_std_error(&com_err) ;
    if (setjmp(setjmp_buffer)) { //might now work :/
        jpeg_destroy_decompress(&deco_ob);
        fclose(infile);
        return 0;
    }
    jpeg_create_decompress(&deco_ob);
    jpeg_create_compress(&com_ob) ;
    jpeg_stdio_src(&deco_ob, infile);
    (void) jpeg_read_header(&deco_ob, TRUE);
    deco_ob.out_color_space = JCS_GRAYSCALE ;
    (void) jpeg_start_decompress(&deco_ob);
    row_stride = deco_ob.output_width * deco_ob.output_components;
    buffer = (*deco_ob.mem->alloc_sarray)
		((j_common_ptr) &deco_ob, JPOOL_IMAGE, row_stride, 1);

    if ((outfile = fopen(fileOut, "wb")) == NULL) {
        fprintf(stderr, "can't open %s\n", fileOut);
        exit(1);
    }
    jpeg_stdio_dest(&com_ob, outfile) ;
    com_ob.image_width = deco_ob.output_width ;
    com_ob.image_height = deco_ob.output_height ;
    com_ob.input_components = 1;
    com_ob.in_color_space = JCS_GRAYSCALE ;

    jpeg_set_defaults(&com_ob) ;
    jpeg_set_quality(&com_ob, 86, TRUE) ;

    jpeg_start_compress(&com_ob, TRUE) ;

    row_stride = deco_ob.image_width * 1;
    
    while (deco_ob.output_scanline < deco_ob.output_height) {
        (void) jpeg_read_scanlines(&deco_ob, buffer, 1);
        row_pointer[0] = *buffer ;
        (void) jpeg_write_scanlines(&com_ob, row_pointer, 1) ;
    }
  
  
    (void) jpeg_finish_decompress(&deco_ob);
    jpeg_destroy_decompress(&deco_ob);
    fclose(infile);

    jpeg_finish_compress(&com_ob);
    fclose(outfile);
    jpeg_destroy_compress(&com_ob);
    return 1;

}