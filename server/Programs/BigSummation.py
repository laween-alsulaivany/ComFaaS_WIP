import sys
import time

def big_summation(n):
    s = 0
    for i in range(n):
        s += i
    return s

if __name__ == "__main__":
    start_time = time.time()
    # Summation up to 50 million for a decent CPU load
    result = big_summation(50_000_000)
    elapsed = time.time() - start_time
    print(f"Result is {result}, took {elapsed:.2f} seconds.")
