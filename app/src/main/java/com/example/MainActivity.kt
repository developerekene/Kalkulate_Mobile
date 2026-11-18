package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.KalkulateApp
import com.example.ui.KalkulateViewModel
import com.example.ui.theme.KalkulateTheme
import com.example.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: KalkulateViewModel = viewModel()
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()
      val systemDark = isSystemInDarkTheme()
      val isDark =
        when (uiState.preferences.themeMode) {
          ThemeMode.DARK -> true
          ThemeMode.LIGHT -> false
          ThemeMode.SYSTEM -> systemDark
        }

      LaunchedEffect(isDark) {
        enableEdgeToEdge(
          statusBarStyle =
            if (isDark) SystemBarStyle.dark(Color.TRANSPARENT)
            else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
          navigationBarStyle =
            if (isDark) SystemBarStyle.dark(Color.TRANSPARENT)
            else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
      }

      KalkulateTheme(themeMode = uiState.preferences.themeMode) {
        KalkulateApp(uiState = uiState, viewModel = viewModel)
      }
    }
  }
}
