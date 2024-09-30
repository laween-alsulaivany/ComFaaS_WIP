#!/bin/bash
# Be sure to have mpich, python3.##-venv, python3-dev

# javac -d out comfass/Main.java 
# jar cf ComFaaS.jar -C out/ .
# jar cmf manifest.txt ComFaaS.jar -C out/ .
# rm -r out/
# java -jar ComFaaS.jar server init
# # source .serverVenv/bin/activate
# # env MPICC=/usr/bin MPI4PY_BUILD_BACKEND="scikit-build-core" pip install mpi4py
# # deactivate

java -jar ComFaaS.jar server run -p 12353

# comment this one out when in released to the git.