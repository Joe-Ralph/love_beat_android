package com.euphoria.lovebeatandroid.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.euphoria.lovebeatandroid.R

class PollingService : Service() {

    private lateinit var vibrationService: VibrationService

    override fun onCreate() {
        super.onCreate()
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrationService = VibrationService(vibrator)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val userId = intent?.getStringExtra("USER_ID") ?: return START_NOT_STICKY

        startForegroundService()
        vibrationService.startPolling(userId) { success ->
            if (success) {
                sendVibrationReceivedBroadcast()
            }
        }

        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "PollingServiceChannel"
        val channelName = "Polling Service Channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Polling Service")
            .setContentText("Listening for vibrations...")
//            .setSmallIcon(R.drawable.ic_notification)
            .build()

        startForeground(1, notification)
    }

    private fun sendVibrationReceivedBroadcast() {
        val intent = Intent("com.euphoria.lovebeatandroid.VIBRATION_RECEIVED")
        sendBroadcast(intent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}