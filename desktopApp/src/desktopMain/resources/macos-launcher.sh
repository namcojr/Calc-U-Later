#!/bin/bash

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
APP_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
RESOURCES_DIR="$APP_DIR/Resources"

# Set environment variables to ensure proper icon display
export JAVA_TOOL_OPTIONS="-Dapple.awt.fileDialogForDirectories=true -Dcom.apple.smallTabs=true"

# Execute the actual Java application
exec "$SCRIPT_DIR/CalcULater" "$@"
