#!/usr/bin/env bash
# FileOperation_Local.sh: Test file operations on local machine

echo "============================================"
echo "===== Local File Operations Test Started ====="
echo "============================================"

# Source environment variables so that ROOT_DIR is set
source ../backend/configs/env.sh

# Define server connection details
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

# java -jar ../ComFaaS.jar client upload -server 127.0.0.1 -p 12353 -local "$ROOT_DIR/Tests/TestFolder" -remote "$ROOT_DIR/client/Input"

##############################################
echo "============================================"
echo "===== Server: File Download Test ====="
echo "============================================"
echo "Downloading 5KB_file.dat from $TEST_FOLDER to $CLIENT_INPUT..."
java -jar ../ComFaaS.jar client download \
    -server $SERVER -p $PORT -l server \
    -remote "$TEST_FOLDER" \
    -file "5KB_file.dat" \
    -local "$CLIENT_INPUT"
echo "============================================"

##############################################
echo "============================================"
echo "Press any key to continue..."
read -n 1 -s
echo "============================================"
##############################################
echo "============================================"
echo "===== Server: File Upload Test ====="
echo "============================================"
echo "Uploading 5KB_file.dat from $CLIENT_INPUT to server ($SERVER_OUTPUT)..."
java -jar ../ComFaaS.jar client upload \
    -server $SERVER -p $PORT -l server \
    -local "$CLIENT_INPUT/5KB_file.dat" \
    -remote "$SERVER_OUTPUT"
echo "============================================"

##############################################
echo "============================================"
echo "Press any key to continue..."
read -n 1 -s
echo "============================================"
##############################################
echo "============================================"
echo "===== Server: File Download Test ====="
echo "============================================"
echo "Downloading 5KB_file.dat from $SERVER_OUTPUT to $LOCAL_DOWNLOADS..."
java -jar ../ComFaaS.jar client download \
    -server $SERVER -p $PORT -l server \
    -remote "$SERVER_OUTPUT" \
    -file "5KB_file.dat" \
    -local "$LOCAL_DOWNLOADS"
echo "============================================"

##############################################
echo "============================================"
echo "Press any key to continue..."
read -n 1 -s
echo "============================================"
##############################################
echo "============================================"
echo "===== Server: File Download Test ====="
echo "============================================"
echo "Downloading file_to_delete.dat from $TEST_FOLDER to $CLIENT_INPUT..."
java -jar ../ComFaaS.jar client download \
    -server $SERVER -p $PORT -l server \
    -remote "$TEST_FOLDER" \
    -file "file_to_delete.dat" \
    -local "$CLIENT_INPUT"
echo "============================================"

##############################################
echo "============================================"
echo "Press any key to continue..."
read -n 1 -s
echo "============================================"
##############################################
echo "============================================"
echo "===== Server: File Upload Test ====="
echo "============================================"
echo "Uploading file_to_delete.dat from $CLIENT_INPUT to server ($SERVER_OUTPUT)..."
java -jar ../ComFaaS.jar client upload \
    -server $SERVER -p $PORT -l server \
    -local "$CLIENT_INPUT/file_to_delete.dat" \
    -remote "$SERVER_OUTPUT"
echo "============================================"

##############################################
echo "============================================"
echo "Press any key to continue..."
read -n 1 -s
echo "============================================"
##############################################
echo "============================================"
echo "===== Server: File List Test ====="
echo "============================================"
echo "Listing files in $SERVER_OUTPUT on server..."
java -jar ../ComFaaS.jar client listfiles \
    -server $SERVER -p $PORT \
    -dir "$SERVER_OUTPUT"
echo "============================================"

##############################################
echo "============================================"
echo "Press any key to continue..."
read -n 1 -s
echo "============================================"
##############################################
echo "============================================"
echo "===== Server: File Delete Test ====="
echo "============================================"
echo "Deleting file_to_delete.dat from server ($SERVER_OUTPUT)..."
java -jar ../ComFaaS.jar client deletefile \
    -server $SERVER -p $PORT -l server \
    -folder "$SERVER_OUTPUT" \
    -file "file_to_delete.dat"
echo "============================================"

##############################################
echo "============================================"
echo "Press any key to continue..."
read -n 1 -s
echo "============================================"
##############################################
echo "============================================"
echo "===== Server: File List Test ====="
echo "============================================"
echo "Listing files in $SERVER_OUTPUT on server..."
java -jar ../ComFaaS.jar client listfiles \
    -server $SERVER -p $PORT \
    -dir "$SERVER_OUTPUT"
echo "============================================"

##############################################
echo "============================================"
echo "Press any key to continue..."
read -n 1 -s
echo "============================================"
##############################################
echo "============================================"
echo "===== Server: Folder Upload Test ====="
echo "============================================"
# IMPORTANT: The current uploadFolder command uploads only the files from $CLIENT_TEST_FOLDER
# directly into the remote directory. If you want to preserve the folder structure,
# consider uploading to "$CLIENT_INPUT/TestFolder" instead.
echo "Uploading folder ($TESTING_FOLDER) to server ($SERVER_OUTPUT)..."
java -jar ../ComFaaS.jar client uploadfolder \
    -server $SERVER -p $PORT -l server \
    -local "$TESTING_FOLDER" \
    -remote "$SERVER_OUTPUT"
echo "============================================"

##############################################
echo "============================================"
echo "Press any key to continue..."
read -n 1 -s
echo "============================================"
##############################################
echo "============================================"
echo "===== Server: Folder Download Test ====="
echo "============================================"
echo "Downloading folder from server ($SERVER_OUTPUT) to $LOCAL_DOWNLOADS..."
java -jar ../ComFaaS.jar client downloadfolder \
    -server $SERVER -p $PORT -l server \
    -remote "$SERVER_OUTPUT/TestFolder" \
    -local "$LOCAL_DOWNLOADS"
echo "============================================"


##############################################
echo "============================================"
echo "Press any key to continue..."
read -n 1 -s
echo "============================================"
##############################################
echo "============================================"
echo "===== Server: Folder Delete Test ====="
echo "============================================"
echo "Deleting test folder from server ($SERVER_OUTPUT)..."
java -jar ../ComFaaS.jar client deletefolder \
    -server $SERVER -p $PORT -l server \
    -folder "$SERVER_OUTPUT/TestFolder"
echo "============================================"

##############################################

# echo ""
# echo "============================================"
# echo "===== Additional Tests ====="
# echo "============================================"
# echo "===== Test 1: Upload file to Server ====="
# java -jar ../ComFaaS.jar client upload \
#     -server $CLIENT_SERVER -p $CLIENT_PORT \
#     -local "$FILE_FOLDER/5KB_file.dat" \
#     -remote "$CLIENT_INPUT"
# echo "============================================"
# echo "===== Test 1.5: Upload another file to Server ====="
# java -jar ../ComFaaS.jar client upload \
#     -server $CLIENT_SERVER -p $CLIENT_PORT \
#     -local "$FILE_FOLDER/500KB_file.dat" \
#     -remote "$CLIENT_INPUT"
# echo "============================================"
# echo "===== Test 1.9: Upload another file to Server ====="
# java -jar ../ComFaaS.jar client upload \
#     -server $CLIENT_SERVER -p $CLIENT_PORT \
#     -local "$FILE_FOLDER/50MB_file.dat" \
#     -remote "$CLIENT_INPUT"
# echo "============================================"
# echo "===== Test 2: List files in Client/Input ====="
# java -jar ../ComFaaS.jar client listfiles \
#     -server $CLIENT_SERVER -p $CLIENT_PORT \
#     -remote "$CLIENT_INPUT"
# echo "============================================"
# echo "===== Additional Tests Completed ====="
