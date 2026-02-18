package com.sunwings.calc_u_later

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.sunwings.calc_u_later.calculator.NumberFormatStyle
import com.sunwings.calc_u_later.ui.CalculatorScreen
import com.sunwings.calc_u_later.ui.theme.CalcULaterTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

fun main() = application {
    val startNanos = System.nanoTime()
    fun logStartup(step: String) {
        val elapsedMs = (System.nanoTime() - startNanos) / 1_000_000
        println("startup:$step:${elapsedMs}ms")
    }

    // Font setup: run asynchronously after window creation to avoid blocking startup
    // (will extract user fonts; registration happens lazily in Type.desktop.kt)
    
    // Desktop preferences stored in local file
    val prefsFile = File(System.getProperty("user.home"), ".calc_u_later_prefs.txt")
    
    fun loadPrefs(): Pair<NumberFormatStyle, Int> {
        return if (prefsFile.exists()) {
            try {
                val content = prefsFile.readText().trim()
                val parts = content.split("|")
                val format = if (parts.size > 0 && parts[0] == "COMMA_GROUP_DECIMAL_DOT") 
                    NumberFormatStyle.COMMA_GROUP_DECIMAL_DOT 
                else 
                    NumberFormatStyle.DOT_GROUP_DECIMAL_COMMA
                val lcdIndex = if (parts.size > 1) parts[1].toIntOrNull() ?: 0 else 0
                format to lcdIndex
            } catch (e: Exception) {
                NumberFormatStyle.DOT_GROUP_DECIMAL_COMMA to 0
            }
        } else {
            NumberFormatStyle.DOT_GROUP_DECIMAL_COMMA to 0
        }
    }
    
    fun savePrefs(format: NumberFormatStyle, lcdIndex: Int) {
        try {
            prefsFile.writeText("${format.name}|$lcdIndex")
        } catch (e: Exception) {
            // Silent fail on save
        }
    }
    
    val (initialFormat, initialLcdIndex) = loadPrefs()
    logStartup("prefs-loaded")
    
    val windowState = rememberWindowState(
        size = DpSize(340.dp, 720.dp)  // Further reduced for better macOS display
    )
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "Calc-U-Later",
        state = windowState,
        resizable = false  // NON-RESIZABLE
    ) {
        LaunchedEffect(Unit) {
            logStartup("first-composition")
            // Kick off font extraction on IO dispatcher so the UI isn't blocked
            launch {
                withContext(Dispatchers.IO) {
                    try {
                        val fontStart = System.nanoTime()
                        FontSetup.ensureFontsAvailable()
                        val elapsed = (System.nanoTime() - fontStart) / 1_000_000
                        println("startup:fonts-ready:${elapsed}ms")
                    } catch (e: Exception) {
                        System.err.println("FontSetup failed: ${e.message}")
                    }
                }
            }
        }
        var currentFormat by remember { mutableStateOf(initialFormat) }
        var currentLcdIndex by remember { mutableStateOf(initialLcdIndex) }
        val scope = rememberCoroutineScope()
        
        CalcULaterTheme {
            CalculatorScreen(
                initialFormat = currentFormat,
                onFormatChange = { format ->
                    currentFormat = format
                    scope.launch {
                        savePrefs(format, currentLcdIndex)
                    }
                },
                initialLcdIndex = currentLcdIndex,
                onLcdIndexChange = { index ->
                    currentLcdIndex = index
                    scope.launch {
                        savePrefs(currentFormat, index)
                    }
                }
            )
        }
    }
}
