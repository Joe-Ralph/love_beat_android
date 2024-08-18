package com.euphoria.lovebeatandroid.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startForegroundService
import coil.compose.rememberImagePainter
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.euphoria.lovebeatandroid.R
import com.euphoria.lovebeatandroid.data.getRandomLoveNote
import com.euphoria.lovebeatandroid.models.User
import com.euphoria.lovebeatandroid.services.PollingService
import com.euphoria.lovebeatandroid.services.StorageService
import com.euphoria.lovebeatandroid.services.VibrationService
import kotlinx.coroutines.delay


val great_vibes_font = FontFamily(
    Font(resId = R.font.greatvibes, weight = FontWeight.Normal, style = FontStyle.Normal),
)

@Composable
fun MainScreen(storageService: StorageService, vibrationService: VibrationService) {
    val context = LocalContext.current
    var user by remember { mutableStateOf(User()) }
    var isLoading by remember { mutableStateOf(true) }
    var isHeartGifPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // For testing purposes, save sample UUIDs
        storageService.savePartnerUuid("2c7f3281-e76e-406c-a7d8-7b1e58300671")
        storageService.saveMyUuid("cea36efa-e594-43bf-9c17-46fef950d3c2")
        // ======================================
        user.uuid = storageService.getMyUuid()
        user.partnerUuid = storageService.getPartnerUuid() ?: ""
        isLoading = false

        // Start the PollingService once the user ID is available
        if (user.uuid.isNotEmpty()) {
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
                isHeartGifPlaying = true
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
        PairingScreen(user, storageService, vibrationService)
    } else {
        VibrationScreen(user, vibrationService, isHeartGifPlaying)
    }
}

@Composable
fun LoadingScreen() {
    // Implement loading screen UI
}

@Composable
fun PairingScreen(user: User, storageService: StorageService, vibrationService: VibrationService) {
}

@Composable
@Preview(widthDp = 512, heightDp = 512, apiLevel = 33)
fun PairingScreenPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .size(512.dp)
            .clip(shape = RoundedCornerShape(512.dp))
            .background(color = Color(0xff25283d))
            .wrapContentWidth(align = Alignment.CenterHorizontally)
            .wrapContentHeight(align = Alignment.CenterVertically)
    ) {

        Text(
            text = "Tap and Let our hearts connect",
            color = Color(0xfffda1a8),
            textAlign = TextAlign.Center,
            fontFamily = great_vibes_font,
            style = androidx.compose.ui.text.TextStyle(
                fontSize = 60.sp
            ),
            modifier = Modifier
                .requiredWidth(width = 277.dp)
                .requiredHeight(height = 250.dp)
                .wrapContentWidth(align = Alignment.CenterHorizontally)
                .wrapContentHeight(align = Alignment.CenterVertically)
        )

    }
}

@Composable
fun VibrationScreen(user: User, vibrationService: VibrationService, isHeartGifPlaying: Boolean) {
    AnimateGifOnTap(vibrationService, user, isHeartGifPlaying)
}

@Composable
fun AnimateGifOnTap(vibrationService: VibrationService, user: User, isHeartGifPlaying: Boolean) {
    val context = LocalContext.current
    var isGifPlaying by remember { mutableStateOf(isHeartGifPlaying) }

    // Use Coil's AsyncImage for handling GIFs
    val painter = rememberImagePainter(
        ImageRequest.Builder(context)
            .data(if (isGifPlaying) R.drawable.heart else null)
            .decoderFactory(ImageDecoderDecoder.Factory()) // Decoder for animated GIFs
            .size(Size.ORIGINAL)
            .build()
    )

    // Box to align image in the center of the screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xff25283d))
            .clickable {
                isGifPlaying = true
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
                color = Color(0xfffda1a8),
                textAlign = TextAlign.Center,
                fontFamily = great_vibes_font,
                style = androidx.compose.ui.text.TextStyle(
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
            vibrationService.sendVibration(user.uuid, user.partnerUuid)
            delay(2000)
            isGifPlaying = false
        }
    }
}