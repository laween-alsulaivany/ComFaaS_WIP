import random
import os
from datetime import datetime

import MPIpy_v3
MPI = MPIpy_v3.MPIpy()

# time mpirun.mpich -np 4 python3 mpiPython_MonteCarlo.py

NUM_POINTS = 360000000

def simulateMonteCarlo(num_points):
    pointsInsideCircle = 0

    for _ in range(num_points):
        x = random.random()
        y = random.random()

        if is_inside_circle(x, y):
            pointsInsideCircle += 1

    return pointsInsideCircle

def is_inside_circle(x, y):
    distance = (x ** 2 + y ** 2) ** 0.5
    return distance <= 1.0

def calculate_pi_approximation(pointsInsideCircle, num_points):
    return 4.0 * pointsInsideCircle / num_points

def main():
    rank = MPI.rankf()
    size = MPI.sizef()

    current_datetime = datetime.now()
    date_time_string = current_datetime.strftime("%Y%m%d_%H%M%S")
    outputDir = os.path.abspath("Output") 
    os.makedirs(outputDir, exist_ok=True)

    #calculate number of points per node
    points_per_node = NUM_POINTS // size
    remainder = NUM_POINTS % size

    #spread out remaining points
    rank_points = points_per_node + (1 if rank < remainder else 0)

    points_inside_circle = simulateMonteCarlo(rank_points)

    total_points_in_circle = MPI.reduceSumInt(points_inside_circle, MPI.MASTER)

    if rank == MPI.MASTER:
        pi_approximation = calculate_pi_approximation(total_points_in_circle, NUM_POINTS)

        filePath = os.path.join(outputDir, f"montecarloResult_{date_time_string}_{MPI.rank}.txt")
        with open(filePath, 'w') as writer:
            writer.write(str(pi_approximation))

if __name__ == '__main__':
    main()