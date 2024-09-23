package com.euphoria.lovebeatandroid.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startForegroundService
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.euphoria.lovebeatandroid.R
import com.euphoria.lovebeatandroid.navigation.NavigationItem
import com.euphoria.lovebeatandroid.services.PollingService
import com.euphoria.lovebeatandroid.services.StorageService
import com.euphoria.lovebeatandroid.services.WebPairingService
import kotlinx.coroutines.launch

@Composable
fun ReceiverScreen(navController: NavHostController, webPairingService: WebPairingService, storageService: StorageService) {
    BackHandler {
        navController.popBackStack() // Navigates back to Screen 1
    }

    val scope = rememberCoroutineScope()
    var pairCode by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        scope.launch {
            pairCode = webPairingService.getNewPairCode()
            val receivedUser = webPairingService.startPairPolling(pairCode)
            println("Received UUIDs @ ReceiverScreen: $receivedUser")
            storageService.saveMyUuid(receivedUser.uuid)
            storageService.savePartnerUuid(receivedUser.partnerUuid)
            if (receivedUser.uuid.isNotEmpty()) {
                println("init polling service @ ReceiverScreen: $receivedUser")
                val intent = Intent(context, PollingService::class.java).apply {
                    putExtra("USER_ID", receivedUser.uuid)
                }
                startForegroundService(context, intent)
            }
            navController.navigate(NavigationItem.Success.route)
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
            composition = pairLoaderLottieComposition, progress = preloaderProgress,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            // Text(
            //     text = "Pairing code:",
            //     color = Color(0xFFD73371),
            //     modifier = Modifier.align(Alignment.CenterHorizontally)
            // )
            Text(
                text = "$pairCode",
                color = Color(0xFFD73371),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontSize = 20.sp
                )
            )
        }
    }
}


@Composable
@Preview(widthDp = 512, heightDp = 512, apiLevel = 33)
fun ReceiverScreenPreview() {

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
             composition = pairLoaderLottieComposition, progress = preloaderProgress,
             modifier = Modifier.fillMaxSize()
         )

        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            // Text(
            //     text = "Pairing code:",
            //     color = Color(0xFFD73371),
            //     modifier = Modifier.align(Alignment.CenterHorizontally)
            // )
            Text(
                text = "627116",
                color = Color(0xFFD73371),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontSize = 20.sp
                )
            )
        }
    }
}