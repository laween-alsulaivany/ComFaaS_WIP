# RunTasks_Local.sh: Run edge tasks locally

# Source environment variables so that ROOT_DIR is set
source ../backend/configs/env.sh
SERVER="127.0.0.1"
PORT="12353"

# Define directories (all absolute based on ROOT_DIR)
CLIENT_INPUT="$ROOT_DIR/client/Input"
CLIENT_OUTPUT="$ROOT_DIR/client/Output"
CLIENT_PROGRAMS="$ROOT_DIR/client/Programs"
SERVER_INPUT="$ROOT_DIR/server/Input"
SERVER_OUTPUT="$ROOT_DIR/server/Output"
SERVER_PROGRAMS="$ROOT_DIR/server/Programs"
# Test folder containing files to upload (ensure this folder exists)
TESTING_FOLDER="$ROOT_DIR/Tests/TestFolder"
# Local downloads folder (where downloaded files/folders will go)
LOCAL_DOWNLOADS="$ROOT_DIR/LocalDownloads"
# Folder containing test files for single file operations
TEST_FOLDER="$ROOT_DIR/Tests"

echo "Running edge tasks locally..."
###
### After downloading the folder, it should run some tasks that are in the folder
##############################################
echo "============================================"
echo "===== Upload BigSummation.py ====="
echo "============================================"
echo "Uploading BigSummation.py from $CLIENT_INPUT to server ($SERVER_PROGRAMS)..."
java -jar ../ComFaaS.jar client upload \
    -server $SERVER -p $PORT -l server \
    -local "$CLIENT_INPUT/BigSummation.py" \
    -remote "$SERVER_PROGRAMS"
echo "============================================"
##############################################
echo "============================================"
echo "Press any key to continue..."
read -n 1 -s
echo "============================================"
##############################################
echo "============================================"
echo "=====  Run BigSummation.py ====="
echo "============================================"
echo "Running BigSummation.py on the server and outputting results to $SERVER_OUTPUT..."
java -jar ../ComFaaS.jar client runtask \
    -server $SERVER -p $PORT -l server \
    -np 1 \
    -tn BigSummation.py \
    -lang python
echo "============================================"
##############################################
echo "============================================"
echo "Press any key to continue..."
read -n 1 -s
echo "============================================"
##############################################
echo "============================================"
echo "=====  Download BigSummation.py Results ====="
echo "============================================"
echo "Downloading BigSummation.py results from $SERVER_OUTPUT to $CLIENT_OUTPUT..."
java -jar ../ComFaaS.jar client download \
    -server $SERVER -p $PORT -l server \
    -remote "$SERVER_OUTPUT" \
    -file "BigSummation.txt" \
    -local "$CLIENT_OUTPUT"
echo "============================================"

# java -jar ../ComFaaS.jar client runtask -server 127.0.0.1 -p 12353 -l server -np 1 -tn WaitFor3Seconds.c -lang C


# echo "Task 1"
# java -jar ../ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12353 -t cloud -np 1 -tn WaitFor3Seconds.c -lang C
# echo "----------------------"
# echo "Task 2"
# java -jar ../ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12354 -t edge -np 1 -tn WaitFor3Seconds.c -lang C
# echo "----------------------"
# echo "Task 3"
# java -jar ../ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12354 -t edge -np 4 -tn mpich_pi_reduce.c -lang C
# echo "----------------------"
# echo "Task 4"
# java -jar ../ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12353 -t cloud -np 4 -tn mpich_pi_reduce.c -lang C
# echo "----------------------"
# echo "Task 5"
# java -jar ../ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12353 -t cloud -np 1 -tn WaitFor3Seconds.py -lang python
# echo "----------------------"
# echo "Task 6"
# java -jar ../ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12354 -t edge -np 1 -tn WaitFor3Seconds.py -lang python
# echo "----------------------"
# echo "Task 7"
# java -jar ../ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12354 -t edge -np 4 -tn mpiPython_test.py -lang python
# echo "----------------------"
# echo "Task 8"
# java -jar ../ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12353 -t cloud -np 4 -tn mpiPython_test.py -lang python
# echo "----------------------"
# echo "Task 9"
# java -jar ../ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12354 -t edge -np 4 -tn mpi4py_test.py -lang python
# echo "----------------------"
# echo "Task 10"
# java -jar ../ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12353 -t cloud -np 4 -tn mpi4py_test.py -lang python
# echo "----------------------"
# echo "Task 11"
# java -jar ../ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12353 -t cloud -np 1 -tn WaitFor3Seconds.java -lang java
# echo "----------------------"
# echo "Task 12"
# java -jar ../ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12354 -t edge -np 1 -tn WaitFor3Seconds.java -lang java
# echo "----------------------"
# echo "Task 13"
# java -jar ../ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12353 -t cloud -np 1 -tn BigSummation.py -lang python
# echo "----------------------"
# echo "Task 14"
# java -jar ../ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12354 -t edge -np 1 -tn BigSummation.py -lang python
# echo "----------------------"
# echo "Task 15"
# java -jar ../ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12353 -t cloud -np 1 -tn StringManipulation.java -lang java
# echo "----------------------"
# echo "Task 16"
# java -jar ../ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12354 -t edge -np 1 -tn StringManipulation.java -lang java
# echo "----------------------"
# echo "Task 17"
# java -jar ../ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12353 -t cloud -np 1 -tn PrimeCheck.c -lang C
# echo "----------------------"
# echo "Task 18"
# java -jar ../ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12354 -t edge -np 1 -tn PrimeCheck.c -lang C

echo "----------------------"
echo "----------------------"
echo "Tasks completed."
echo "----------------------"
echo "----------------------"

