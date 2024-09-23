package com.euphoria.lovebeatandroid.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val great_vibes_font = FontFamily(
    Font(resId = R.font.greatvibes, weight = FontWeight.Normal, style = FontStyle.Normal),
)

@Composable
fun MainScreen(
    storageService: StorageService,
    navHostController: NavHostController
) {
    val context = LocalContext.current
    var loadingState by remember { mutableStateOf<LoadingState>(LoadingState.Loading) }

    LaunchedEffect(key1 = Unit) {
        withContext(Dispatchers.IO) {

            val myUuid = storageService.getMyUuid() ?: ""
            val myPartnerUuid = storageService.getPartnerUuid() ?: ""
            println("Loading user data from storage @ Main UUID: $myUuid, Partner UUID: $myPartnerUuid")
            if (myUuid.isNotEmpty()) {
                println("Done loading user data: UUID: $myUuid, Partner UUID: $myPartnerUuid")
                val intent = Intent(context, PollingService::class.java).apply {
                    putExtra("USER_ID", myUuid)
                }
                withContext(Dispatchers.Main) {
                    startForegroundService(context, intent)
                }
            }
            loadingState = LoadingState.Loaded(myUuid, myPartnerUuid)
        }
    }

    when (val state = loadingState) {
        is LoadingState.Loading -> LoadingScreen()
        is LoadingState.Loaded -> {
            LaunchedEffect(state) {
                if (state.partnerUuid.isEmpty()) {
                    println("nav to pairing")
                    navHostController.navigate(NavigationItem.Pairing.route)
                } else {
                    println("nav to vibration")
                    navHostController.navigate(NavigationItem.Vibration.route)
                }
            }
        }
    }
}


@Composable
fun LoadingScreen() {
    val pairLoaderLottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            R.raw.pairloader
        )
    )

    val preloaderProgress by animateLottieCompositionAsState(
        pairLoaderLottieComposition, iterations = LottieConstants.IterateForever, isPlaying = true
    )
    // Your loading UI here
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
    }
}

sealed class LoadingState {
    object Loading : LoadingState()
    data class Loaded(val uuid: String, val partnerUuid: String) : LoadingState()
}
