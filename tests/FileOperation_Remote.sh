#!/usr/bin/env bash
# FileOperation_Remote.sh: Test file operations on remote servers (Edge and Cloud)

echo "============================================"
echo "===== Remote File Operations Test Started ====="
echo "============================================"

# Source environment variables so that ROOT_DIR is set
source ../backend/configs/env.sh

# Define remote server connection details
EDGE_SERVER="140.186.71.123"
EDGE_PORT="12354"
CLOUD_SERVER="140.186.71.123"
CLOUD_PORT="12353"

# Define remote directory names as they are expected on the remote servers.
# (Remote servers expect these as their local structure.)
EDGE_INPUT="Edge/Input"
CLOUD_INPUT="Server/Input"

# Define local directories (all absolute based on ROOT_DIR)
# FILE_FOLDER: where individual test files are located.
FILE_FOLDER="$ROOT_DIR/Tests"
# TEST_FOLDER: the folder to be used for folder operations.
TEST_FOLDER="$ROOT_DIR/Tests/TestFolder"
# LOCAL_DOWNLOADS: where downloaded files/folders will be placed.
LOCAL_DOWNLOADS="$ROOT_DIR/LocalDownloads"

##############################################
echo "============================================"
echo "===== Remote Edge Server: File Upload Test ====="
echo "============================================"
echo "Uploading 5KB_file.dat from $FILE_FOLDER to remote Edge server ($EDGE_INPUT)..."
java -jar ../ComFaaS.jar edge upload \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -local "$FILE_FOLDER/5KB_file.dat" \
    -remote "$EDGE_INPUT"
echo "============================================"

##############################################
echo "============================================"
echo "===== Remote Edge Server: File List Test ====="
echo "============================================"
echo "Listing files in remote Edge server directory ($EDGE_INPUT)..."
java -jar ../ComFaaS.jar edge listfiles \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -dir "$EDGE_INPUT"
echo "============================================"

##############################################
echo "============================================"
echo "===== Remote Edge Server: File Download Test ====="
echo "============================================"
echo "Downloading 5KB_file.dat from remote Edge server ($EDGE_INPUT) to $LOCAL_DOWNLOADS..."
java -jar ../ComFaaS.jar edge download \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -remote "$EDGE_INPUT" \
    -file "5KB_file.dat" \
    -local "$LOCAL_DOWNLOADS"
echo "============================================"

##############################################
echo "============================================"
echo "===== Remote Edge Server: File Delete Test ====="
echo "============================================"
echo "Deleting 5KB_file.dat from remote Edge server ($EDGE_INPUT)..."
java -jar ../ComFaaS.jar edge deletefile \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -folder "$EDGE_INPUT" \
    -file "5KB_file.dat"
echo "============================================"

##############################################
echo "============================================"
echo "===== Remote Edge Server: Folder Upload Test ====="
echo "============================================"
# To ensure that only the test folder is affected on the remote side,
# upload the local test folder to a subdirectory on the remote server.
echo "Uploading folder ($TEST_FOLDER) to remote Edge server as subfolder 'TestFolder'..."
java -jar ../ComFaaS.jar edge uploadfolder \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -local "$TEST_FOLDER" \
    -remote "$EDGE_INPUT/TestFolder"
echo "============================================"

##############################################
echo "============================================"
echo "===== Remote Edge Server: Folder Download Test ====="
echo "============================================"
echo "Downloading folder from remote Edge server ($EDGE_INPUT/TestFolder) to $LOCAL_DOWNLOADS..."
java -jar ../ComFaaS.jar edge downloadfolder \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -remote "$EDGE_INPUT/TestFolder" \
    -local "$LOCAL_DOWNLOADS"
echo "============================================"

##############################################
echo "============================================"
echo "===== Remote Edge Server: Folder Delete Test ====="
echo "============================================"
echo "Deleting test folder from remote Edge server ($EDGE_INPUT/TestFolder)..."
java -jar ../ComFaaS.jar edge deletefolder \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -folder "$EDGE_INPUT/TestFolder"
echo "============================================"

##############################################
echo "============================================"
echo "===== Remote Cloud Server: File Upload Test ====="
echo "============================================"
echo "Uploading 5KB_file.dat from $FILE_FOLDER to remote Cloud server ($CLOUD_INPUT)..."
java -jar ../ComFaaS.jar edge upload \
    -server $CLOUD_SERVER -p $CLOUD_PORT \
    -local "$FILE_FOLDER/5KB_file.dat" \
    -remote "$CLOUD_INPUT"
echo "============================================"

##############################################
echo "============================================"
echo "===== Remote Cloud Server: File List Test ====="
echo "============================================"
echo "Listing files in remote Cloud server directory ($CLOUD_INPUT)..."
java -jar ../ComFaaS.jar edge listfiles \
    -server $CLOUD_SERVER -p $CLOUD_PORT \
    -dir "$CLOUD_INPUT"
echo "============================================"

##############################################
echo "============================================"
echo "===== Remote Cloud Server: File Download Test ====="
echo "============================================"
echo "Downloading 5KB_file.dat from remote Cloud server ($CLOUD_INPUT) to $LOCAL_DOWNLOADS..."
java -jar ../ComFaaS.jar edge download \
    -server $CLOUD_SERVER -p $CLOUD_PORT \
    -remote "$CLOUD_INPUT" \
    -file "5KB_file.dat" \
    -local "$LOCAL_DOWNLOADS"
echo "============================================"

##############################################
echo "============================================"
echo "===== Remote Cloud Server: File Delete Test ====="
echo "============================================"
echo "Deleting 5KB_file.dat from remote Cloud server ($CLOUD_INPUT)..."
java -jar ../ComFaaS.jar edge deletefile \
    -server $CLOUD_SERVER -p $CLOUD_PORT \
    -folder "$CLOUD_INPUT" \
    -file "5KB_file.dat"
echo "============================================"

##############################################
echo "============================================"
echo "===== Remote Cloud Server: Folder Upload Test ====="
echo "============================================"
echo "Uploading folder ($TEST_FOLDER) to remote Cloud server as subfolder 'TestFolder'..."
java -jar ../ComFaaS.jar edge uploadfolder \
    -server $CLOUD_SERVER -p $CLOUD_PORT \
    -local "$TEST_FOLDER" \
    -remote "$CLOUD_INPUT/TestFolder"
echo "============================================"

##############################################
echo "============================================"
echo "===== Remote Cloud Server: Folder Download Test ====="
echo "============================================"
echo "Downloading folder from remote Cloud server ($CLOUD_INPUT/TestFolder) to $LOCAL_DOWNLOADS..."
java -jar ../ComFaaS.jar edge downloadfolder \
    -server $CLOUD_SERVER -p $CLOUD_PORT \
    -remote "$CLOUD_INPUT/TestFolder" \
    -local "$LOCAL_DOWNLOADS"
echo "============================================"

##############################################
echo "============================================"
echo "===== Remote Cloud Server: Folder Delete Test ====="
echo "============================================"
echo "Deleting test folder from remote Cloud server ($CLOUD_INPUT/TestFolder)..."
java -jar ../ComFaaS.jar edge deletefolder \
    -server $CLOUD_SERVER -p $CLOUD_PORT \
    -folder "$CLOUD_INPUT/TestFolder"
echo "============================================"

echo "============================================"
echo "===== Remote File and Folder Operations Test Completed ====="
echo "============================================"


# # FileOperation_Remote.sh: Test file operations on remote servers (Edge and Cloud)

# echo "===== Remote file operations test started ====="


# # Source environment variables so that ROOT_DIR is set
# source ../backend/configs/env.sh


# # Edge server tests
# EDGE_SERVER="140.186.71.123"
# EDGE_PORT="12354"
# EDGE_INPUT="Edge/Input"
# EDGE_TEST_FOLDER="TestFolder"

# # Cloud server tests
# CLOUD_SERVER="140.186.71.123"
# CLOUD_PORT="12353"
# CLOUD_INPUT="Server/Input"
# CLOUD_TEST_FOLDER="TestFolder"



# echo " "
# echo "== Edge server tests =="
# echo " "
# # 1) Upload a local file "5KB_file.dat" to the Edge server
# echo "Uploading 5KB_file.dat to Edge server $EDGE_INPUT..."
# java -jar ../ComFaaS.jar edge upload \
#     -server $EDGE_SERVER -p $EDGE_PORT \
#     -local 5KB_file.dat \
#     -remote $EDGE_INPUT

# echo "------------------------"

# # 2) List files on the Edge server in EdgeInput
# echo "Listing files in $EDGE_INPUT on the Edge server..."
# java -jar ../ComFaaS.jar edge listfiles \
#     -server $EDGE_SERVER -p $EDGE_PORT \
#     -dir $EDGE_INPUT

# echo "------------------------"

# # 3) Download that file back from Edge server to a local folder called "LocalDownloads"
# echo "Downloading 5KB_file.dat from $EDGE_INPUT on Edge server..."
# java -jar ../ComFaaS.jar edge download \
#     -server $EDGE_SERVER -p $EDGE_PORT \
#     -remote $EDGE_INPUT \
#     -file 5KB_file.dat \
#     -local LocalDownloads

# echo "------------------------"

# # 4) Delete it from the Edge server
# echo "Deleting 5KB_file.dat from Edge server $EDGE_INPUT..."
# java -jar ../ComFaaS.jar edge deletefile \
#     -server $EDGE_SERVER -p $EDGE_PORT \
#     -folder $EDGE_INPUT \
#     -file 5KB_file.dat

# echo "------------------------"

# # 5) Upload a folder to the Edge server
# echo "Uploading folder $EDGE_TEST_FOLDER to Edge server ..."
# java -jar ../ComFaaS.jar edge uploadfolder \
#     -server $EDGE_SERVER -p $EDGE_PORT \
#     -local $EDGE_TEST_FOLDER \
#     -remote $EDGE_INPUT

# echo "------------------------"

# # 6) Download that folder back from Edge server to a local folder called "LocalDownloads"
# echo "Downloading folder $EDGE_INPUT on Edge server..."
# java -jar ../ComFaaS.jar edge downloadfolder \
#     -server $EDGE_SERVER -p $EDGE_PORT \
#     -remote $EDGE_INPUT\
#     -local LocalDownloads

# echo "------------------------"

# # 7) Delete the folder from the Edge server
# echo "Deleting folder $EDGE_INPUT from Edge server ..."
# java -jar ../ComFaaS.jar edge deletefolder \
#     -server $EDGE_SERVER -p $EDGE_PORT \
#     -folder $EDGE_INPUT

# echo "------------------------"

# # Repeat the same steps for the Cloud server

# echo " "
# echo "== Cloud server tests =="
# echo " "
# # 1) Upload a local file "5KB_file.dat" to the Cloud server
# echo "Uploading 5KB_file.dat to Cloud server $CLOUD_INPUT..."
# java -jar ../ComFaaS.jar edge upload \
#     -server $CLOUD_SERVER -p $CLOUD_PORT \
#     -local 5KB_file.dat \
#     -remote $CLOUD_INPUT

# echo "------------------------"

# # 2) List files on the Cloud server in CloudInput
# echo "Listing files in $CLOUD_INPUT on the Cloud server..."
# java -jar ../ComFaaS.jar edge listfiles \
#     -server $CLOUD_SERVER -p $CLOUD_PORT \
#     -dir $CLOUD_INPUT

# echo "------------------------"

# # 3) Download that file back from Cloud server to a local folder called "LocalDownloads"
# echo "Downloading 5KB_file.dat from $CLOUD_INPUT on Cloud server..."
# java -jar ../ComFaaS.jar edge download \
#     -server $CLOUD_SERVER -p $CLOUD_PORT \
#     -remote $CLOUD_INPUT \
#     -file 5KB_file.dat \
#     -local LocalDownloads

# echo "------------------------"

# # 4) Delete it from the Cloud server
# echo "Deleting 5KB_file.dat from Cloud server $CLOUD_INPUT..."
# java -jar ../ComFaaS.jar edge deletefile \
#     -server $CLOUD_SERVER -p $CLOUD_PORT \
#     -folder $CLOUD_INPUT \
#     -file 5KB_file.dat

# echo "------------------------"

# # 5) Upload a folder to the Cloud server
# echo "Uploading folder $CLOUD_TEST_FOLDER to Cloud server $CLOUD_INPUT..."
# java -jar ../ComFaaS.jar edge uploadfolder \
#     -server $CLOUD_SERVER -p $CLOUD_PORT \
#     -local $CLOUD_TEST_FOLDER \
#     -remote $CLOUD_INPUT

# echo "------------------------"

# # 6) Download that folder back from Cloud server to a local folder called "LocalDownloads"
# echo "Downloading folder $CLOUD_INPUT on Cloud server..."
# java -jar ../ComFaaS.jar edge downloadfolder \
#     -server $CLOUD_SERVER -p $CLOUD_PORT \
#     -remote $CLOUD_INPUT \
#     -local LocalDownloads

# echo "------------------------"

# # 7) Delete the folder from the Cloud server
# echo "Deleting folder $CLOUD_INPUT from Cloud server ..."
# java -jar ../ComFaaS.jar edge deletefolder \
#     -server $CLOUD_SERVER -p $CLOUD_PORT \
#     -folder $CLOUD_INPUT

# echo "------------------------"



# echo "File and folder operations test completed."
