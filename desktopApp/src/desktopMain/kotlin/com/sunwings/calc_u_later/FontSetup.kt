package com.sunwings.calc_u_later

import java.io.File
import kotlin.system.exitProcess

/**
 * Utility to set up fonts on first run
 * Extracts LCD fonts from resources to user/system directories
 */
object FontSetup {
    fun ensureFontsAvailable() {
        val userFontsDir = File(System.getProperty("user.home"), ".calc_u_later/fonts")
        
        // If fonts already exist, skip setup
        if (fontFilesExist(userFontsDir)) {
            return
        }
        
        // Create user fonts directory
        userFontsDir.mkdirs()
        
        // List of font files to extract
        val fontFiles = listOf(
            "led_dot_matrix.ttf",
            "led_italic.ttf",
            "DejaVuSans.ttf",
            "DejaVuSans-Bold.ttf"
        )
        
        // Try to extract fonts from resources
        var successCount = 0
        for (fontFile in fontFiles) {
            if (extractFontFile(fontFile, userFontsDir)) {
                successCount++
            }
        }
        
        // Try to install system-wide fonts if we have sudo access
        if (successCount > 0) {
            tryInstallSystemFonts(userFontsDir)
        }
    }
    
    private fun fontFilesExist(dir: File): Boolean {
        return dir.exists() && 
               File(dir, "led_dot_matrix.ttf").exists() &&
               File(dir, "led_italic.ttf").exists()
    }
    
    private fun extractFontFile(filename: String, targetDir: File): Boolean {
        return try {
            val resourcePath = "/fonts/$filename"
            val resourceUrl = object {}.javaClass.getResource(resourcePath)
            
            if (resourceUrl != null) {
                val inputStream = object {}.javaClass.getResourceAsStream(resourcePath)
                if (inputStream != null) {
                    val targetFile = File(targetDir, filename)
                    inputStream.use { input ->
                        targetFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    targetFile.setReadable(true, false)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun tryInstallSystemFonts(sourceDir: File) {
        try {
            // Only try on Linux with fontconfig available
            val os = System.getProperty("os.name").toLowerCase()
            if (!os.contains("linux")) {
                return
            }
            
            // Check if we can write to system fonts directory
            val systemFontsDir = File("/usr/share/fonts/calc-u-later")
            if (systemFontsDir.exists() || canCreateSystemDir()) {
                // Try to copy fonts system-wide (may require sudo)
                Runtime.getRuntime().exec(arrayOf(
                    "sudo", "cp", "${sourceDir.absolutePath}/*.ttf", "/usr/share/fonts/calc-u-later/"
                )).waitFor()
                
                // Rebuild font cache
                Runtime.getRuntime().exec(arrayOf(
                    "fc-cache", "-f", "/usr/share/fonts/calc-u-later"
                )).waitFor()
            }
        } catch (e: Exception) {
            // Silently fail - user fonts are enough
        }
    }
    
    private fun canCreateSystemDir(): Boolean {
        return try {
            val testFile = File("/usr/share/fonts/.calc-u-later-test")
            testFile.createNewFile() && testFile.delete()
        } catch (e: Exception) {
            false
        }
    }
}
