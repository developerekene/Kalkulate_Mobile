package com.devekene.kalkulate

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devekene.kalkulate.ui.KalkulateApp
import com.devekene.kalkulate.ui.KalkulateViewModel
import com.devekene.kalkulate.ui.theme.KalkulateTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
    )
    setContent {
      val viewModel: KalkulateViewModel = viewModel()
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()

      KalkulateTheme {
        KalkulateApp(uiState = uiState, viewModel = viewModel)
      }
    }
  }
}
