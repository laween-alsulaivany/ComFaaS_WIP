#!/usr/bin/env bash
# FileOperation_Local.sh: Test file operations on local machine

echo "============================================"
echo "===== Local File Operations Test Started ====="
echo "============================================"

# Source environment variables so that ROOT_DIR is set
source ../backend/configs/env.sh

# Define server connection details
EDGE_SERVER="127.0.0.1"
EDGE_PORT="12354"
CLOUD_SERVER="127.0.0.1"
CLOUD_PORT="12353"

# Define directories (all absolute based on ROOT_DIR)
EDGE_INPUT="$ROOT_DIR/edge/Input"
CLOUD_INPUT="$ROOT_DIR/server/Input"
# Test folder containing files to upload (ensure this folder exists)
EDGE_TEST_FOLDER="$ROOT_DIR/Tests/TestFolder"
CLOUD_TEST_FOLDER="$ROOT_DIR/Tests/TestFolder"
# Local downloads folder (where downloaded files/folders will go)
LOCAL_DOWNLOADS="$ROOT_DIR/LocalDownloads"
# Folder containing test files for single file operations
FILE_FOLDER="$ROOT_DIR/Tests"

##############################################
echo "============================================"
echo "===== Edge Server: File Upload Test ====="
echo "============================================"
echo "Uploading 5KB_file.dat from $FILE_FOLDER to Edge server ($EDGE_INPUT)..."
java -jar ../ComFaaS.jar edge upload \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -local "$FILE_FOLDER/5KB_file.dat" \
    -remote "$EDGE_INPUT"
echo "============================================"

##############################################
echo "============================================"
echo "===== Edge Server: File List Test ====="
echo "============================================"
echo "Listing files in $EDGE_INPUT on Edge server..."
java -jar ../ComFaaS.jar edge listfiles \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -dir "$EDGE_INPUT"
echo "============================================"

##############################################
echo "============================================"
echo "===== Edge Server: File Download Test ====="
echo "============================================"
echo "Downloading 5KB_file.dat from $EDGE_INPUT on Edge server to $LOCAL_DOWNLOADS..."
java -jar ../ComFaaS.jar edge download \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -remote "$EDGE_INPUT" \
    -file "5KB_file.dat" \
    -local "$LOCAL_DOWNLOADS"
echo "============================================"

##############################################
echo "============================================"
echo "===== Edge Server: File Delete Test ====="
echo "============================================"
echo "Deleting 5KB_file.dat from Edge server ($EDGE_INPUT)..."
java -jar ../ComFaaS.jar edge deletefile \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -folder "$EDGE_INPUT" \
    -file "5KB_file.dat"
echo "============================================"

##############################################
echo "============================================"
echo "===== Edge Server: Folder Upload Test ====="
echo "============================================"
# IMPORTANT: The current uploadFolder command uploads only the files from $EDGE_TEST_FOLDER
# directly into the remote directory. If you want to preserve the folder structure,
# consider uploading to "$EDGE_INPUT/TestFolder" instead.
echo "Uploading folder ($EDGE_TEST_FOLDER) to Edge server..."
java -jar ../ComFaaS.jar edge uploadfolder \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -local "$EDGE_TEST_FOLDER" \
    -remote "$EDGE_INPUT/TestFolder"
echo "============================================"

##############################################
echo "============================================"
echo "===== Edge Server: Folder Download Test ====="
echo "============================================"
echo "Downloading folder from Edge server ($EDGE_INPUT/TestFolder) to $LOCAL_DOWNLOADS..."
java -jar ../ComFaaS.jar edge downloadfolder \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -remote "$EDGE_INPUT/TestFolder" \
    -local "$LOCAL_DOWNLOADS"
echo "============================================"

##############################################
echo "============================================"
echo "===== Edge Server: Folder Delete Test ====="
echo "============================================"
echo "Deleting test folder from Edge server ($EDGE_INPUT/TestFolder)..."
java -jar ../ComFaaS.jar edge deletefolder \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -folder "$EDGE_INPUT/TestFolder"
echo "============================================"

##############################################
echo "============================================"
echo "===== Cloud Server: File Upload Test ====="
echo "============================================"
echo "Uploading 5KB_file.dat from $FILE_FOLDER to Cloud server ($CLOUD_INPUT)..."
java -jar ../ComFaaS.jar edge upload \
    -server $CLOUD_SERVER -p $CLOUD_PORT \
    -local "$FILE_FOLDER/5KB_file.dat" \
    -remote "$CLOUD_INPUT"
echo "============================================"

##############################################
echo "============================================"
echo "===== Cloud Server: File List Test ====="
echo "============================================"
echo "Listing files in $CLOUD_INPUT on Cloud server..."
java -jar ../ComFaaS.jar edge listfiles \
    -server $CLOUD_SERVER -p $CLOUD_PORT \
    -dir "$CLOUD_INPUT"
echo "============================================"

##############################################
echo "============================================"
echo "===== Cloud Server: File Download Test ====="
echo "============================================"
echo "Downloading 5KB_file.dat from $CLOUD_INPUT on Cloud server to $LOCAL_DOWNLOADS..."
java -jar ../ComFaaS.jar edge download \
    -server $CLOUD_SERVER -p $CLOUD_PORT \
    -remote "$CLOUD_INPUT" \
    -file "5KB_file.dat" \
    -local "$LOCAL_DOWNLOADS"
echo "============================================"

##############################################
echo "============================================"
echo "===== Cloud Server: File Delete Test ====="
echo "============================================"
echo "Deleting 5KB_file.dat from Cloud server ($CLOUD_INPUT)..."
java -jar ../ComFaaS.jar edge deletefile \
    -server $CLOUD_SERVER -p $CLOUD_PORT \
    -folder "$CLOUD_INPUT" \
    -file "5KB_file.dat"
echo "============================================"

##############################################
echo "============================================"
echo "===== Cloud Server: Folder Upload Test ====="
echo "============================================"
echo "Uploading folder ($CLOUD_TEST_FOLDER) to Cloud server..."
java -jar ../ComFaaS.jar edge uploadfolder \
    -server $CLOUD_SERVER -p $CLOUD_PORT \
    -local "$CLOUD_TEST_FOLDER" \
    -remote "$CLOUD_INPUT/TestFolder"
echo "============================================"

##############################################
echo "============================================"
echo "===== Cloud Server: Folder Download Test ====="
echo "============================================"
echo "Downloading folder from Cloud server ($CLOUD_INPUT/TestFolder) to $LOCAL_DOWNLOADS..."
java -jar ../ComFaaS.jar edge downloadfolder \
    -server $CLOUD_SERVER -p $CLOUD_PORT \
    -remote "$CLOUD_INPUT/TestFolder" \
    -local "$LOCAL_DOWNLOADS"
echo "============================================"

##############################################
echo "============================================"
echo "===== Cloud Server: Folder Delete Test ====="
echo "============================================"
echo "Deleting test folder from Cloud server ($CLOUD_INPUT/TestFolder)..."
java -jar ../ComFaaS.jar edge deletefolder \
    -server $CLOUD_SERVER -p $CLOUD_PORT \
    -folder "$CLOUD_INPUT/TestFolder"
echo "============================================"

##############################################
echo "============================================"
echo "===== All File and Folder Operations Test Completed ====="
echo "============================================"

##############################################
echo ""
echo "============================================"
echo "===== Additional Tests ====="
echo "============================================"
echo "===== Test 1: Upload file to Edge Server ====="
java -jar ../ComFaaS.jar edge upload \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -local "$FILE_FOLDER/5KB_file.dat" \
    -remote "$EDGE_INPUT"
echo "============================================"
echo "===== Test 1.5: Upload another file to Edge Server ====="
java -jar ../ComFaaS.jar edge upload \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -local "$FILE_FOLDER/500KB_file.dat" \
    -remote "$EDGE_INPUT"
echo "============================================"
echo "===== Test 1.9: Upload another file to Edge Server ====="
java -jar ../ComFaaS.jar edge upload \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -local "$FILE_FOLDER/50MB_file.dat" \
    -remote "$EDGE_INPUT"
echo "============================================"
echo "===== Test 2: List files in EdgeInput ====="
java -jar ../ComFaaS.jar edge listfiles \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -remote "$EDGE_INPUT"
echo "============================================"
echo "===== Additional Tests Completed ====="
