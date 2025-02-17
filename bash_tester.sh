#!/bin/bash

# Number of times to run the test
NUM_RUNS=10 

# Server and Port Pool
declare -A SERVERS
SERVERS["192.168.68.17"]="12353 cloud"
SERVERS["192.168.68.18"]="12345 edge"
SERVERS["192.168.68.20"]="12333 edge"

# Task Pool
TASKS=(
    "BigSummation.py"
    "Matrix.java"
    "Matrix.py"
    "Matrix_Numpy.py"
    "Matrix_Numpy2.py"
    "Matrix_Optimized.java"
    "PrimeCheck.c"
    "StressBoth.java"
    "StringManipulation.java"
    "WaitFor3Seconds.c"
    "WaitFor3Seconds.java"
    "WaitFor3Seconds.py"
    "mpi4py_test.py"
    "mpiPython_test.py"
    "mpich_matrix_mult.c"
    "mpich_pi_reduce.c"
)

# TASKS=(
#     "cpu_stress.c"
#     "memory_stress.py"
# )
# Function to determine language based on file extension
get_language() {
    case "$1" in
        *.py) echo "python" ;;
        *.java) echo "java" ;;
        *.c) echo "c" ;;
        *) echo "unknown" ;;
    esac
}

# Stress test loop
for ((i=1; i<=NUM_RUNS; i++)); do
    # Pick a random server
    SERVER=$(printf "%s\n" "${!SERVERS[@]}" | shuf -n 1)
    PORT_ROLE=(${SERVERS[$SERVER]}) # Get port and role
    PORT=${PORT_ROLE[0]}
    ROLE=${PORT_ROLE[1]}

    # Pick a random task
    TASK=${TASKS[$(( RANDOM % ${#TASKS[@]} ))]}
    LANG=$(get_language "$TASK")

    # Run the command
    echo "Running test $i on $SERVER:$PORT with role $ROLE - Task: $TASK (Lang: $LANG)"
    java -jar ComFaaS.jar client runtask -server "$SERVER" -p "$PORT" -l server -role "$ROLE" -np 1 -tn "$TASK" -lang "$LANG" &

    # Optional: Add a slight delay to prevent overwhelming the servers instantly
    sleep 1
done

# Wait for all background processes to complete
wait

echo "Stress test completed!"
