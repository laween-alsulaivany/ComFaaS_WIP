#!/bin/bash

javac -d out comfass/Main.java 
jar cf ComFaaS.jar -C out/ .
jar cmf manifest.txt ComFaaS.jar -C out/ .
rm -r out/
java -jar ComFaaS.jar edge init
source .edgeVenv/bin/activate
env MPICC=/usr/bin MPI4PY_BUILD_BACKEND="scikit-build-core" pip install mpi4py
deactivate


java -jar ComFaaS.jar edge send -f 5KB_file.dat -c II
java -jar ComFaaS.jar edge request -f 5KB_file.dat -c IO
java -jar ComFaaS.jar edge delete -f 5KB_file.dat -c EI
