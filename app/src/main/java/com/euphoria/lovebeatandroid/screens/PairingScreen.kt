package com.euphoria.lovebeatandroid.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.euphoria.lovebeatandroid.navigation.NavigationItem
import com.euphoria.lovebeatandroid.services.WifiDirectService

@Composable
//@Preview(widthDp = 225, heightDp = 225, apiLevel = 33)
fun PairingScreen(navHostController: NavHostController, wifiDirectService: WifiDirectService) {
    var isSender by remember { mutableStateOf(false) }

    BackHandler {
        navHostController.popBackStack() // Navigates back to Screen 1
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
        Column(
            modifier = Modifier
                .requiredWidth(width = 225.dp)
                .requiredHeight(height = 200.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                onClick = { navHostController.navigate(NavigationItem.SenderScreen.route) },
                color = Color(0xFFD73371),
                modifier = Modifier
                    .width(150.dp)
                    .requiredHeight(50.dp)
                    .clip(shape = RoundedCornerShape(50.dp))
            ) {
                Text(
                    text = "Seek",
                    color = Color(0xffffffff),
                    textAlign = TextAlign.Center,
                    fontFamily = great_vibes_font,
                    style = TextStyle(
                        fontSize = 20.sp
                    ),
                    modifier = Modifier
                        .wrapContentWidth(align = Alignment.CenterHorizontally)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                )
            }
            Surface(
                onClick = { navHostController.navigate(NavigationItem.ReceiverScreen.route) },
                color = Color(0xFFD73371),
                modifier = Modifier
                    .width(150.dp)
                    .height(50.dp)
                    .requiredHeight(50.dp)
                    .clip(shape = RoundedCornerShape(50.dp))
            ) {
                Text(
                    text = "Embrace",
                    color = Color(0xffffffff),
                    textAlign = TextAlign.Center,
                    fontFamily = great_vibes_font,
                    style = TextStyle(
                        fontSize = 20.sp
                    ),
                    modifier = Modifier
                        .wrapContentWidth(align = Alignment.CenterHorizontally)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                )
            }
        }
    }
}