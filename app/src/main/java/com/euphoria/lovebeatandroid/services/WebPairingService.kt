package com.euphoria.lovebeatandroid.services

import android.content.Context
import com.euphoria.lovebeatandroid.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

class WebPairingService(private val context: Context) {


    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS) // No read timeout for long-polling
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
    private val pollJob = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + pollJob)


    private fun generatePairCode(): String {
        return (100000..999999).random().toString()
    }

    fun getNewPairCode(): String {
        return generatePairCode()
    }

    suspend fun startPairPolling(userId: String): User {
        while (true) {
            try {
                val user = pollForPairing(userId)
                if (user != null) {
                    return user
                }
            } catch (e: Exception) {
                // Handle exception, e.g., log or display error message
                println("Error polling for pairing: ${e.message}")
            }
            delay(30 * 1000L)
        }
    }

    private suspend fun pollForPairing(userId: String): User? {
        val request = Request.Builder()
            .url("https://lovebeatserver.onrender.com/pair/$userId")
            .build()

        return withContext(Dispatchers.IO) {
            try {
                println("https://lovebeatserver.onrender.com/pair/$userId")
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val jsonResponse = JSONObject(response.body!!.string())
                        val myUuid = jsonResponse.getString("myUuid")
                        val partnerUuid = jsonResponse.getString("partnerUuid")
                        println("Received UUID data as myUUID: $myUuid and partnerUUID $partnerUuid")
                        val user = User()
                        user.uuid = myUuid
                        user.partnerUuid = partnerUuid
                        return@use user
                    } else {
                        throw IOException("Failed pairing")
                    }
                }
            } catch (e: IOException) {
                println("Error while pair polling: ${e.message}")
                return@withContext null
            }
        }
    }

    public fun generateUuids(): User {
        val myUuid = UUID.randomUUID().toString()
        val partnerUuid = UUID.randomUUID().toString()
        val user = User()
        user.uuid = myUuid
        user.partnerUuid = partnerUuid
        return user
    }
}