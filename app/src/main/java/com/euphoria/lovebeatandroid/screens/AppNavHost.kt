package com.euphoria.lovebeatandroid.screens

import android.content.Context
import android.os.Vibrator
import androidx.compose.runtime.Composable
import androidx.datastore.core.Storage
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.euphoria.lovebeatandroid.navigation.NavigationItem
import com.euphoria.lovebeatandroid.services.StorageService
import com.euphoria.lovebeatandroid.services.VibrationService
import com.euphoria.lovebeatandroid.services.WebPairingService

@Composable
fun AppNavHost(
    navHostController: NavHostController,
    startDestination: String = NavigationItem.Main.route,
    context: Context
) {
    val storageService = StorageService(context = context)
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val vibrationService = VibrationService(vibrator)
    val webPairingService = WebPairingService(context)
    NavHost(navController = navHostController, startDestination = startDestination) {
        composable(NavigationItem.Main.route) {
            MainScreen(
                storageService = storageService,
                navHostController = navHostController
            )
        }
        composable(NavigationItem.Pairing.route) {
            PairingScreen(
                navHostController = navHostController
            )
        }
        composable(
            NavigationItem.Vibration.route
        ) {
            VibrationScreen(
                vibrationService = vibrationService,
                storageService = storageService,
                navController = navHostController
            )
        }
        composable(NavigationItem.SenderScreen.route) {
            SenderScreen(
                navController = navHostController,
                webPairingService = webPairingService,
                storageService = storageService
            )
        }
        composable(NavigationItem.ReceiverScreen.route) {
            ReceiverScreen(
                navController = navHostController,
                webPairingService = webPairingService,
                storageService = storageService
            )
        }
        composable(NavigationItem.Success.route) {
            SuccessScreen(navController = navHostController)
        }

        composable(NavigationItem.UnPair.route) {
            UnpairScreen(
                navController = navHostController,
                storageService = storageService
            )
        }
    }
}