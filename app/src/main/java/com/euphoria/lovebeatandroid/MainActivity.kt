package com.euphoria.lovebeatandroid

import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.MaterialTheme
import com.euphoria.lovebeatandroid.screens.MainScreen
import com.euphoria.lovebeatandroid.services.StorageService
import com.euphoria.lovebeatandroid.services.VibrationService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp(applicationContext)
        }
    }
}

@Composable
fun WearApp(context: Context) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            val storageService = StorageService(context = context)
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val vibrationService = VibrationService(vibrator)
            MainScreen(storageService = storageService, vibrationService = vibrationService)
        }
    }
}