package com.euphoria.lovebeatandroid.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.euphoria.lovebeatandroid.R
import com.euphoria.lovebeatandroid.data.getRandomLoveNote
import com.euphoria.lovebeatandroid.navigation.NavigationItem
import com.euphoria.lovebeatandroid.services.StorageService
import com.euphoria.lovebeatandroid.services.VibrationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun VibrationScreen(
    myUuid: String,
    partnerUuid: String,
    vibrationService: VibrationService,
    storageService: StorageService,
    navController: NavController
) {
    val context = LocalContext.current
    var isGifPlaying by remember { mutableStateOf(false) }
    val navigateToUnpairScreen: () -> Unit = {
        navController.navigate(NavigationItem.UnPair.route)
    }

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context).data(if (isGifPlaying) R.drawable.heart else null)
            .decoderFactory(ImageDecoderDecoder.Factory())
            .size(Size.ORIGINAL).build()
    )

    var myUuidFromStorage by remember { mutableStateOf("") }
    var partnerUuidFromStorage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    var longPressJob by remember { mutableStateOf<Job?>(null) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xff000212))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        isGifPlaying = true
                    },
                    onPress = {
                        longPressJob = coroutineScope.launch {
                            delay(5000)
                            navigateToUnpairScreen()
                        }
                        tryAwaitRelease()
                        longPressJob?.cancel()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (isGifPlaying) {
            Image(
                painter = painter,
                contentDescription = "Heartbeat Animation",
                modifier = Modifier.size(200.dp)
            )
        } else {
            Text(
                text = getRandomLoveNote(),
                color = Color(0xFFD73371),
                textAlign = TextAlign.Center,
                 fontFamily = satisfy_font,
                style = TextStyle(
                    fontSize = 30.sp
                ),
                modifier = Modifier
                    .requiredWidth(width = 200.dp)
                    .requiredHeight(height = 200.dp)
                    .wrapContentWidth(align = Alignment.CenterHorizontally)
                    .wrapContentHeight(align = Alignment.CenterVertically)
            )
        }
    }

    LaunchedEffect(isGifPlaying) {
        if (isGifPlaying) {
            vibrationService.vibrate()
            try {
                myUuidFromStorage = storageService.getMyUuid() ?: ""
                partnerUuidFromStorage = storageService.getPartnerUuid() ?: ""
            } catch (e: Exception) {
                println("Error fetching UUIDs from storage: ${e.message}")
            }

            println("My UUID from storage: $myUuidFromStorage")
            println("Partner UUID from storage: $partnerUuidFromStorage")

            val finalMyUuid = if (myUuidFromStorage.isNotEmpty()) myUuidFromStorage else myUuid
            val finalPartnerUuid = if (partnerUuidFromStorage.isNotEmpty()) partnerUuidFromStorage else partnerUuid

            if (finalMyUuid.isNotEmpty() && finalPartnerUuid.isNotEmpty()) {
                vibrationService.sendVibration(finalMyUuid, finalPartnerUuid)
            } else {
                println("Cannot send vibration: UUIDs are missing.")
            }

            delay(2000)
            isGifPlaying = false
        }
    }
}
