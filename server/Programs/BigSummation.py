import os
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
    # Corrected relative path: go two directories up to reach the project root then into Output
    script_dir = os.path.dirname(os.path.abspath(__file__))
    output_path = os.path.join(script_dir, "..", "Output", "BigSummation.txt")

    with open(output_path, "w") as f:
        # with open("../../Output/BigSummation.txt", "w") as f:
        f.write(f"Result is {result}, took {elapsed:.2f} seconds.")
