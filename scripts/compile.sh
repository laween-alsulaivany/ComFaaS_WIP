# compile.sh

# javac -d out comfaas/*.java
# jar cmf manifest.txt ComFaaS.jar -C out/ .
#!/usr/bin/env bash
#
# compile.sh - Compile Java source and generate the ComFaaS JAR.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/../backend/configs/env.sh"

if [ -f "$ENV_FILE" ]; then
    source "$ENV_FILE"
else
    echo "[ERROR] env.sh not found at $ENV_FILE. Exiting..."
    exit 1
fi

echo "[INFO] Using ROOT_DIR: $ROOT_DIR"

# Create output directory if it doesn't exist
OUT_DIR="$ROOT_DIR/out"
mkdir -p "$OUT_DIR"

# Compile Java files
echo "[INFO] Compiling Java files..."
javac -d "$OUT_DIR" $(find "$ROOT_DIR/backend/comfaas" -name "*.java") \
    || { echo "[ERROR] Compilation failed."; exit 1; }

# Create JAR file
echo "[INFO] Creating JAR file: ComFaaS.jar"
jar cmf "$CONFIGS_DIR/manifest.txt" "$ROOT_DIR/ComFaaS.jar" -C "$OUT_DIR" . \
    || { echo "[ERROR] Failed to create JAR file."; exit 1; }

echo "[SUCCESS] Compilation and JAR creation completed."
