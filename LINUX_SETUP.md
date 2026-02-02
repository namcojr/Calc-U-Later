# Calc-U-Later Linux Setup Guide

## After Installing the Updated .deb Package

The new version includes automatic font setup on first launch. Here's what to do:

### Step 1: Install the Package
```bash
sudo dpkg -i calculater_1.7.0-1_amd64.deb
# or
sudo apt install ./calculater_1.7.0-1_amd64.deb
```

### Step 2: Launch the Application
```bash
/opt/calculater/bin/CalcULater
# or search for "Calc-U-Later" in your application menu
```

**The app will automatically:**
1. Extract LCD fonts to `~/.calc_u_later/fonts/` on first run
2. Attempt to set up system-wide fonts (may prompt for sudo password)
3. Apply the fonts immediately

### Step 3: Verify Fonts Are Loaded
- The display should show the LCD dot-matrix font (digital clock style)
- The brushed aluminum background should be visible
- Numbers and buttons should use the proper fonts

### Troubleshooting

#### Fonts Still Not Showing After First Run?

1. **Force font extraction:**
   ```bash
   rm -rf ~/.calc_u_later/fonts/*
   /opt/calculater/bin/CalcULater  # Launch app again
   ```

2. **Manually install system fonts (optional):**
   ```bash
   /opt/calculater/lib/install-fonts.sh
   ```

3. **Check fonts were extracted:**
   ```bash
   ls -la ~/.calc_u_later/fonts/
   ```
   Should show:
   - led_dot_matrix.ttf
   - led_italic.ttf  
   - DejaVuSans.ttf
   - DejaVuSans-Bold.ttf

4. **Restart the application:**
   Close and reopen Calc-U-Later

#### Background Not Appearing?
The brushed aluminum background is part of the theme and should appear automatically. If it doesn't:
- Ensure you're running this latest version
- Try restarting the application

### Font Search Priority

The app looks for fonts in this order:
1. User directory: `~/.calc_u_later/fonts/` (auto-extracted on first run)
2. System directory: `/usr/share/fonts/calc-u-later/` (optional system-wide install)
3. Standard Linux fonts: `/usr/share/fonts/truetype/dejavu/` (fallback)

### For System Administrators

To pre-install fonts system-wide for all users:
```bash
sudo mkdir -p /usr/share/fonts/calc-u-later
sudo cp ~/.calc_u_later/fonts/*.ttf /usr/share/fonts/calc-u-later/
sudo fc-cache -f /usr/share/fonts/calc-u-later
```

Then all users will have the LCD fonts available.

---

**Version:** 1.7.0  
**Features:**
- LCD dot-matrix display font (led_dot_matrix.ttf)
- LCD italic font (led_italic.ttf)
- Brushed aluminum background
- Automatic font extraction and installation
- Cross-platform support (Android, Linux, macOS, Windows)
