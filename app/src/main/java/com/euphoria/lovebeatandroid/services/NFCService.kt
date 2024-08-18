package com.euphoria.lovebeatandroid.services

import android.content.Context
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.app.Activity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NFCService(private val context: Context, private val activity: Activity) {
    suspend fun startNFCPairing(myUuid: String): String = suspendCancellableCoroutine { continuation ->
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        if (nfcAdapter == null || !nfcAdapter.isEnabled) {
            continuation.resumeWithException(Exception("NFC is not available on this device"))
            return@suspendCancellableCoroutine
        }

        val callback = object : NfcAdapter.ReaderCallback {
            override fun onTagDiscovered(tag: Tag) {
                val ndef = Ndef.get(tag) ?: run {
                    continuation.resumeWithException(Exception("NDEF is not supported by this tag"))
                    return
                }

                try {
                    ndef.connect()
                    val ndefMessage = NdefMessage(arrayOf(
                        NdefRecord.createMime("application/com.euphoria.lovebeat", myUuid.toByteArray())
                    ))
                    ndef.writeNdefMessage(ndefMessage)

                    val receivedMessage = ndef.ndefMessage
                    val partnerUuid = String(receivedMessage.records[0].payload)

                    ndef.close()
                    nfcAdapter.disableReaderMode(activity)
                    continuation.resume(partnerUuid)
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }
        }

        nfcAdapter.enableReaderMode(activity, callback, NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null)
    }
}