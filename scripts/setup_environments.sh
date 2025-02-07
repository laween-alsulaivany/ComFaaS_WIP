


#!/usr/bin/env bash
#
# setup_environments.sh - Set up the server and edge environments for ComFaaS.

# --------------------------------------------------------------------------------
# 1. Load environment variables
# --------------------------------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/../backend/configs/env.sh"

if [ -f "$ENV_FILE" ]; then
    source "$ENV_FILE"
else
    echo "[ERROR] env.sh not found at $ENV_FILE. Exiting..."
    exit 1
fi

# --------------------------------------------------------------------------------
# Helper Functions
# --------------------------------------------------------------------------------
log() {
    echo "[INFO] $1"
}

error_exit() {
    echo "[ERROR] $1" >&2
    exit 1
}

# --------------------------------------------------------------------------------
# 2. Ensure required system packages
# --------------------------------------------------------------------------------
PYTHON_VERSION=$(python3 -c "import sys; print(f'{sys.version_info.major}.{sys.version_info.minor}')")
PACKAGES=("python3-venv" "python3-dev" "mpich" "libopenmpi-dev" "openmpi-bin")

log "Checking required packages..."
for PACKAGE in "${PACKAGES[@]}"; do
    if dpkg -s "$PACKAGE" >/dev/null 2>&1; then
        log "$PACKAGE is already installed."
    else
        log "Installing $PACKAGE..."
        sudo apt-get update && sudo apt-get install -y "$PACKAGE" || error_exit "Failed to install $PACKAGE"
    fi
done

# --------------------------------------------------------------------------------
# 3. Set up directories for server and edge
# --------------------------------------------------------------------------------
setup_directories() {
    local TARGET="$1"
    case "$TARGET" in
        ("server")
            log "Setting up directories for the server..."
            mkdir -p "$SERVER_DIR/Programs" \
                     "$SERVER_DIR/Input" \
                     "$SERVER_DIR/Output" \
            || error_exit "Failed to create server directories"
            ;;
        ("edge")
            log "Setting up directories for the edge..."
            mkdir -p "$EDGE_DIR/Programs" \
                     "$EDGE_DIR/Input" \
                     "$EDGE_DIR/Output" \
            || error_exit "Failed to create edge directories"
            ;;
        (*)
            error_exit "Invalid target for directory setup: $TARGET"
            ;;
    esac
}

setup_directories "server"
setup_directories "edge"

# --------------------------------------------------------------------------------
# 4. Set up Python virtual environments
# --------------------------------------------------------------------------------
setup_virtualenv() {
    local ENV_PATH="$1"
    log "Setting up Python virtual environment: $ENV_PATH..."

    if [ ! -d "$ENV_PATH" ]; then
        python3 -m venv "$ENV_PATH" || error_exit "Failed to create virtual environment: $ENV_PATH"
        log "Virtual environment created: $ENV_PATH."
    else
        log "Virtual environment already exists: $ENV_PATH."
    fi

    # Activate, upgrade pip, and install dependencies
    source "$ENV_PATH/bin/activate" || error_exit "Failed to activate virtual environment: $ENV_PATH"
    pip install --upgrade pip || error_exit "Failed to upgrade pip"
    pip install mpiPython mpi4py || error_exit "Failed to install Python dependencies"
    deactivate
}

setup_virtualenv "$SERVER_VENV"
setup_virtualenv "$EDGE_VENV"

# --------------------------------------------------------------------------------
# 5. Compile Java application and create JAR
# --------------------------------------------------------------------------------
log "Compiling Java application..."
OUT_DIR="$ROOT_DIR/out"
mkdir -p "$OUT_DIR"

# Find and compile .java files under backend/comfaas
javac -d "$OUT_DIR" $(find "$ROOT_DIR/backend/comfaas" -name "*.java") \
    || error_exit "Failed to compile Java files"

# Create the JAR file
jar cmf "$CONFIGS_DIR/manifest.txt" "$ROOT_DIR/ComFaaS.jar" -C "$OUT_DIR" . \
    || error_exit "Failed to create JAR file"

# Clean up the out directory if you wish (comment out if you want to keep .class files)
rm -rf "$OUT_DIR"

# --------------------------------------------------------------------------------
# 6. Initialize server and edge
# --------------------------------------------------------------------------------
log "Initializing server with ComFaaS..."
java -jar "$ROOT_DIR/ComFaaS.jar" server init || error_exit "Failed to initialize server"

log "Initializing edge with ComFaaS..."
java -jar "$ROOT_DIR/ComFaaS.jar" edge init || error_exit "Failed to initialize edge"

# --------------------------------------------------------------------------------
# Finished
# --------------------------------------------------------------------------------
log "Setup and initialization completed successfully."

# Print out where the virtual environments were created
echo "[INFO] Server VENV: $SERVER_VENV"
echo "[INFO] Edge VENV:   $EDGE_VENV"
