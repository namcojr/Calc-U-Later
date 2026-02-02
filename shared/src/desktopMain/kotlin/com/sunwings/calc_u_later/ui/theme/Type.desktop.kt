package com.sunwings.calc_u_later.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.platform.Font as DesktopFont
import java.awt.Font as AwtFont
import java.awt.GraphicsEnvironment
import java.io.File

/**
 * Desktop implementation: Uses fonts embedded in resources and system-installed fonts
 * 
 * Fonts are loaded from desktopApp/src/desktopMain/resources/fonts/
 * On first run, FontSetup extracts them to ~/.calc_u_later/fonts/
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
                    val font = AwtFont.createFont(AwtFont.TRUETYPE_FONT, fontFile)
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

private fun loadFontFromFile(fontPath: String): FontFamily? {
    return try {
        val file = File(fontPath)
        if (file.exists()) {
            // Register with AWT first (for system-wide availability)
            val awtFont = AwtFont.createFont(AwtFont.TRUETYPE_FONT, file)
            
            // Load for Compose Desktop using the File constructor
            FontFamily(DesktopFont(file, FontWeight.Normal, FontStyle.Normal))
        } else {
            null
        }
    } catch (e: Exception) {
        System.err.println("Could not load font from $fontPath: ${e.message}")
        null
    }
}

private fun loadFontFromResources(fontName: String, fallbackPaths: List<String>): FontFamily {
    // Ensure fonts are registered first
    val unused = fontsRegistered
    
    // Try to load font from file using Compose Desktop's Font API
    val userFontsDir = "${System.getProperty("user.home")}/.calc_u_later/fonts"
    val userFontPath = "$userFontsDir/$fontName"
    
    loadFontFromFile(userFontPath)?.let {
        System.err.println("✓ Loaded font from file: $fontName")
        return it
    }
    
    // Try system paths
    for (path in fallbackPaths) {
        loadFontFromFile(path)?.let {
            System.err.println("✓ Loaded font from system: $path")
            return it
        }
    }
    
    // Fallback to system fonts
    System.err.println("⚠ Using fallback font for: $fontName")
    return when (fontName) {
        "led_dot_matrix.ttf", "led_italic.ttf" -> FontFamily.Monospace
        else -> FontFamily.SansSerif
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

