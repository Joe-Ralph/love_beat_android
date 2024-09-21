package com.euphoria.lovebeatandroid.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.euphoria.lovebeatandroid.navigation.NavigationItem
import com.euphoria.lovebeatandroid.services.WifiDirectService
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun ConsentScreen(
    wifiDirectService: WifiDirectService,
    navController: NavController,
    deviceAddress: String
) {
    var exchangeComplete by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Consent to Exchange Info")
//        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        val myUuid = UUID.randomUUID().toString()
                        val partnerUuid = wifiDirectService.exchangeData(myUuid)
                        exchangeComplete = true
                        navController.navigate("success/$partnerUuid")
                    } catch (e: Exception) {
                        error = e.message
                    }
                }
            }
        ) {
            Text("Accept and Exchange")
        }

        if (error != null) {
            Text("Error: $error")
        }
    }
}

@Composable
fun SuccessScreen(partnerUuid: String, navController: NavController) {
    var showAnimation by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000) // Show animation for 3 seconds
        showAnimation = false
        navController.navigate(NavigationItem.Vibration.route)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (showAnimation) {
            // Replace this with your actual animation
            CircularProgressIndicator()
        }
        Text("Successfully paired! Partner UUID: $partnerUuid")
    }
}