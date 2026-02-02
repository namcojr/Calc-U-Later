# Calc-U-Later Desktop & Mobile Packaging Guide

## Overview

Calc-U-Later is built as a **Kotlin Multiplatform Mobile (KMM)** application with independent packages for:
- ✅ **Linux** (DEB package)
- ✅ **Universal JAR** (Windows, macOS, Linux)
- ✅ **Android** (APK)
- 📝 **Windows** (EXE) - Requires Windows to build
- 📝 **macOS** (DMG) - Requires macOS to build

All desktop distributions **include fonts and icons** directly within the package.

---

## Available Packages

### 1. Linux (.deb Package)
**File:** `desktopApp/build/compose/binaries/main/deb/calculater_1.7.0-1_amd64.deb`
**Size:** 49 MB
**Installation:**
```bash
sudo apt install ./calculater_1.7.0-1_amd64.deb
```
**What's Included:**
- Calculator application (standalone, no runtime dependencies)
- Application icon for desktop integration
- DejaVuSans fonts (regular & bold) bundled
- Launch shortcut in Applications menu

### 2. Universal Java Package (.jar)
**File:** `desktopApp/build/compose/jars/CalcULater-linux-x64-1.7.0.jar`
**Size:** 36 MB
**Requirements:** Java 11 or higher
**Installation:**
```bash
# Linux/macOS
java -jar CalcULater-linux-x64-1.7.0.jar

# Windows (if Java is installed)
java -jar CalcULater-linux-x64-1.7.0.jar
```
**What's Included:**
- Full application with all dependencies
- Fonts embedded
- Icons included
- Platform-independent (runs on Windows, macOS, Linux)

### 3. Android (.apk)
**File:** `androidApp/build/outputs/apk/debug/androidApp-debug.apk`
**Size:** 28 MB
**Installation:**
```bash
# Using ADB (Android Debug Bridge)
adb install androidApp-debug.apk

# Or drag & drop to Android device
# Or email to device and install
```
**What's Included:**
- Full calculator app optimized for mobile
- Material Design 3 UI
- DataStore for preferences persistence
- 3 LCD display themes

---

## Build Instructions by Platform

### Prerequisites (All Platforms)
```bash
git clone <repository>
cd Calc-U-Later

# Install Java 17 or higher
java -version

# Gradle is included in the project (./gradlew)
```

### Linux/macOS (.deb)
```bash
# Build Linux DEB package
./gradlew :desktopApp:packageDeb

# Output: desktopApp/build/compose/binaries/main/deb/*.deb
```

### Windows (.exe)
**Must be built on Windows with WiX Toolset installed:**

1. Install [WiX Toolset v4+](https://wixtoolset.org/)
2. Build the package:
```bash
./gradlew :desktopApp:packageExe

# Output: desktopApp\build\compose\binaries\main\exe\*.exe
```

### macOS (.dmg)
**Must be built on macOS:**

1. Ensure Xcode Command Line Tools are installed:
```bash
xcode-select --install
```

2. Build the package:
```bash
./gradlew :desktopApp:packageDmg

# Output: desktopApp/build/compose/binaries/main/dmg/*.dmg
```

### Universal JAR (All Platforms)
```bash
# Build runnable JAR for your current OS
./gradlew :desktopApp:packageUberJarForCurrentOS

# Output: desktopApp/build/compose/jars/CalcULater-*.jar
```

### Android
```bash
# Build debug APK
./gradlew :androidApp:assembleDebug

# Output: androidApp/build/outputs/apk/debug/androidApp-debug.apk

# For release APK (requires signing setup)
./gradlew :androidApp:assembleRelease
```

---

## Package Contents

### Desktop Distribution Includes:
- ✅ Calculator executable (native or Java-based)
- ✅ Application icon (256×256 PNG)
- ✅ Bundled fonts (DejaVuSans, DejaVuSans-Bold)
- ✅ All required dependencies (standalone, no external downloads)
- ✅ Dark mode icon (orange/white themed)

### Android APK Includes:
- ✅ Full Material Design 3 interface
- ✅ Calculator with memory operations
- ✅ LCD display themes (Green, Red, Blue)
- ✅ DataStore persistence
- ✅ Touch-optimized button layout

---

## Architecture & Features

### Shared Code (95% reuse):
```
shared/src/commonMain/
├── CalculatorState.kt       # Core logic (pure Kotlin)
├── CalculatorScreen.kt      # UI (Compose Multiplatform)
└── theme/
    ├── Color.kt             # Color definitions
    ├── Theme.kt             # Material3 theming
    └── Type.kt              # Typography
```

### Desktop-Specific:
- Fixed 408×900 dp non-resizable window
- Local file preferences (`~/.calc_u_later_prefs.json`)
- System file menu integration

### Android-Specific:
- DataStore preferences
- Material Design 3 Dark/Light themes
- Touch-optimized 6×4 button grid

---

## Deployment Recommendations

### For Linux:
1. **Primary:** Use the .deb package for easy installation
   ```bash
   sudo apt install calculater_1.7.0-1_amd64.deb
   ```

2. **Alternative:** Universal JAR (requires Java installed)
   ```bash
   java -jar CalcULater-linux-x64-1.7.0.jar
   ```

### For macOS:
1. **Primary:** Build native .dmg on macOS (includes App Store integration)
   ```bash
   ./gradlew :desktopApp:packageDmg
   ```

2. **Alternative:** Universal JAR
   ```bash
   java -jar CalcULater-macos-x64-1.7.0.jar
   ```

3. **Alternative:** Homebrew (if packaged)
   ```bash
   brew install calc-u-later
   ```

### For Windows:
1. **Primary:** Build native .exe on Windows (best user experience)
   ```bash
   ./gradlew :desktopApp:packageExe
   ```

2. **Alternative:** Universal JAR
   ```bash
   java -jar CalcULater-windows-x64-1.7.0.jar
   ```

3. **Alternative:** Windows Store (if submitted)

### For Android:
1. **Development:** Install .apk via ADB or file transfer
   ```bash
   adb install androidApp-debug.apk
   ```

2. **Release:** Publish to Google Play Store or F-Droid
   ```bash
   ./gradlew :androidApp:bundleRelease
   ```

---

## Building Platform-Specific Packages

### Generate All Available Packages:

```bash
# For current platform
./gradlew :desktopApp:packageDistributionForCurrentOS

# For all available tasks
./gradlew :desktopApp:tasks | grep package

# Individual package commands
./gradlew :desktopApp:packageDeb      # Linux
./gradlew :desktopApp:packageExe      # Windows (on Windows)
./gradlew :desktopApp:packageDmg      # macOS (on macOS)
./gradlew :desktopApp:packageRpm      # Red Hat/Fedora
```

---

## Customization

### Modify Package Properties:
Edit `desktopApp/build.gradle.kts`:
```kotlin
packageName = "CalcULater"           // Name in system menus
packageVersion = "1.7.0"             // Version number
vendor = "Sunwings"                  // Publisher name
```

### Change Application Icon:
Replace `desktopApp/src/desktopMain/resources/icon.png` (256×256 PNG)

### Add More Fonts:
Place `.ttf` files in `desktopApp/src/desktopMain/resources/fonts/`

### Modify Application Size/Behavior:
Edit `desktopApp/src/desktopMain/kotlin/Main.kt`:
```kotlin
window(
    state = rememberWindowState(size = DpSize(408.dp, 900.dp)),
    resizable = false,  // Change to true for resizable window
    title = "Calc-U-Later"
)
```

---

## Size Optimization

Current sizes (v1.7.0):
- Linux .deb: 49 MB (includes all dependencies)
- Universal JAR: 36 MB (platform-independent)
- Android .apk: 28 MB (mobile optimized)

To reduce size:
1. Use R8/ProGuard for Android (release builds do this automatically)
2. Remove unused Compose dependencies from shared module
3. Minimize font files (currently includes 2 fonts @ 1.4 MB)

---

## Troubleshooting

### "java -jar" command not found
**Solution:** Install Java 11+
- Ubuntu: `sudo apt install openjdk-17-jre`
- macOS: `brew install openjdk`
- Windows: Download from [java.com](https://java.com)

### DEB package fails to install
**Solution:** Check dependencies
```bash
dpkg -l | grep libssl   # Check for required libs
sudo apt update && sudo apt install -f
```

### APK installation fails on Android
**Solution:** 
```bash
# Enable developer mode on Android device
# Verify ADB connection
adb devices

# Install APK
adb install -r androidApp-debug.apk  # -r to reinstall
```

### Application icon not showing
**Solution:** Verify icon paths in `build.gradle.kts`
```kotlin
iconFile.set(project.file("src/desktopMain/resources/icon.png"))
```

---

## Distribution Channels

### Linux
- [ ] Publish to Flathub
- [ ] Create PPA (Personal Package Archive)
- [ ] Submit to Snap Store
- [ ] List on AlternativeTo.net

### Windows
- [ ] Microsoft Store
- [ ] Chocolatey Package Manager
- [ ] Windows Package Manager (winget)
- [ ] Direct download website

### macOS
- [ ] Mac App Store
- [ ] Homebrew
- [ ] Direct download website

### Android
- [ ] Google Play Store
- [ ] F-Droid
- [ ] Amazon Appstore

---

## Release Checklist

- [ ] Bump version in `desktopApp/build.gradle.kts` and `androidApp/build.gradle.kts`
- [ ] Update CHANGELOG.md
- [ ] Build all packages for current platform
- [ ] Request builds for Windows (if available)
- [ ] Request builds for macOS (if available)
- [ ] Test each package on target platform
- [ ] Sign binaries (optional but recommended for distribution)
- [ ] Create GitHub release with packages attached
- [ ] Update website/documentation
- [ ] Post to distribution channels

---

**Last Updated:** 2026-02-01
**Version:** 1.7.0
**Supported Platforms:** Linux, Windows, macOS, Android
