package com.euphoria.lovebeatandroid.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.net.ServerSocket
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class WifiDirectService(private val context: Context) {

    private val manager: WifiP2pManager by lazy(LazyThreadSafetyMode.NONE) {
        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }

    private val channel: WifiP2pManager.Channel by lazy(LazyThreadSafetyMode.NONE) {
        manager.initialize(context, Looper.getMainLooper(), null)
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // ... (previous implementation)
        }
    }

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
    }

    suspend fun startAdvertising(): Boolean = suspendCancellableCoroutine { continuation ->
        if (!checkAndRequestPermissions()) {
            continuation.resumeWithException(Exception("Required permissions are not granted"))
            return@suspendCancellableCoroutine
        }

        try {
            context.registerReceiver(receiver, intentFilter)

            manager.createGroup(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    continuation.resume(true)
                }

                override fun onFailure(reason: Int) {
                    continuation.resumeWithException(Exception("Failed to create group: $reason"))
                }
            })

            continuation.invokeOnCancellation {
                context.unregisterReceiver(receiver)
                manager.removeGroup(channel, null)
            }
        } catch (e: SecurityException) {
            continuation.resumeWithException(Exception("Security exception: ${e.message}"))
        }
    }

    suspend fun discoverPeers(): List<WifiP2pDevice> = callbackFlow {
        if (!checkAndRequestPermissions()) {
            throw Exception("Required permissions are not granted")
        }

        try {
            context.registerReceiver(receiver, intentFilter)

            manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    // Discovery started
                }

                override fun onFailure(reason: Int) {
                    close(Exception("Failed to start peer discovery: $reason"))
                }
            })

            manager.requestPeers(channel) { peers ->
                trySend(peers.deviceList.toList())
            }

            awaitClose {
                context.unregisterReceiver(receiver)
                manager.stopPeerDiscovery(channel, null)
            }
        } catch (e: SecurityException) {
            throw Exception("Security exception: ${e.message}")
        }
    }.first() // We only need the first emission for our use case

    suspend fun exchangeData(myUuid: String): String = suspendCancellableCoroutine { continuation ->
        if (!checkAndRequestPermissions()) {
            continuation.resumeWithException(Exception("Required permissions are not granted"))
            return@suspendCancellableCoroutine
        }

        try {
            context.registerReceiver(receiver, intentFilter)

            manager.requestConnectionInfo(channel) { info ->
                if (info.groupFormed) {
                    if (info.isGroupOwner) {
                        // This device is the group owner (server)
                        transferData(myUuid, continuation)
                    } else {
                        // This device is a client
                        connectToGroupOwner(
                            info.groupOwnerAddress.hostAddress,
                            myUuid,
                            continuation
                        )
                    }
                } else {
                    continuation.resumeWithException(Exception("No group formed"))
                }
            }

            continuation.invokeOnCancellation {
                context.unregisterReceiver(receiver)
                manager.removeGroup(channel, null)
            }
        } catch (e: SecurityException) {
            continuation.resumeWithException(Exception("Security exception: ${e.message}"))
        }
    }

    private fun connectToGroupOwner(
        hostAddress: String,
        myUuid: String,
        continuation: kotlin.coroutines.Continuation<String>
    ) {
        Thread {
            try {
                val socket = java.net.Socket(hostAddress, 8888)
                val inputStream = socket.getInputStream()
                val outputStream = socket.getOutputStream()

                // Send my UUID
                outputStream.write(myUuid.toByteArray())

                // Receive partner's UUID
                val buffer = ByteArray(36)
                inputStream.read(buffer)
                val partnerUuid = String(buffer)

                socket.close()
                continuation.resume(partnerUuid)
            } catch (e: IOException) {
                continuation.resumeWithException(e)
            }
        }.start()
    }

    private fun transferData(myUuid: String, continuation: kotlin.coroutines.Continuation<String>) {
        Thread {
            try {
                val socket = ServerSocket(8888).accept()
                val inputStream = socket.getInputStream()
                val outputStream = socket.getOutputStream()

                // Send my UUID
                outputStream.write(myUuid.toByteArray())

                // Receive partner's UUID
                val buffer = ByteArray(36)
                inputStream.read(buffer)
                val partnerUuid = String(buffer)

                socket.close()
                continuation.resume(partnerUuid)
            } catch (e: IOException) {
                continuation.resumeWithException(e)
            }
        }.start()
    }

    private fun checkAndRequestPermissions(): Boolean {
        // ... (previous implementation)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}