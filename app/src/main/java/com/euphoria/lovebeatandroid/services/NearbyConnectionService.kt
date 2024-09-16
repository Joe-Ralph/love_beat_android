package com.euphoria.lovebeatandroid.services

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NearbyConnectionsService(
    private val context: Context
) {
    private val strategy = Strategy.P2P_POINT_TO_POINT
    private val serviceId = "com.euphoria.lovebeat"

    suspend fun exchangeData(myUuid: String): String = suspendCancellableCoroutine { continuation ->
        val connectionsClient = Nearby.getConnectionsClient(context)

        val payloadCallback = object : PayloadCallback() {
            override fun onPayloadReceived(endpointId: String, payload: Payload) {
                val receivedUuid = String(payload.asBytes()!!)
                connectionsClient.stopAdvertising()
                connectionsClient.stopDiscovery()
                continuation.resume(receivedUuid)
            }

            override fun onPayloadTransferUpdate(
                endpointId: String, update: PayloadTransferUpdate
            ) {
            }
        }

        val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                connectionsClient.acceptConnection(endpointId, payloadCallback)
            }

            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                when (result.status.statusCode) {
                    ConnectionsStatusCodes.STATUS_OK -> {
                        connectionsClient.sendPayload(
                            endpointId, Payload.fromBytes(myUuid.toByteArray())
                        )
                    }

                    else -> {
                        continuation.resumeWithException(Exception("Connection failed with status code: ${result.status.statusCode}"))
                    }
                }
            }

            override fun onDisconnected(endpointId: String) {
                // Handle disconnection if needed
            }
        }

        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(strategy).build()

        connectionsClient.startAdvertising(
            myUuid, serviceId, connectionLifecycleCallback, advertisingOptions
        ).addOnFailureListener { e ->
            continuation.resumeWithException(e)
        }

        connectionsClient.startDiscovery(
            serviceId, object : EndpointDiscoveryCallback() {
                override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                    connectionsClient.requestConnection(
                        myUuid, endpointId, connectionLifecycleCallback
                    )
                }

                override fun onEndpointLost(endpointId: String) {}
            }, discoveryOptions
        ).addOnFailureListener { e ->
            continuation.resumeWithException(e)
        }

        continuation.invokeOnCancellation {
            connectionsClient.stopAllEndpoints()
        }
    }
}