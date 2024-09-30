#include <stdio.h>
#include <stdlib.h>
#include <dirent.h>
#include <ctype.h>

void quickFlip(char*) ;

int main() {
    // Specify the path to the folder you want to list files from
    const char* folder_path = "Input";

    // Open the folder
    DIR* directory = opendir(folder_path);
    if (directory == NULL) {
        perror("Unable to open the directory");
        return 1;
    }

    // Read and print file names
    struct dirent *entry;
    while ((entry = readdir(directory)) != NULL) {
        // Skip "." and ".." entries
        if (entry->d_name[0] == '.')
            continue;
        quickFlip(entry->d_name) ;
        entry->d_name[0] = (char)toupper(entry->d_name[0]);
        printf("File: %s\n", entry->d_name);
    }


    // Close the directory
    closedir(directory);

    return 0;
}

void quickFlip(char* str) {
  for (int i = 0; str[i] != '\0'; i++) {
    if (str[i] == ' ') {
      str[i] = '/' ;
      return ;
    }
  }
}