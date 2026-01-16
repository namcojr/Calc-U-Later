package com.sunwings.calc_u_later

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sunwings.calc_u_later.ui.CalculatorScreen
import com.sunwings.calc_u_later.ui.theme.CalcULaterTheme
import com.sunwings.calc_u_later.calculator.NumberFormatStyle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "calc_prefs")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalcULaterTheme {
                val ctx = LocalContext.current
                val ds = ctx.dataStore
                val prefsKey = stringPreferencesKey("locale_format")
                val scope = rememberCoroutineScope()

                val initialFormat by produceState(initialValue = NumberFormatStyle.DOT_GROUP_DECIMAL_COMMA, key1 = ds) {
                    val prefs = ds.data.first()
                    val saved = prefs[prefsKey]
                    value = when (saved) {
                        NumberFormatStyle.COMMA_GROUP_DECIMAL_DOT.name -> NumberFormatStyle.COMMA_GROUP_DECIMAL_DOT
                        else -> NumberFormatStyle.DOT_GROUP_DECIMAL_COMMA
                    }
                }

                CalculatorScreen(initialFormat = initialFormat) { newFormat ->
                    scope.launch {
                        ds.edit { settings ->
                            settings[prefsKey] = newFormat.name
                        }
                    }
                }
            }
        }
    }
}