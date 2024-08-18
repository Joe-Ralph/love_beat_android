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
import java.io.IOException
import java.util.concurrent.TimeUnit

class VibrationService(private val vibrator: Vibrator) {
    private var isVibrating by mutableStateOf(false)
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS) // No read timeout for long-polling
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
    private val pollJob = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + pollJob)

    fun startPolling(userId: String, onVibrationReceived: (Boolean) -> Unit) {
        coroutineScope.launch {
            while (true) {
                try {
                    val success = pollForVibrations(userId)
                    if (success) {
                        onVibrationReceived(true)
                    }
                } catch (e: Exception) {
                    // Handle exception, e.g., log or display error message
                    println("Error polling for vibrations: ${e.message}")
                }
                delay(30 * 1000L) // Delay for 2 minutes before next poll
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
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        println("Vibration sent successfully - ${response.body?.string()}")
                        vibrate()
                    } else {
                        throw IOException("Failed to send vibration")
                    }
                }
            } catch (e: IOException) {
                println("Error sending vibration: ${e.message}")
            }
        }
    }

    fun vibrate() {
        isVibrating = true
        val effect = VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(effect)
        isVibrating = false
    }

    fun vibrateInPattern() {
        isVibrating = true
        val pattern = longArrayOf(0, 293, 673, 557, 678, 355,0)
        val effect = VibrationEffect.createWaveform(pattern, -1)
        vibrator.vibrate(effect)
        isVibrating = false
    }

    

    suspend fun pollForVibrations(userId: String): Boolean {
        val request = Request.Builder()
            .url("https://lovebeatserver.onrender.com/poll/$userId")
            .build()

        return withContext(Dispatchers.IO) {
            try {
                println("https://lovebeatserver.onrender.com/poll/$userId")
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val jsonResponse = JSONObject(response.body!!.string())
                        if (jsonResponse.getBoolean("vibrate")) {
                            val senderId = jsonResponse.getString("from")
                            println("Received vibration from $senderId")
                            vibrateInPattern()
                            return@use true
                        }
                    } else {
                        throw IOException("Failed to poll for vibrations")
                    }
                    return@use false
                }
            } catch (e: IOException) {
                println("Error polling for vibrations: ${e.message}")
                return@withContext false
            }
        }
    }
}