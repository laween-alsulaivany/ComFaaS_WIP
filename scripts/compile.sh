# # compile.sh

# # javac -d out comfaas/*.java
# # jar cmf manifest.txt ComFaaS.jar -C out/ .
# #!/usr/bin/env bash
# #
# # compile.sh - Compile Java source and generate the ComFaaS JAR.

# SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# ENV_FILE="$SCRIPT_DIR/../backend/configs/env.sh"

# if [ -f "$ENV_FILE" ]; then
#     source "$ENV_FILE"
# else
#     echo "[ERROR] env.sh not found at $ENV_FILE. Exiting..."
#     exit 1
# fi

# echo "[INFO] Using ROOT_DIR: $ROOT_DIR"

# # Create output directory if it doesn't exist
# OUT_DIR="$ROOT_DIR/out"
# mkdir -p "$OUT_DIR"

# # Compile Java files
# echo "[INFO] Compiling Java files..."
# javac -d "$OUT_DIR" $(find "$ROOT_DIR/backend/comfaas" -name "*.java") \
#     || { echo "[ERROR] Compilation failed."; exit 1; }

# # Create JAR file
# echo "[INFO] Creating JAR file: ComFaaS.jar"
# jar cmf "$CONFIGS_DIR/manifest.txt" "$ROOT_DIR/ComFaaS.jar" -C "$OUT_DIR" . \
#     || { echo "[ERROR] Failed to create JAR file."; exit 1; }

# echo "[SUCCESS] Compilation and JAR creation completed."



#!/usr/bin/env bash
#
# compile.sh - Compile Java source using Maven and generate ComFaaS.jar in the root directory.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
BACKEND_DIR="$ROOT_DIR/backend"

echo "[INFO] Compiling and packaging using Maven..."
cd "$BACKEND_DIR" || { echo "[ERROR] Backend directory not found."; exit 1; }

# Ensure `env.sh` is sourced
ENV_FILE="$BACKEND_DIR/configs/env.sh"
if [ -f "$ENV_FILE" ]; then
    source "$ENV_FILE"
    echo "[INFO] Environment variables loaded from env.sh."
else
    echo "[WARNING] env.sh not found. Proceeding without it."
fi

# Manually remove the target directory to prevent file lock issues
if [ -d "$BACKEND_DIR/target" ]; then
    echo "[INFO] Removing existing target directory..."
    rm -rf "$BACKEND_DIR/target" || { echo "[ERROR] Failed to delete target directory."; exit 1; }
fi

# Compile and package using Maven
mvn clean package || { echo "[ERROR] Maven build failed."; exit 1; }

# Move the JAR to the root directory and rename it
echo "[INFO] Moving JAR to root directory as ComFaaS.jar..."
mv "$BACKEND_DIR/target/backend-1.0-SNAPSHOT.jar" "$ROOT_DIR/ComFaaS.jar" \
    || { echo "[ERROR] Failed to move JAR file."; exit 1; }

echo "[SUCCESS] Compilation and JAR creation completed."
