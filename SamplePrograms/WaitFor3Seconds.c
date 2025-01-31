#include <stdio.h>
#include <unistd.h>  // For the sleep function

int main() {
    // Wait for 3 seconds
    printf("Waiting for 3 seconds...\n");
    sleep(3);  // Sleep for 3 seconds
    
    // Print the message
    printf("All good to go!\n");
    
    return 0;
}
