#!/usr/bin/env bash
# Packages the release Linux build into:
#   1. A portable tarball  (works anywhere, no dependencies)
#   2. An AppImage         (single-file universal Linux app)
#
# Usage: ./linux/packaging/package_linux.sh
# Run from the `desktop/` project root (or anywhere; paths are resolved).

set -euo pipefail

# --- Resolve paths ------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"
cd "${PROJECT_DIR}"

APP_NAME="calc_u_later"
PRETTY_NAME="Calc-U-Later"
VERSION="$(grep '^version:' pubspec.yaml | head -1 | sed 's/version:[[:space:]]*//' | cut -d'+' -f1)"
ARCH="x86_64"

BUNDLE_DIR="build/linux/x64/release/bundle"
DIST_DIR="dist"
ICON_PNG="assets/icon/app_icon.png"
DESKTOP_FILE="linux/packaging/calc-u-later.desktop"
APPRUN="linux/packaging/AppRun"

FLUTTER_BIN="${FLUTTER_BIN:-flutter}"

# --- Build --------------------------------------------------------------
echo ">> Building Linux release ..."
"${FLUTTER_BIN}" build linux --release

mkdir -p "${DIST_DIR}"

# --- 1. Tarball ---------------------------------------------------------
echo ">> Creating tarball ..."
TAR_STAGE="$(mktemp -d)"
STAGE_ROOT="${TAR_STAGE}/${PRETTY_NAME}-${VERSION}"
mkdir -p "${STAGE_ROOT}"
cp -r "${BUNDLE_DIR}/." "${STAGE_ROOT}/"
cp "${ICON_PNG}" "${STAGE_ROOT}/calc-u-later.png"
cp "${DESKTOP_FILE}" "${STAGE_ROOT}/calc-u-later.desktop"
cat > "${STAGE_ROOT}/install.sh" <<'INSTALL'
#!/usr/bin/env bash
# Installs Calc-U-Later for the current user (no root required).
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PREFIX="${HOME}/.local"
APPDIR="${PREFIX}/lib/calc-u-later"
mkdir -p "${APPDIR}" "${PREFIX}/bin" "${PREFIX}/share/applications" \
         "${PREFIX}/share/icons/hicolor/512x512/apps"
cp -r "${HERE}/." "${APPDIR}/"
ln -sf "${APPDIR}/calc_u_later" "${PREFIX}/bin/calc_u_later"
cp "${HERE}/calc-u-later.png" "${PREFIX}/share/icons/hicolor/512x512/apps/calc-u-later.png"
sed "s|^Exec=.*|Exec=${PREFIX}/bin/calc_u_later|" "${HERE}/calc-u-later.desktop" \
    > "${PREFIX}/share/applications/calc-u-later.desktop"
echo "Installed. Launch from your app menu or run: calc_u_later"
echo "(Ensure ${PREFIX}/bin is on your PATH.)"
INSTALL
chmod +x "${STAGE_ROOT}/install.sh"

TARBALL="${DIST_DIR}/${PRETTY_NAME}-${VERSION}-linux-${ARCH}.tar.gz"
tar -czf "${TARBALL}" -C "${TAR_STAGE}" "${PRETTY_NAME}-${VERSION}"
rm -rf "${TAR_STAGE}"
echo "   -> ${TARBALL}"

# --- 2. AppImage --------------------------------------------------------
echo ">> Creating AppImage ..."
TOOL_DIR="${DIST_DIR}/.tools"
mkdir -p "${TOOL_DIR}"
APPIMAGETOOL="${TOOL_DIR}/appimagetool-${ARCH}.AppImage"
if [ ! -x "${APPIMAGETOOL}" ]; then
  echo "   downloading appimagetool ..."
  curl -fsSL -o "${APPIMAGETOOL}" \
    "https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-${ARCH}.AppImage"
  chmod +x "${APPIMAGETOOL}"
fi

APPDIR="$(mktemp -d)/${PRETTY_NAME}.AppDir"
mkdir -p "${APPDIR}/usr/bin" "${APPDIR}/usr/lib" \
         "${APPDIR}/usr/share/applications" \
         "${APPDIR}/usr/share/icons/hicolor/512x512/apps"

cp -r "${BUNDLE_DIR}/"* "${APPDIR}/usr/bin/"
# Move bundled libs into usr/lib so AppRun finds them.
if [ -d "${APPDIR}/usr/bin/lib" ]; then
  cp -r "${APPDIR}/usr/bin/lib/." "${APPDIR}/usr/lib/"
fi

cp "${APPRUN}" "${APPDIR}/AppRun"
chmod +x "${APPDIR}/AppRun"
cp "${DESKTOP_FILE}" "${APPDIR}/calc-u-later.desktop"
cp "${DESKTOP_FILE}" "${APPDIR}/usr/share/applications/calc-u-later.desktop"
cp "${ICON_PNG}" "${APPDIR}/calc-u-later.png"
cp "${ICON_PNG}" "${APPDIR}/usr/share/icons/hicolor/512x512/apps/calc-u-later.png"

OUT_APPIMAGE="${DIST_DIR}/${PRETTY_NAME}-${VERSION}-${ARCH}.AppImage"
# FUSE is often unavailable in CI/containers; extract-and-run avoids it.
ARCH="${ARCH}" "${APPIMAGETOOL}" --appimage-extract-and-run "${APPDIR}" "${OUT_APPIMAGE}" \
  || ARCH="${ARCH}" "${APPIMAGETOOL}" "${APPDIR}" "${OUT_APPIMAGE}"
rm -rf "$(dirname "${APPDIR}")"
echo "   -> ${OUT_APPIMAGE}"

echo ""
echo "Done. Artifacts in ${DIST_DIR}/:"
ls -la "${DIST_DIR}" | grep -vE '\.tools' || true
