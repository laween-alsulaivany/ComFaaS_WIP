import math
import os
import random
from datetime import datetime

import MPIpy_v3
MPI = MPIpy_v3.MPIpy()

# time mpirun.mpich -np 4 python3 mpiPython_sqrtBenchmark.py

NUM_ITERATIONS = 36000000

def main() :
    rank = MPI.rankf()
    size = MPI.sizef()

    currentDatetime = datetime.now()
    dateTimeString = currentDatetime.strftime("%Y%m%d_%H%M%S")
    outputDir = os.path.abspath("Output") 
    os.makedirs(outputDir, exist_ok=True)

    #calculate points per node
    points_per_node = NUM_ITERATIONS // size
    remainder = NUM_ITERATIONS % size

    rank_points = points_per_node + (1 if rank < remainder else 0)

    #distribute work
    start_index = rank * points_per_node + min(rank, remainder)
    end_index = start_index + rank_points

    filePath = os.path.join(outputDir, f"sqrtResult_{dateTimeString}_{MPI.rank}.txt")
    with open(filePath, 'w') as writer:
        for i in range(start_index, end_index):
            a = random.randrange(20000, 40001)
            writer.write(f"{math.sqrt(a)}\n")


if __name__ == '__main__':
    main()