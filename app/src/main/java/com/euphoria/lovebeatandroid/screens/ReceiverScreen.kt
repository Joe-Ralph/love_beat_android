package com.euphoria.lovebeatandroid.screens

import android.net.wifi.p2p.WifiP2pDevice
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.euphoria.lovebeatandroid.services.WifiDirectService
import kotlinx.coroutines.launch

@Composable
fun ReceiverScreen(navController: NavHostController, wifiDirectService: WifiDirectService) {
    BackHandler {
        navController.popBackStack() // Navigates back to Screen 1
    }

    var peers by remember { mutableStateOf(listOf<WifiP2pDevice>()) }
    var isScanning by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                error = null
                isScanning = true
                wifiDirectService.discoverPeers().collect { newPeers ->
                    peers = newPeers
                }
            } catch (e: Exception) {
                error = "Failed to discover peers: ${e.message}"
            } finally {
                isScanning = false
            }

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

    val list = listOf(
        "A", "B", "C", "D"
    ) + ((0..100).map { it.toString() })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .size(512.dp)
            .clip(shape = RoundedCornerShape(512.dp))
            .background(color = Color(0xff000212))
            .wrapContentWidth(align = Alignment.CenterHorizontally)
            .wrapContentHeight(align = Alignment.CenterVertically)
    ) {
        if (isScanning) {
            LottieAnimation(
                composition = pairLoaderLottieComposition, progress = preloaderProgress
            )
        } else if (error == null) {
            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                items(peers.size) { device ->
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    if (wifiDirectService.connectToDevice(device)) {
                                        navController.navigate("consent/${device.deviceAddress}")
                                    }
                                } catch (e: Exception) {
                                    error = e.message
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text(device.deviceName)
                    }
                }
            }
        } else {
            Text(text = error!!, color = Color.Red)
        }
    }
}