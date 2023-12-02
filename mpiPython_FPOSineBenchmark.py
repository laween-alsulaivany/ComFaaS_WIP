import math
import os
from datetime import datetime

import MPIpy_v3
MPI = MPIpy_v3.MPIpy()

# time mpirun.mpich -np 4 python3 mpiPython_FPOSineBenchmark.py

NUM_ITERATIONS = 10000 * 7

rank = MPI.rankf()
size = MPI.sizef()

currentDatetime = datetime.now()
dateTimeString = currentDatetime.strftime("%Y%m%d_%H%M%S")
outputDir = os.path.abspath("Output")
os.makedirs(outputDir, exist_ok=True)

points_per_node = NUM_ITERATIONS // size
remainder = NUM_ITERATIONS % size

rank_points = points_per_node + (1 if rank < remainder else 0)

start_index = rank * points_per_node + min(rank, remainder)
end_index = start_index + rank_points

filePath = os.path.join(outputDir, f"sineResult_{dateTimeString}.txt")
with open(filePath, "w") as writer:
    for i in range(start_index, end_index):
        for degrees in range(360):
            radians = math.radians(degrees)
            writer.write(f"{math.sin(radians)}\n")
