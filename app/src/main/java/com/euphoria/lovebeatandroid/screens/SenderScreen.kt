package com.euphoria.lovebeatandroid.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.euphoria.lovebeatandroid.R
import com.euphoria.lovebeatandroid.navigation.NavigationItem
import com.euphoria.lovebeatandroid.services.NfcService
import com.euphoria.lovebeatandroid.services.WifiDirectService
@Composable
//@Preview(widthDp = 512, heightDp = 512, apiLevel = 33)
fun SenderScreen(
    navController: NavHostController, wifiDirectService: WifiDirectService, nfcService: NfcService
) {

//    val scope = rememberCoroutineScope()
//    var error by remember { mutableStateOf<String?>(null) }
//    var isAdvertising by remember { mutableStateOf(false) }


    var showAnimation by remember { mutableStateOf(false) }
    val nfcState by nfcService.nfcState.collectAsState()

    BackHandler {
        navController.popBackStack() // Navigates back to Screen 1
    }

    LaunchedEffect(Unit) {


//        scope.launch {
//            try {
//                isAdvertising = wifiDirectService.startAdvertising()
//            } catch (e: Exception) {
//                error = e.message
//            }
//        }
//        enabled = !isAdvertising

//        wifiDirectService.connectionState.collect { state ->
//            when (state) {
//                is WifiDirectService.ConnectionState.Connected -> {
//                    navController.navigate("consent/${state.device.deviceAddress}")
//                }
//
//                is WifiDirectService.ConnectionState.Failed -> {
//                    error = "Connection failed: ${state.reason}"
//                }
//
//                else -> {}
//            }
//        }
        nfcService.enableWriteMode()
    }

    DisposableEffect(Unit) {
        onDispose {
            nfcService.disableWriteMode()
        }
    }


    val pairLoaderLottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            R.raw.pairloader
        )
    )

    val preloaderProgress by animateLottieCompositionAsState(
        pairLoaderLottieComposition, iterations = LottieConstants.IterateForever, isPlaying = true
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .size(512.dp)
            .clip(shape = RoundedCornerShape(512.dp))
            .background(color = Color(0xff000212))
            .wrapContentWidth(align = Alignment.CenterHorizontally)
            .wrapContentHeight(align = Alignment.CenterVertically)
    ) {

        LottieAnimation(
            composition = pairLoaderLottieComposition, progress = preloaderProgress
        )

        when (nfcState) {
            is NfcService.NfcState.WriteSuccess -> {
                Text("Data written successfully!")
                showAnimation = true
            }

            is NfcService.NfcState.Error -> {
                Text(
                    "Error: ${(nfcState as NfcService.NfcState.Error).message}"
                )
            }

            else -> {
                LottieAnimation(
                    composition = pairLoaderLottieComposition, progress = preloaderProgress
                )
            }
        }

        if (showAnimation) {
//            SuccessAnimation()
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                navController.navigate("vibration")
            }
        }


    }
}