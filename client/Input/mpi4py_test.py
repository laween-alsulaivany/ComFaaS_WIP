import random
# import mpiPython
# import mpiPython.mpiPython
# MPI = mpiPython.mpiPython.betaMPIpy(True)
from mpi4py import MPI
comm = MPI.COMM_WORLD

DARTS = 2<<19 # increase later.
ROUNDS = 100
MASTER = 0

def dboard(darts: int) -> float:
    # Constant similar to C code; equivalent to 2^31
    score = 0

    # Throw darts at the board
    for n in range(darts):
        # Generate random numbers for x and y coordinates
        r = random.random()  # Random float in [0.0, 1.0)
        x_coord = (2.0 * r) - 1.0  # Scale to [-1, 1]

        r = random.random()  # Random float in [0.0, 1.0)
        y_coord = (2.0 * r) - 1.0  # Scale to [-1, 1]

        # If dart lands in the circle, increment score
        if (x_coord ** 2 + y_coord ** 2) <= 1.0:
            score += 1

    # Estimate of pi based on ratio of darts inside the circle
    pi_estimate = 4.0 * score / darts
    return pi_estimate

# Run the simulation with a given number of darts
# darts = 10000
# pi_estimate = estimate_pi(darts)
# print(f"Estimated value of Pi after {darts} darts: {pi_estimate}")

def main():
    homepi = 0.0
    pisum = 0.0
    piSumBig = 0.0
    pi = 0.0
    avepi = 0.0
    taskid = 0
    numtasks = 0
    rc = 0
    # i = 0

    # numtasks = MPI.Size()
    numtasks = comm.Get_size()
    # taskid = MPI.Rank()
    taskid = comm.Get_rank()

    # printf ("MPI task %d has started...\n", taskid);
    print(f"MPI task {taskid} has started...")

    random.seed(taskid)

    for i in range(ROUNDS):
        homepi = dboard(DARTS//numtasks)

        # pisum = MPI.reduceSumDouble(homepi, 0)
        pisum = comm.reduce(homepi,MPI.SUM, 0)
        

        if (taskid == MASTER):
            pi = pisum/ numtasks
            avepi = ((avepi * i) + pi)/(i + 1)
            piSumBig += pi
    
    
    if (taskid == MASTER):
        avepi = piSumBig/100
        print(f"\tAfter {(DARTS/numtasks)* (i + 1)} throws, average value of pi = {avepi}")
        print("\nReal value of PI: 3.1415926535897 \n")

if __name__ == "__main__":
    main()

