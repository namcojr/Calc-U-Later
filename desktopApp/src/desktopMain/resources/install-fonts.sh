#!/bin/bash
# Calc-U-Later Font Installation Script
# Run this script after installing the DEB package to install LCD fonts system-wide

set -e

FONT_DIR="/usr/share/fonts/calc-u-later"
APP_LIB_DIR="/opt/calc-u-later/lib"
USER_FONT_DIR="$HOME/.calc_u_later/fonts"

echo "Installing Calc-U-Later LCD fonts..."

# Check if app is installed
if [ ! -d "$APP_LIB_DIR" ]; then
    echo "Error: Calc-U-Later not found in /opt/calc-u-later"
    echo "Please install the DEB package first."
    exit 1
fi

# Create system font directory (requires sudo)
if [ "$EUID" -eq 0 ]; then
    mkdir -p "$FONT_DIR"
    # Try to find and copy fonts from the app bundle
    find "$APP_LIB_DIR" -name "led_*.ttf" -exec cp {} "$FONT_DIR"/ \; 2>/dev/null || true
    find "$APP_LIB_DIR" -name "DejaVuSans*.ttf" -exec cp {} "$FONT_DIR"/ \; 2>/dev/null || true
    chmod 644 "$FONT_DIR"/*.ttf 2>/dev/null || true
    # Rebuild font cache
    fc-cache -f "$FONT_DIR" 2>/dev/null || true
    echo "✓ System fonts installed to $FONT_DIR"
else
    echo "Note: System-wide font installation requires sudo. Skipping system directory setup."
fi

# Create user font directory (no sudo needed)
mkdir -p "$USER_FONT_DIR"
find "$APP_LIB_DIR" -name "*.ttf" -exec cp {} "$USER_FONT_DIR"/ \; 2>/dev/null || true
chmod 644 "$USER_FONT_DIR"/*.ttf 2>/dev/null || true

echo "✓ User fonts installed to $USER_FONT_DIR"
echo "✓ Fonts installation complete!"
echo ""
echo "If fonts are still not displaying:"
echo "1. Restart the Calc-U-Later application"
echo "2. Run 'fc-cache -f' to rebuild the font cache"
echo "3. Check that fonts exist in $FONT_DIR or $USER_FONT_DIR"
