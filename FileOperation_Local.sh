#!/usr/bin/env bash

echo "===== Local file operations test started ====="

echo "===== Test 1: Upload file to Edge Server ====="
java -jar ComFaaS.jar edge upload \
    -server 127.0.0.1 -p 12354 \
    -local 5KB_file.dat \
    -remote EdgeInput

echo "===== Test 1.5: Upload another file to Edge Server ====="
java -jar ComFaaS.jar edge upload \
    -server 127.0.0.1 -p 12354 \
    -local 500KB_file.dat \
    -remote EdgeInput

echo "===== Test 1.9: Upload another file to Edge Server ====="
java -jar ComFaaS.jar edge upload \
    -server 127.0.0.1 -p 12354 \
    -local 50MB_file.dat \
    -remote EdgeInput

echo "===== Test 2: List files in EdgeInput ====="
java -jar ComFaaS.jar edge listfiles \
    -server 127.0.0.1 -p 12354 \
    -dir EdgeInput

echo "===== Test 3: Download file from Edge Server ====="
java -jar ComFaaS.jar edge download \
    -server 127.0.0.1 -p 12354 \
    -remote EdgeInput \
    -file 5KB_file.dat \
    -local LocalDownloads

echo "===== Test 4: Delete file on Edge Server ====="
java -jar ComFaaS.jar edge deletefile \
    -server 127.0.0.1 -p 12354 \
    -folder EdgeInput \
    -file 5KB_file.dat

# Repeat on Cloud server (assuming Cloud is 127.0.0.1:12353)
echo "===== Test 5: Upload file to Cloud Server ====="
java -jar ComFaaS.jar edge upload \
    -server 127.0.0.1 -p 12353 \
    -local 5KB_file.dat \
    -remote ServerInput

echo "===== Test 6: List files in ServerInput ====="
java -jar ComFaaS.jar edge listfiles \
    -server 127.0.0.1 -p 12353 \
    -dir ServerInput

echo "===== Test 7: Download file from Cloud Server ====="
java -jar ComFaaS.jar edge download \
    -server 127.0.0.1 -p 12353 \
    -remote ServerInput \
    -file 5KB_file.dat \
    -local LocalDownloads

echo "===== Test 8: Delete file on Cloud Server ====="
java -jar ComFaaS.jar edge deletefile \
    -server 127.0.0.1 -p 12353 \
    -folder ServerInput \
    -file 5KB_file.dat

echo "===== File operations test completed ====="
