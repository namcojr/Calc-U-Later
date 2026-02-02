# Calc-U-Later: Font Installation Guide

## After Installing the .deb Package

The Calc-U-Later calculator includes beautiful LCD fonts that need to be installed for proper display. Follow these steps:

### Option 1: Automatic Font Installation (Recommended)

1. Find and run the font installation script:
   ```bash
   /opt/calc-u-later/lib/install-fonts.sh
   ```
   
   OR download it from: `/opt/calc-u-later/lib/`

2. To install system-wide (requires sudo):
   ```bash
   sudo /opt/calc-u-later/lib/install-fonts.sh
   ```

3. Restart the Calc-U-Later application

### Option 2: Manual Font Installation

#### For System-Wide Installation (Linux):
```bash
sudo mkdir -p /usr/share/fonts/calc-u-later
sudo cp /opt/calc-u-later/lib/resources/fonts/*.ttf /usr/share/fonts/calc-u-later/
sudo fc-cache -f /usr/share/fonts/calc-u-later
```

#### For User-Only Installation (No sudo needed):
```bash
mkdir -p ~/.calc_u_later/fonts
cp /opt/calc-u-later/lib/resources/fonts/*.ttf ~/.calc_u_later/fonts/
```

### Option 3: Let the App Extract Fonts Automatically

The app can automatically extract fonts to `~/.calc_u_later/fonts/` on first run if they're not found elsewhere. Simply:
1. Launch the application
2. Wait a moment for fonts to be extracted
3. Restart the application

## Font Details

The calculator uses two specialty LCD fonts:

- **led_dot_matrix.ttf** - Main display font (digital clock-style)
- **led_italic.ttf** - Secondary display font (stylized LCD)

Plus standard fonts:
- **DejaVuSans.ttf** - UI elements
- **DejaVuSans-Bold.ttf** - Button labels

## Troubleshooting

### Fonts Still Not Showing?

1. **Verify font files exist:**
   ```bash
   ls -l ~/.calc_u_later/fonts/
   ls -l /usr/share/fonts/calc-u-later/  # If system-wide
   ```

2. **Rebuild font cache:**
   ```bash
   fc-cache -f ~/.calc_u_later/fonts/
   fc-cache -f /usr/share/fonts/calc-u-later/  # If system-wide
   ```

3. **Restart the application:**
   - Close and reopen Calc-U-Later

4. **Check application logs:**
   - The app logs font loading attempts to the console

### Background Not Appearing?

The brushed aluminum background should appear automatically. If it doesn't:
1. Ensure you're running the latest version from this build
2. Try clearing the application cache: `rm -rf ~/.calc_u_later/` (but this will remove settings)
3. Restart the application

## Font Search Paths (In Order)

The application looks for fonts in this order:

1. Application bundle resources (`/opt/calc-u-later/lib/resources/fonts/`)
2. User directory (`~/.calc_u_later/fonts/`)
3. System fonts (`/usr/share/fonts/calc-u-later/`)
4. Standard system directories (`/usr/share/fonts/truetype/dejavu/`)
5. Fallback to system sans-serif

## For Developers

To rebuild the DEB package with fonts included:
```bash
./gradlew :desktopApp:packageDeb
```

The resulting package at `desktopApp/build/compose/binaries/main/deb/` will include all fonts in the resources bundle.
