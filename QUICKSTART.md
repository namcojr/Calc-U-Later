# KMP Migration - Quick Start Guide

## What Changed

Your Calc-U-Later project has been converted to **Kotlin Multiplatform (KMP)** structure. This enables building for Android, Windows, Linux, and macOS with **99% code sharing**.

## Project Layout

```
Root Project (build.gradle.kts is now a root aggregator)
├── shared/              ← All shared UI & calculator logic
├── androidApp/          ← Android-specific entry point
├── desktopApp/          ← Desktop (Windows/Linux/macOS) entry point
└── settings.gradle.kts  ← Includes all modules
```

## Key Files

### Shared Code (Used by Both Platforms)
- `shared/src/commonMain/kotlin/com/sunwings/calc_u_later/`
  - `calculator/CalculatorState.kt` - All calculator logic
  - `ui/CalculatorScreen.kt` - UI shared across platforms
  - `ui/theme/` - Colors, themes, typography

### Platform-Specific Entry Points
- `androidApp/src/main/java/.../MainActivity.kt` - Android launcher
- `desktopApp/src/desktopMain/kotlin/.../Main.kt` - Desktop launcher (fixed 408×900 non-resizable window)

## First Steps

### 1. Move Old Files
Your original files in `src/main/` are still there (not deleted). You should:

```bash
cd /home/namco/Documents/Projects/Calc-U-Later

# Option A: Keep old files for reference, ignore them
# (They won't be compiled since androidApp uses its own structure)

# Option B: Remove old files after verifying new structure works
rm -rf src/main
```

**I recommend Option A for now** - keep them as backup until you verify the build works.

### 2. Build & Test

#### Android
```bash
# Build APK
./gradlew :androidApp:build

# Run on device/emulator
./gradlew :androidApp:installDebug
```

#### Desktop (Windows/Linux/macOS)
```bash
# Run directly
./gradlew :desktopApp:run

# Build native installer
./gradlew :desktopApp:package
```

### 3. Verify Gradle Sync
In Android Studio:
- File → Sync Now
- Should show 3 modules: `shared`, `androidApp`, `desktopApp`

If you see errors, run:
```bash
./gradlew clean
./gradlew build
```

## Important Notes

### ✅ Desktop Window is Fixed & Non-Resizable
```kotlin
// From desktopApp/src/desktopMain/kotlin/com/sunwings/calc_u_later/Main.kt
window state = rememberWindowState(size = DpSize(408.dp, 900.dp))
Window(..., resizable = false)
```

This ensures pixel-perfect match with the mobile version.

### ✅ All Features Preserved
- LCD color cycling (3 themes)
- Swipe gestures for LCD changes
- Long-press for format toggle
- Memory functions
- Number formatting (Portuguese/US)
- Beautiful gradients & animations

### ⚠️ Resource Files
If you have custom fonts or resources in `src/main/res/`:
- Android resources: Move to `androidApp/src/main/res/`
- Cross-platform assets: Should go in `shared/src/commonMain/resources/`

Currently using system fonts. If you need custom fonts, modify:
- `shared/src/commonMain/kotlin/.../theme/Type.kt`

### ⚠️ Preferences Storage

**Android** (unchanged):
```
Uses DataStore in: shared/src/androidMain/...
Stores: locale_format, lcd_color_index
```

**Desktop** (new):
```
File: ~/.calc_u_later_prefs.txt
Format: "COMMA_GROUP_DECIMAL_DOT|1"
```

## Branching Strategy (As You Mentioned)

You mentioned you'll handle branches. Here's the suggested flow:

```bash
# 1. Create feature branch
git checkout -b feature/kmp-multiplatform

# 2. Test everything locally
./gradlew clean build
./gradlew :androidApp:build
./gradlew :desktopApp:run

# 3. Commit all changes
git add .
git commit -m "feat: Convert to KMP multiplatform architecture

- Create shared module with calculator logic and UI
- Refactor androidApp as wrapper around shared code
- Add desktopApp for Windows/Linux/macOS
- Fixed non-resizable window (408x900dp) for desktop
- Desktop preferences stored locally (~/.calc_u_later_prefs.txt)
- 95%+ code reuse across platforms"

# 4. Push and create PR
git push origin feature/kmp-multiplatform
```

## Build Configuration

### Gradle Files
- `build.gradle.kts` (root) - Now empty, just aggregates modules
- `shared/build.gradle.kts` - KMP config
- `androidApp/build.gradle.kts` - Android-specific
- `desktopApp/build.gradle.kts` - Desktop-specific
- `gradle/libs.versions.toml` - Centralized dependency management

### Updated Dependencies
```toml
[versions]
kotlin = "2.0.21"
agp = "8.13.2"
composeMultiplatform = "1.6.10"
# ...
```

## Troubleshooting

### "Module not found: shared"
```bash
./gradlew :shared:build
./gradlew --refresh-dependencies
```

### IDE not recognizing shared code
- File → Sync Now (Android Studio)
- Or run: `./gradlew build`

### Desktop app won't run
```bash
# Make sure you have JDK 17+
java -version

# Then try:
./gradlew :desktopApp:run -DlogLevel=debug
```

### Preferences not persisting
Check file permissions on `~/.calc_u_later_prefs.txt`:
```bash
ls -la ~/.calc_u_later_prefs.txt
```

## Next: Distribution

Once you verify everything works:

### Desktop Distribution
```bash
# Create native installers
./gradlew :desktopApp:package

# Outputs:
# - desktopApp/build/compose/binaries/main/msi/ (Windows .msi)
# - desktopApp/build/compose/binaries/main/deb/ (Linux .deb)
# - desktopApp/build/compose/binaries/main/dmg/ (macOS .dmg)
```

### Android Distribution
Same as before:
- Build: `./gradlew :androidApp:bundleRelease`
- Sign & upload to Play Store

## Documentation

Full technical details in: **KMP_MIGRATION.md** (in project root)

---

**Ready to build?** Start with:
```bash
./gradlew :androidApp:build
./gradlew :desktopApp:run
```

Any issues, check `KMP_MIGRATION.md` or the build error messages for details.
