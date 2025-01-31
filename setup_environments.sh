# setup_environments.sh: Set up the server and edge environments for ComFaaS

# Log utility function
log() {
    echo "[INFO] $1"
}

# Exit with an error message
error_exit() {
    echo "[ERROR] $1" >&2
    exit 1
}

# Step 1: Ensure required system packages
PYTHON_VERSION=$(python3 -c "import sys; print(f'{sys.version_info.major}.{sys.version_info.minor}')")
PACKAGES=("python${PYTHON_VERSION}-venv" "python${PYTHON_VERSION}-dev" "mpich")

log "Checking required packages..."
for PACKAGE in "${PACKAGES[@]}"; do
    if dpkg -s "$PACKAGE" >/dev/null 2>&1; then
        log "$PACKAGE is already installed."
    else
        log "$PACKAGE is not installed. Installing..."
        sudo apt-get update && sudo apt-get install -y "$PACKAGE" || error_exit "Failed to install $PACKAGE"
sudo apt-get install -y libopenmpi-dev openmpi-bin || error_exit "Failed to install OpenMPI"

    fi
done

# Step 2: Set up directories for server and edge
setup_directories() {
    log "Setting up directories for $1..."
    case $1 in
        ("server")
            mkdir -p ServerPrograms ServerInput ServerOutput || error_exit "Failed to create server directories"
            ;;
        ("edge")
            mkdir -p EdgePrograms EdgeInput EdgeOutput || error_exit "Failed to create edge directories"
            ;;
        (*)
            error_exit "Invalid argument for directory setup: $1"
            ;;
    esac
}

setup_directories "server"
setup_directories "edge"

# Step 3: Set up Python virtual environments for server and edge
setup_virtualenv() {
    ENV_NAME=".$1Venv"
    log "Setting up Python virtual environment: $ENV_NAME..."
    if [ ! -d "$ENV_NAME" ]; then
        python3 -m venv "$ENV_NAME" || error_exit "Failed to create virtual environment: $ENV_NAME"
        log "Virtual environment $ENV_NAME created."
    else
        log "Virtual environment $ENV_NAME already exists."
    fi

    . "$ENV_NAME/bin/activate" || error_exit "Failed to activate virtual environment: $ENV_NAME"
    pip install --upgrade pip || error_exit "Failed to upgrade pip"
    pip install mpiPython mpi4py || error_exit "Failed to install Python dependencies"
    deactivate || unset VIRTUAL_ENV
}

setup_virtualenv "server"
setup_virtualenv "edge"



# Step 4: Compile Java application
log "Compiling Java application..."
if [ ! -f "comfaas/Main.java" ]; then
    error_exit "Main.java not found in comfaas directory!"
fi

mkdir -p out
javac -d out comfaas/Main.java || error_exit "Failed to compile Java files"
jar cmf manifest.txt ComFaaS.jar -C out/ . || error_exit "Failed to create JAR file"
rm -r out/

# Step 5: Initialize server and edge
log "Initializing server with ComFaaS..."
java -jar ComFaaS.jar server init || error_exit "Failed to initialize server"

log "Initializing edge with ComFaaS..."
java -jar ComFaaS.jar edge init || error_exit "Failed to initialize edge"

log "Setup and initialization for server and edge completed successfully."
