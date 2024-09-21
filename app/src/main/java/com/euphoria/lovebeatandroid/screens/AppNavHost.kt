package com.euphoria.lovebeatandroid.screens

import android.app.Activity
import android.content.Context
import android.os.Vibrator
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.euphoria.lovebeatandroid.models.User
import com.euphoria.lovebeatandroid.navigation.NavigationItem
import com.euphoria.lovebeatandroid.services.NearbyConnectionsService
import com.euphoria.lovebeatandroid.services.NfcService
import com.euphoria.lovebeatandroid.services.StorageService
import com.euphoria.lovebeatandroid.services.VibrationService
import com.euphoria.lovebeatandroid.services.WifiDirectService

@Composable
fun AppNavHost(
    navHostController: NavHostController,
    startDestination: String = NavigationItem.Main.route,
    context: Context
) {
    val storageService = StorageService(context = context)
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val vibrationService = VibrationService(vibrator)
    val nearbyConnectionsService = NearbyConnectionsService(context)
    val wifiDirectService = WifiDirectService(context)
    val nfcService = NfcService(activity = context as Activity)
    val user = User()
    NavHost(navController = navHostController, startDestination = startDestination) {
        composable(NavigationItem.Main.route) {
            MainScreen(
                storageService = storageService,
                vibrationService = vibrationService,
                nearbyConnectionsService = nearbyConnectionsService,
                navHostController = navHostController
            )
        }
        composable(NavigationItem.Pairing.route) {
            PairingScreen(
                navHostController = navHostController,
                wifiDirectService = wifiDirectService
            )
        }
        composable(
            NavigationItem.Vibration.route
        ) {
//            println(" @ vibration screen route User: $user")
            VibrationScreen(
                myUuid = "cea36efa-e594-43bf-9c17-46fef950d3c2",
                partnerUuid = "2c7f3281-e76e-406c-a7d8-7b1e58300671",
                vibrationService = vibrationService
            )
        }
        composable(NavigationItem.SenderScreen.route) {
            SenderScreen(navController = navHostController, wifiDirectService = wifiDirectService, nfcService = nfcService)
        }
        composable(NavigationItem.ReceiverScreen.route) {
            ReceiverScreen(navController = navHostController, wifiDirectService = wifiDirectService, nfcService = nfcService)
        }
        composable(NavigationItem.Consent.route) { backStackEntry ->
            ConsentScreen(
                wifiDirectService = wifiDirectService,
                navController = navHostController,
                deviceAddress = backStackEntry.arguments?.getString("deviceAddress") ?: ""
            )
        }
        composable("success/{partnerUuid}") { backStackEntry ->
            SuccessScreen(
                partnerUuid = backStackEntry.arguments?.getString("partnerUuid") ?: "",
                navController = navHostController
            )
        }

    }
}