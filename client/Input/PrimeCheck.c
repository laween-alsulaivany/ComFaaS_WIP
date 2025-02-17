#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>

int isPrime(long n) {
    if (n <= 1) return 0;
    if (n <= 3) return 1;
    if (n % 2 == 0 || n % 3 == 0) return 0;
    for (long i = 5; i*i <= n; i += 6) {
        if (n % i == 0 || n % (i + 2) == 0) return 0;
    }
    return 1;
}

int main(int argc, char *argv[]) {
    clock_t start = clock();

    long count = 0;
    for (long num = 1; num < 200000; num++) {
        if (isPrime(num)) count++;
    }

    clock_t end = clock();
    double elapsed = (double)(end - start) / CLOCKS_PER_SEC;

    printf("Found %ld primes below 200000\n", count);
    printf("Time elapsed: %.2f seconds\n", elapsed);

    return 0;
}
