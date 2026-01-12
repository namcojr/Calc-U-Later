# Calc-U-Later

A beautiful, realistic calculator app for Android, built with Jetpack Compose.

## Features

- Classic calculator layout and button grid
- Realistic LCD display with memory indicator (M)
- Memory functions: MC, MR, M+, M-
- Custom color palette and LED-style font
- Responsive button sizing and gradients
- Handles large/small numbers with scientific notation (e.g., `1.23 e12`)
- Displays "Overflow" for results above 1e100
- Error handling for invalid operations (e.g., division by zero)

## Screenshots

<!-- Add screenshots here if available -->

## Getting Started

### Prerequisites
- Android Studio (latest recommended)
- Android SDK 24+

### Build & Run
1. Clone this repository:
   ```sh
   git clone <repo-url>
   cd Calc-U-Later
   ```
2. Open in Android Studio.
3. Click **Run** or use:
   ```sh
   ./gradlew assembleDebug
   ```

## Project Structure

- `src/main/java/com/sunwings/calc_u_later/`
  - `ui/` — Compose UI components, theming, and screen layout
  - `calculator/` — Calculator logic, state, and operations
- `res/` — Drawable assets, fonts, and XML resources
- `build.gradle.kts` — Project dependencies and configuration

## Customization
- Change colors and fonts in `ui/theme/`
- Adjust button layout or add features in `ui/CalculatorScreen.kt`
- Update calculation logic in `calculator/CalculatorState.kt`

## License

MIT License. See [LICENSE](LICENSE) for details.

---

Made with ❤️ using Jetpack Compose.
