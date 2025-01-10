#!/usr/bin/env bash

echo "===== Remote file operations test started ====="
 

# 1) Upload a local file "5KB_file.dat" to the Edge server
echo "Uploading 5KB_file.dat to Edge server EdgeInput..."
java -jar ComFaaS.jar edge upload \
    -server 140.186.71.123 -p 12354 \
    -local 5KB_file.dat \
    -remote EdgeInput

echo "------------------------"

# 2) List files on the Edge server in EdgeInput
echo "Listing files in EdgeInput on the Edge server..."
java -jar ComFaaS.jar edge listfiles \
    -server 140.186.71.123 -p 12354 \
    -dir EdgeInput

echo "------------------------"

# 3) Download that file back from Edge server to a local folder called "LocalDownloads"
echo "Downloading 5KB_file.dat from EdgeInput on Edge server..."
java -jar ComFaaS.jar edge download \
    -server 140.186.71.123 -p 12354 \
    -remote EdgeInput \
    -file 5KB_file.dat \
    -local LocalDownloads

echo "------------------------"

# 4) Delete it from the Edge server
echo "Deleting 5KB_file.dat from Edge server EdgeInput..."
java -jar ComFaaS.jar edge deletefile \
    -server 140.186.71.123 -p 12354 \
    -folder EdgeInput \
    -file 5KB_file.dat

echo "------------------------"

# 5) Repeat the same steps but for the Cloud server (port 12353)
#    Just change -server and -p

echo "Uploading 5KB_file.dat to Cloud server ServerInput..."
java -jar ComFaaS.jar edge upload \
    -server 140.186.71.124 -p 12353 \
    -local 5KB_file.dat \
    -remote ServerInput

echo "------------------------"

echo "Listing files in ServerInput on the Cloud server..."
java -jar ComFaaS.jar edge listfiles \
    -server 140.186.71.124 -p 12353 \
    -dir ServerInput

echo "------------------------"

echo "Downloading 5KB_file.dat from Cloud server..."
java -jar ComFaaS.jar edge download \
    -server 140.186.71.124 -p 12353 \
    -remote ServerInput \
    -file 5KB_file.dat \
    -local LocalDownloads

echo "------------------------"

echo "Deleting 5KB_file.dat from Cloud server..."
java -jar ComFaaS.jar edge deletefile \
    -server 140.186.71.124 -p 12353 \
    -folder ServerInput \
    -file 5KB_file.dat

echo "------------------------"

echo "File operations test completed."

