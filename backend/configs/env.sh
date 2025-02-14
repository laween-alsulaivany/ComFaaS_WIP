#!/usr/bin/env bash
#
# env.sh - Environment variable definitions for ComFaaS.
#          Should be sourced by other scripts.

# Resolve the path to the directory containing this file (i.e., backend/configs/)
ENV_SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# The project root is two levels up from backend/configs/
export ROOT_DIR="$(cd "$ENV_SCRIPT_DIR/../.." && pwd)"

# Define Virtual Environment Paths (created at the root level)
export SERVER_VENV="$ROOT_DIR/.serverVenv"
export CLIENT_VENV="$ROOT_DIR/.clientVenv"

# Define Server and Client parent directories (created under root)
export SERVER_DIR="$ROOT_DIR/server"
export CLIENT_DIR="$ROOT_DIR/client"

# Optionally, define configs directory if needed
export CONFIGS_DIR="$ROOT_DIR/backend/configs"

# You can export additional environment variables or paths here if needed.

