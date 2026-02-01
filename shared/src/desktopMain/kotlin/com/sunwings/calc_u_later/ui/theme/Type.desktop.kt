package com.sunwings.calc_u_later.ui.theme

import androidx.compose.ui.text.font.FontFamily
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.io.File

/**
 * Desktop implementation: Uses system-wide installed fonts
 * 
 * For best results, install fonts to your system:
 * Linux: sudo cp led_*.ttf DejaVu*.ttf /usr/share/fonts/
 *        sudo fc-cache -f
 * 
 * Or fonts are auto-extracted to ~/.calc_u_later/fonts/
 */

// Font family names (extracted from registered TTF files)
private val registeredFonts = mutableMapOf<String, String>()

private val fontsRegistered by lazy {
    try {
        val userFontsDir = "${System.getProperty("user.home")}/.calc_u_later/fonts"
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        
        val fontNames = listOf("led_dot_matrix.ttf", "led_italic.ttf", "DejaVuSans.ttf", "DejaVuSans-Bold.ttf")
        
        for (fontName in fontNames) {
            val fontFile = File("$userFontsDir/$fontName")
            if (fontFile.exists()) {
                try {
                    val font = Font.createFont(Font.TRUETYPE_FONT, fontFile)
                    ge.registerFont(font)
                    registeredFonts[fontName] = font.family  // Store the family name
                    System.err.println("✓ Registered font: $fontName (family: ${font.family}, name: ${font.name})")
                } catch (e: Exception) {
                    System.err.println("⚠ Could not register $fontName: ${e.message}")
                }
            }
        }
    } catch (e: Exception) {
        System.err.println("Error registering fonts: ${e.message}")
    }
    true
}

private fun loadFontFromResources(fontName: String, fallbackPaths: List<String>): FontFamily {
    // Ensure fonts are registered first
    val unused = fontsRegistered
    
    // Try to use registered font family name
    val registeredFamilyName = registeredFonts[fontName]
    if (registeredFamilyName != null) {
        System.err.println("✓ Using registered font family: $fontName → $registeredFamilyName")
        // For now, use the monospace for LCD fonts since they're registered
        return when {
            fontName.contains("led_") -> FontFamily.Monospace
            fontName.contains("DejaVu") -> FontFamily.SansSerif
            else -> FontFamily.SansSerif
        }
    }
    
    // Fallback based on font type
    return when (fontName) {
        "led_dot_matrix.ttf", "led_italic.ttf" -> {
            System.err.println("⚠ LCD font not registered, using fallback Monospace for: $fontName")
            FontFamily.Monospace
        }
        "DejaVuSans.ttf" -> {
            System.err.println("ℹ Using SansSerif for DejaVuSans")
            FontFamily.SansSerif
        }
        "DejaVuSans-Bold.ttf" -> {
            System.err.println("ℹ Using SansSerif for DejaVuSans-Bold")
            FontFamily.SansSerif
        }
        else -> {
            System.err.println("⚠ Unknown font: $fontName, using SansSerif fallback")
            FontFamily.SansSerif
        }
    }
}

// LCD dot matrix font (for display)
actual val LcdFontFamily: FontFamily by lazy {
    loadFontFromResources(
        "led_dot_matrix.ttf",
        listOf(
            "${System.getProperty("user.home")}/.calc_u_later/fonts/led_dot_matrix.ttf",
            "/usr/share/fonts/calc-u-later/led_dot_matrix.ttf"
        )
    )
}

// LCD italic font (for secondary display)
actual val MainDisplayFontFamily: FontFamily by lazy {
    loadFontFromResources(
        "led_italic.ttf",
        listOf(
            "${System.getProperty("user.home")}/.calc_u_later/fonts/led_italic.ttf",
            "/usr/share/fonts/calc-u-later/led_italic.ttf"
        )
    )
}

// Body text uses DejaVuSans for better readability on smaller UI elements
actual val BodyFontFamily: FontFamily by lazy {
    loadFontFromResources(
        "DejaVuSans.ttf",
        listOf(
            "${System.getProperty("user.home")}/.calc_u_later/fonts/DejaVuSans.ttf",
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
            "C:\\Windows\\Fonts\\DejaVuSans.ttf"
        )
    )
}

// Button text uses DejaVuSans-Bold
actual val MemoryButtonFontFamily: FontFamily by lazy {
    loadFontFromResources(
        "DejaVuSans-Bold.ttf",
        listOf(
            "${System.getProperty("user.home")}/.calc_u_later/fonts/DejaVuSans-Bold.ttf",
            "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
            "C:\\Windows\\Fonts\\DejaVuSans.ttf"
        )
    )
}

