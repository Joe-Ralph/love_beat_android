package com.euphoria.lovebeatandroid.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat.startForegroundService
import androidx.navigation.NavHostController
import com.euphoria.lovebeatandroid.R
import com.euphoria.lovebeatandroid.models.User
import com.euphoria.lovebeatandroid.navigation.NavigationItem
import com.euphoria.lovebeatandroid.services.NearbyConnectionsService
import com.euphoria.lovebeatandroid.services.PollingService
import com.euphoria.lovebeatandroid.services.StorageService
import com.euphoria.lovebeatandroid.services.VibrationService


val great_vibes_font = FontFamily(
    Font(resId = R.font.greatvibes, weight = FontWeight.Normal, style = FontStyle.Normal),
)


@Composable
fun MainScreen(
    storageService: StorageService,
    vibrationService: VibrationService,
    nearbyConnectionsService: NearbyConnectionsService,
    navHostController: NavHostController
) {
    val context = LocalContext.current
//    val user by remember { mutableStateOf(User()) }
    val user = User()
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // For testing purposes, save sample UUIDs
//        storageService.savePartnerUuid("2c7f3281-e76e-406c-a7d8-7b1e58300671")
//        storageService.saveMyUuid("cea36efa-e594-43bf-9c17-46fef950d3c2")
        // ======================================
//        user.uuid = storageService.getMyUuid()
//        user.partnerUuid = storageService.getPartnerUuid() ?: ""
//        user.uuid = "cea36efa-e594-43bf-9c17-46fef950d3c2"
//        user.partnerUuid = "2c7f3281-e76e-406c-a7d8-7b1e58300671"
//        isLoading = false

        // Start the PollingService once the user ID is available
        if (user.uuid.isNotEmpty()) {
            println("Done loading user data: $user")
            val intent = Intent(context, PollingService::class.java).apply {
                putExtra("USER_ID", user.uuid)
            }
            startForegroundService(context, intent)
        }
    }

    // Register BroadcastReceiver
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
            }
        }
        val filter = IntentFilter("com.euphoria.lovebeatandroid.VIBRATION_RECEIVED")
        context.registerReceiver(receiver, filter)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    if (isLoading) {
        LoadingScreen()
    } else if (user.partnerUuid.isEmpty()) {
        println("nav to pairing $user")
        navHostController.navigate(NavigationItem.Pairing.route)
    } else {
//        println("nav to vibration $user")
        navHostController.navigate(NavigationItem.Vibration.route)
    }
}

@Composable
fun LoadingScreen() {
    // Implement loading screen UI
}
