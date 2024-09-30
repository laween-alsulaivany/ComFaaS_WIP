#!/bin/bash

# javac -d out comfass/Main.java 
# jar cf ComFaaS.jar -C out/ .
# jar cmf manifest.txt ComFaaS.jar -C out/ .
# rm -r out/
# java -jar ComFaaS.jar edge init
# source .edgeVenv/bin/activate
# env MPICC=/usr/bin MPI4PY_BUILD_BACKEND="scikit-build-core" pip install mpi4py
# deactivate

java -jar ComFaaS.jar edge run -server 127.0.0.1 -p 12353 -t cloud -np 1 -tn WaitFor3Seconds.c -lang C
echo "----------------------"
java -jar ComFaaS.jar edge run -server 127.0.0.1 -p 12353 -t edge -np 1 -tn WaitFor3Seconds.c -lang C
echo "----------------------"
java -jar ComFaaS.jar edge run -server 127.0.0.1 -p 12353 -t edge -np 4 -tn mpich_pi_reduce.c -lang C
echo "----------------------"
java -jar ComFaaS.jar edge run -server 127.0.0.1 -p 12353 -t cloud -np 4 -tn mpich_pi_reduce.c -lang C
echo "----------------------"
java -jar ComFaaS.jar edge run -server 127.0.0.1 -p 12353 -t cloud -np 1 -tn WaitFor3Seconds.py -lang python
echo "----------------------"
java -jar ComFaaS.jar edge run -server 127.0.0.1 -p 12353 -t edge -np 1 -tn WaitFor3Seconds.py -lang python
echo "----------------------"
java -jar ComFaaS.jar edge run -server 127.0.0.1 -p 12353 -t edge -np 4 -tn mpiPython_test.py -lang python
echo "----------------------"
java -jar ComFaaS.jar edge run -server 127.0.0.1 -p 12353 -t cloud -np 4 -tn mpiPython_test.py -lang python
echo "----------------------"
java -jar ComFaaS.jar edge run -server 127.0.0.1 -p 12353 -t edge -np 4 -tn mpi4py_test.py -lang python
echo "----------------------"
java -jar ComFaaS.jar edge run -server 127.0.0.1 -p 12353 -t cloud -np 4 -tn mpi4py_test.py -lang python
echo "----------------------"
java -jar ComFaaS.jar edge run -server 127.0.0.1 -p 12353 -t cloud -np 1 -tn WaitFor3Seconds.java -lang java
echo "----------------------"
java -jar ComFaaS.jar edge run -server 127.0.0.1 -p 12353 -t edge -np 1 -tn WaitFor3Seconds.java -lang java

# tests two for the future.
#   These need to run with mpich
# java -jar ComFaaS.jar edge run -server 127.0.0.1 -p 12353 -t cloud -np 2 -tn WaitFor3Seconds.c -lang C
# java -jar ComFaaS.jar edge run -server 127.0.0.1 -p 12353 -t edge -np 2 -tn WaitFor3Seconds.c -lang C

# java -jar ComFaaS.jar edge run -server 127.0.0.1 -p 12353 -t cloud -np 2 -tn WaitFor3Seconds.py -lang python
# java -jar ComFaaS.jar edge run -server 127.0.0.1 -p 12353 -t edge -np 2 -tn WaitFor3Seconds.py -lang python
