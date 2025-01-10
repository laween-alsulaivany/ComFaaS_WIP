#!/bin/bash
echo "Running edge tasks locally..."

echo "Task 1"
java -jar ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12353 -t cloud -np 1 -tn WaitFor3Seconds.c -lang C
echo "----------------------"
echo "Task 2"
java -jar ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12354 -t edge -np 1 -tn WaitFor3Seconds.c -lang C
echo "----------------------"
echo "Task 3"
java -jar ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12354 -t edge -np 4 -tn mpich_pi_reduce.c -lang C
echo "----------------------"
echo "Task 4"
java -jar ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12353 -t cloud -np 4 -tn mpich_pi_reduce.c -lang C
echo "----------------------"
echo "Task 5"
java -jar ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12353 -t cloud -np 1 -tn WaitFor3Seconds.py -lang python
echo "----------------------"
echo "Task 6"
java -jar ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12354 -t edge -np 1 -tn WaitFor3Seconds.py -lang python
echo "----------------------"
echo "Task 7"
java -jar ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12354 -t edge -np 4 -tn mpiPython_test.py -lang python
echo "----------------------"
echo "Task 8"
java -jar ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12353 -t cloud -np 4 -tn mpiPython_test.py -lang python
echo "----------------------"
echo "Task 9"
java -jar ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12354 -t edge -np 4 -tn mpi4py_test.py -lang python
echo "----------------------"
echo "Task 10"
java -jar ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12353 -t cloud -np 4 -tn mpi4py_test.py -lang python
echo "----------------------"
echo "Task 11"
java -jar ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12353 -t cloud -np 1 -tn WaitFor3Seconds.java -lang java
echo "----------------------"
echo "Task 12"
java -jar ComFaaS.jar edge remotetask -server 127.0.0.1 -p 12354 -t edge -np 1 -tn WaitFor3Seconds.java -lang java
