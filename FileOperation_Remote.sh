# FileOperation_Remote.sh: Test file operations on remote servers (Edge and Cloud)

echo "===== Remote file operations test started ====="


# Edge server tests
EDGE_SERVER="140.186.71.123"
EDGE_PORT="12354"
EDGE_INPUT="EdgeInput"
EDGE_TEST_FOLDER="TestFolder"

# Cloud server tests
CLOUD_SERVER="140.186.71.123"
CLOUD_PORT="12353"
CLOUD_INPUT="ServerInput"
CLOUD_TEST_FOLDER="TestFolder"

echo " "
echo "== Edge server tests =="
echo " "
# 1) Upload a local file "5KB_file.dat" to the Edge server
echo "Uploading 5KB_file.dat to Edge server $EDGE_INPUT..."
java -jar ComFaaS.jar edge upload \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -local 5KB_file.dat \
    -remote $EDGE_INPUT

echo "------------------------"

# 2) List files on the Edge server in EdgeInput
echo "Listing files in $EDGE_INPUT on the Edge server..."
java -jar ComFaaS.jar edge listfiles \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -dir $EDGE_INPUT

echo "------------------------"

# 3) Download that file back from Edge server to a local folder called "LocalDownloads"
echo "Downloading 5KB_file.dat from $EDGE_INPUT on Edge server..."
java -jar ComFaaS.jar edge download \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -remote $EDGE_INPUT \
    -file 5KB_file.dat \
    -local LocalDownloads

echo "------------------------"

# 4) Delete it from the Edge server
echo "Deleting 5KB_file.dat from Edge server $EDGE_INPUT..."
java -jar ComFaaS.jar edge deletefile \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -folder $EDGE_INPUT \
    -file 5KB_file.dat

echo "------------------------"

# 5) Upload a folder to the Edge server
echo "Uploading folder $EDGE_TEST_FOLDER to Edge server ..."
java -jar ComFaaS.jar edge uploadfolder \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -local $EDGE_TEST_FOLDER \
    -remote $EDGE_INPUT

echo "------------------------"

# 6) Download that folder back from Edge server to a local folder called "LocalDownloads"
echo "Downloading folder $EDGE_INPUT on Edge server..."
java -jar ComFaaS.jar edge downloadfolder \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -remote $EDGE_INPUT\
    -local LocalDownloads

echo "------------------------"

# 7) Delete the folder from the Edge server
echo "Deleting folder $EDGE_INPUT from Edge server ..."
java -jar ComFaaS.jar edge deletefolder \
    -server $EDGE_SERVER -p $EDGE_PORT \
    -folder $EDGE_INPUT

echo "------------------------"

# Repeat the same steps for the Cloud server

echo " "
echo "== Cloud server tests =="
echo " "
# 1) Upload a local file "5KB_file.dat" to the Cloud server
echo "Uploading 5KB_file.dat to Cloud server $CLOUD_INPUT..."
java -jar ComFaaS.jar edge upload \
    -server $CLOUD_SERVER -p $CLOUD_PORT \
    -local 5KB_file.dat \
    -remote $CLOUD_INPUT

echo "------------------------"

# 2) List files on the Cloud server in CloudInput
echo "Listing files in $CLOUD_INPUT on the Cloud server..."
java -jar ComFaaS.jar edge listfiles \
    -server $CLOUD_SERVER -p $CLOUD_PORT \
    -dir $CLOUD_INPUT

echo "------------------------"

# 3) Download that file back from Cloud server to a local folder called "LocalDownloads"
echo "Downloading 5KB_file.dat from $CLOUD_INPUT on Cloud server..."
java -jar ComFaaS.jar edge download \
    -server $CLOUD_SERVER -p $CLOUD_PORT \
    -remote $CLOUD_INPUT \
    -file 5KB_file.dat \
    -local LocalDownloads

echo "------------------------"

# 4) Delete it from the Cloud server
echo "Deleting 5KB_file.dat from Cloud server $CLOUD_INPUT..."
java -jar ComFaaS.jar edge deletefile \
    -server $CLOUD_SERVER -p $CLOUD_PORT \
    -folder $CLOUD_INPUT \
    -file 5KB_file.dat

echo "------------------------"

# 5) Upload a folder to the Cloud server
echo "Uploading folder $CLOUD_TEST_FOLDER to Cloud server $CLOUD_INPUT..."
java -jar ComFaaS.jar edge uploadfolder \
    -server $CLOUD_SERVER -p $CLOUD_PORT \
    -local $CLOUD_TEST_FOLDER \
    -remote $CLOUD_INPUT

echo "------------------------"

# 6) Download that folder back from Cloud server to a local folder called "LocalDownloads"
echo "Downloading folder $CLOUD_INPUT on Cloud server..."
java -jar ComFaaS.jar edge downloadfolder \
    -server $CLOUD_SERVER -p $CLOUD_PORT \
    -remote $CLOUD_INPUT \
    -local LocalDownloads

echo "------------------------"

# 7) Delete the folder from the Cloud server
echo "Deleting folder $CLOUD_INPUT from Cloud server ..."
java -jar ComFaaS.jar edge deletefolder \
    -server $CLOUD_SERVER -p $CLOUD_PORT \
    -folder $CLOUD_INPUT

echo "------------------------"



echo "File and folder operations test completed."
