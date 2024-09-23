package com.euphoria.lovebeatandroid.screens

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startForegroundService
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.euphoria.lovebeatandroid.R
import com.euphoria.lovebeatandroid.navigation.NavigationItem
import com.euphoria.lovebeatandroid.services.PollingService
import com.euphoria.lovebeatandroid.services.StorageService
import com.euphoria.lovebeatandroid.services.WebPairingService
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch
import okhttp3.HttpUrl


@Composable
//@Preview(widthDp = 512, heightDp = 512, apiLevel = 33)
fun SenderScreen(navController: NavHostController, webPairingService: WebPairingService, storageService: StorageService) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pairCode by remember { mutableStateOf("") }
    var pairUrl by remember { mutableStateOf("") }


    BackHandler {
        navController.popBackStack() // Navigates back to Screen 1
    }

    LaunchedEffect(Unit) {
        scope.launch {
            pairCode = webPairingService.getNewPairCode()
            val user = webPairingService.generateUuids();
            println("Generated UUIDs @ SenderScreen: $user")
            val urlBuilder = HttpUrl.Builder()
                .scheme("https")
                .host("lovebeatserver.onrender.com")
                .addQueryParameter("paircode", pairCode)
                .addQueryParameter("senderId", user.uuid)
                .addQueryParameter("receiverId", user.partnerUuid)
            pairUrl = urlBuilder.build().toString()

            val receivedUser = webPairingService.startPairPolling(pairCode)
            println("Received UUIDs @ SenderScreen: $receivedUser")
            storageService.saveMyUuid(receivedUser.uuid)
            storageService.savePartnerUuid(receivedUser.partnerUuid)
            if (receivedUser.uuid.isNotEmpty()) {
                println("init polling service @ SenderScreen: $receivedUser")
                val intent = Intent(context, PollingService::class.java).apply {
                    putExtra("USER_ID", receivedUser.uuid)
                }
                startForegroundService(context, intent)
            }
            navController.navigate(NavigationItem.Success.route)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .size(512.dp)
            .clip(shape = RoundedCornerShape(512.dp))
            .background(color = Color(0xff000212))
            .wrapContentWidth(align = Alignment.CenterHorizontally)
            .wrapContentHeight(align = Alignment.CenterVertically)
    ) {
        if (pairUrl.isNotEmpty()) {
            QRCodeImage(pairUrl)
        }
    }
}

@Composable
fun QRCodeImage(url: String, size: Int = 200) {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(url, BarcodeFormat.QR_CODE, size, size)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(
                x,
                y,
                if (bitMatrix[x, y]) Color(0xFFD73371).toArgb() else Color(0xff000212).toArgb()
            )
        }
    }
    Color(0xff000212).toArgb()

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "QR Code for $url",
        modifier = Modifier.size(size.dp)
    )
}


@Composable
@Preview(widthDp = 512, heightDp = 512, apiLevel = 33)
fun SenderScreenPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .size(512.dp)
            .clip(shape = RoundedCornerShape(512.dp))
            .background(color = Color(0xff000212))
            .wrapContentWidth(align = Alignment.CenterHorizontally)
            .wrapContentHeight(align = Alignment.CenterVertically)
    ) {
        QRCodeImage("pairUrl")
    }
}