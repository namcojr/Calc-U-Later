package com.sunwings.calc_u_later

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sunwings.calc_u_later.ui.CalculatorScreen
import com.sunwings.calc_u_later.ui.theme.CalcULaterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalcULaterTheme {
                CalculatorScreen()
            }
        }
    }
}