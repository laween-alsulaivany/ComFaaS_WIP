#!/bin/bash

# Get the current Python 3 version (e.g., 3.8)
PYTHON_VERSION=$(python3 -c "import sys; print(f'{sys.version_info.major}.{sys.version_info.minor}')")

# Construct the package name
PACKAGE="python${PYTHON_VERSION}-venv"

# Check if the package is installed
if dpkg -s "$PACKAGE" >/dev/null 2>&1; then
    echo "$PACKAGE is already installed."
else
    echo "$PACKAGE is not installed. Installing it now..."
    sudo apt-get update
    sudo apt-get install -y "$PACKAGE"
fi

#!/bin/bash

# Get the current Python 3 version (e.g., 3.8)
PYTHON_VERSION=$(python3 -c "import sys; print(f'{sys.version_info.major}.{sys.version_info.minor}')")

# Construct the package names
PYTHON_DEV_PACKAGE="python${PYTHON_VERSION}-dev"
MPICH_PACKAGE="mpich"

# Function to check if a package is installed and install it if not
check_and_install() {
    PACKAGE=$1
    if dpkg -s "$PACKAGE" >/dev/null 2>&1; then
        echo "$PACKAGE is already installed."
    else
        echo "$PACKAGE is not installed. Installing it now..."
        sudo apt-get update
        sudo apt-get install -y "$PACKAGE"
    fi
}

# Check and install the required packages
check_and_install "$PYTHON_DEV_PACKAGE"
check_and_install "$MPICH_PACKAGE"


javac -d out comfaas/Main.java 
jar cf ComFaaS.jar -C out/ .
jar cmf manifest.txt ComFaaS.jar -C out/ .
rm -r out/
java -jar ComFaaS.jar edge init
source .edgeVenv/bin/activate

python -m mpiPython

env MPICC=/usr/bin MPI4PY_BUILD_BACKEND="scikit-build-core" pip install mpi4py
deactivate