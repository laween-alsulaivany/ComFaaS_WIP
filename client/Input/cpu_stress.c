// cpu_stress.c
#include <stdio.h>
#include <math.h>
#include <pthread.h>
#include <stdlib.h>

#define NUM_THREADS 4 // Adjust based on your CPU cores

void *cpu_stress(void *arg)
{
    volatile double x = 0.0001;
    while (1)
    {
        for (int i = 0; i < 1000000; i++)
        {
            // Perform continuous math operations
            x = sin(x) * cos(x) + sqrt(x);
        }
    }
    return NULL;
}

int main()
{
    pthread_t threads[NUM_THREADS];
    for (int i = 0; i < NUM_THREADS; i++)
    {
        if (pthread_create(&threads[i], NULL, cpu_stress, NULL))
        {
            perror("pthread_create failed");
            exit(1);
        }
    }
    // Wait indefinitely (or until manually terminated)
    for (int i = 0; i < NUM_THREADS; i++)
    {
        pthread_join(threads[i], NULL);
    }
    return 0;
}
