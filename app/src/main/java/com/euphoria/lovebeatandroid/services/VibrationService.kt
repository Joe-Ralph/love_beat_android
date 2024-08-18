package com.euphoria.lovebeatandroid.services

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class VibrationService(private val vibrator: Vibrator) {
    private var isVibrating by mutableStateOf(false)
    private val client = OkHttpClient()
    private val pollJob = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + pollJob)

    fun startPolling(userId: String) {
        coroutineScope.launch {
            while (true) {
                try {
                    pollForVibrations(userId)
                } catch (e: Exception) {
                    // Handle exception, e.g., log or display error message
                    println("Error polling for vibrations: ${e.message}")
                }
                delay(2 * 60 * 1000L) // Delay for 2 minutes
            }
        }
    }

    suspend fun sendVibration(senderUuid: String, receiverUuid: String) {
        val json = JSONObject().apply {
            put("senderId", senderUuid)
            put("receiverId", receiverUuid)
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://lovebeatserver.onrender.com/vibrate")
            .post(body)
            .build()

        withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    println("Vibration sent successfully - ${response.body?.string()}")
                    vibrate()
                } else {
                    throw Exception("Failed to send vibration")
                }
            }
        }
    }

    fun vibrate() {
        isVibrating = true
        val effect = VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(effect)
        isVibrating = false
    }

    suspend fun pollForVibrations(userId: String) {
        val request = Request.Builder()
            .url("https://lovebeatserver.onrender.com/poll/$userId")
            .build()

        withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(response.body!!.string())
                    if (jsonResponse.getBoolean("vibrate")) {
                        val senderId = jsonResponse.getString("from")
                        println("Received vibration from $senderId")
                        vibrate()
                    }
                } else {
                    throw Exception("Failed to poll for vibrations")
                }
            }
        }
    }

}