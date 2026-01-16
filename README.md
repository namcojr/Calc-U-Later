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
 - Locale-aware number formatting and parsing
    - Supports two display styles: grouping `.` + decimal `,` (Portuguese style), and grouping `,` + decimal `.` (English/US style)
    - Long-press the LCD display to toggle the number format
    - The selected format is persisted across app restarts

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

## Input Validation

- The app enforces a per-number digit limit: users cannot enter more than **10 digits** in a single number segment (signs and the decimal separator are allowed and do not count toward the 10-digit limit). This ensures predictable formatting and prevents excessively long inputs.

   Examples:
   - Allowed: `-12345,12345`, `1,123456789`, `-300`, `+8987656374` (signs and decimal are OK when total digits <= 10)
   - Disallowed: `12345678901`, `12345,123456798`, `-8756475902,22` (these have more than 10 digits in one number segment)

- The validation lives in `calculator/CalculatorState.kt` and is applied centrally so both UI and programmatic inputs are restricted consistently.

## Locale / Formatting Behavior

- Number formatting is centralized in `calculator/CalculatorState.kt` with `NumberFormatStyle`.
- Parsing and evaluation are locale-aware: displayed numbers (with grouping) are parsed back to their correct numeric value for computation.
- When toggling locale format (long-press on the display), the current calculation result and pending operation are cleared to avoid mismatches between visual formatting and internal numeric representation.

## Tests

- The project includes unit tests covering operations, edge cases, digit limits, display fit, and locale-specific behavior. See `src/test/java/com/sunwings/calc_u_later/calculator`.
- New tests cover:
   - `ToggleLocaleFormat` resets display and pending state.
   - `Clear` preserves the chosen `localeFormat`.
   - Formatting correctness for grouping and decimal separators during operations.

## How it works

- Long-press the LCD display to toggle the number-format style between:
   - Grouping `.` with decimal `,` (e.g. `1.234,56`) and
   - Grouping `,` with decimal `.` (e.g. `1,234.56`).
- The toggle clears the current result and pending operation to avoid visual vs internal numeric mismatches.
- The chosen preference is saved persistently using Jetpack DataStore so it remains after app restarts.

Screenshot (example):

![Long-press Toggle](docs/long-press-screenshot.png)


## Tests

- Unit tests for the input digit-limit logic are included under `src/test/java/com/sunwings/calc_u_later/calculator/CalculatorDigitLimitTest.kt`.


## License

MIT License. See [LICENSE](LICENSE) for details.

---

Made with ❤️ using Jetpack Compose.
