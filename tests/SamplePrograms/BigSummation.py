import sys
import time
import os


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

    OUTPUT_PATH = os.path.join(os.path.dirname(__file__), "..", "Output")

    # Replace console output with file output
    with open(os.path.join(OUTPUT_PATH, "BigSummation.txt"), "w") as f:
        f.write(f"Result is {result}, took {elapsed:.2f} seconds.")
