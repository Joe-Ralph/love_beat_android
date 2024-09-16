package com.euphoria.lovebeatandroid.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.euphoria.lovebeatandroid.R
import com.euphoria.lovebeatandroid.data.getRandomLoveNote
import com.euphoria.lovebeatandroid.services.VibrationService
import kotlinx.coroutines.delay

@Composable
fun VibrationScreen(myUuid: String, partnerUuid: String?, vibrationService: VibrationService) {
    val context = LocalContext.current
    var isGifPlaying by remember { mutableStateOf(false) }

    // Use Coil's AsyncImage for handling GIFs
    val painter = rememberImagePainter(
        ImageRequest.Builder(context).data(if (isGifPlaying) R.drawable.heart else null)
            .decoderFactory(ImageDecoderDecoder.Factory()) // Decoder for animated GIFs
            .size(Size.ORIGINAL).build()
    )

    // Box to align image in the center of the screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xff000212))
            .clickable {
                isGifPlaying = true
            }, contentAlignment = Alignment.Center
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
                fontFamily = great_vibes_font,
                style = TextStyle(
                    fontSize = 40.sp
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
            if (partnerUuid != null) {
                vibrationService.sendVibration(myUuid, partnerUuid)
            }
            delay(2000)
            isGifPlaying = false
        }
    }
}