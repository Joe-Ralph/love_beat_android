package com.euphoria.lovebeatandroid.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
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

    private val _connectionState = MutableSharedFlow<ConnectionState>()
    val connectionState: Flow<ConnectionState> = _connectionState


    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    // Check if Wi-Fi P2P is supported and enabled
                }

                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    manager.requestPeers(channel) { peerList ->
                        // Handle the peer list
                    }
                }

                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    // Handle connection changes
                }
            }
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

    suspend fun discoverPeers(): Flow<List<WifiP2pDevice>> = callbackFlow {
        if (!checkAndRequestPermissions()) {
            throw Exception("Required permissions are not granted")
        }

        val peerListListener = WifiP2pManager.PeerListListener { peers ->
            trySend(peers.deviceList.toList())
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

            manager.requestPeers(channel, peerListListener)

            awaitClose {
                context.unregisterReceiver(receiver)
                manager.stopPeerDiscovery(channel, null)
            }
        } catch (e: SecurityException) {
            throw Exception("Security exception: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun connectToDevice(device: WifiP2pDevice): Boolean = suspendCancellableCoroutine { continuation ->
        if (!checkAndRequestPermissions()) {
            continuation.resumeWithException(Exception("Required permissions are not granted"))
            return@suspendCancellableCoroutine
        }

        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
            wps.setup = WpsInfo.PBC
        }

        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                _connectionState.tryEmit(ConnectionState.Connected(device))
                continuation.resume(true)
            }

            override fun onFailure(reason: Int) {
                _connectionState.tryEmit(ConnectionState.Failed(reason))
                continuation.resumeWithException(Exception("Failed to connect: $reason"))
            }
        })
    }

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
                            info.groupOwnerAddress.hostAddress!!,
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
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                context as android.app.Activity,
                permissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
            return false
        }

        return true
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        data class Connected(val device: WifiP2pDevice) : ConnectionState()
        data class Failed(val reason: Int) : ConnectionState()
    }
}