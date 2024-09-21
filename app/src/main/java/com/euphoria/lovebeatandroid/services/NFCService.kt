package com.euphoria.lovebeatandroid.services

import android.app.Activity
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NfcService(private val activity: Activity) {
    private val nfcAdapter: NfcAdapter? by lazy { NfcAdapter.getDefaultAdapter(activity) }

    private val _nfcState = MutableStateFlow<NfcState>(NfcState.Idle)
    val nfcState: StateFlow<NfcState> = _nfcState

    private var messageToWrite: String = ""

    fun enableReaderMode() {
        nfcAdapter?.enableReaderMode(activity, { tag ->
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                val ndefMessage = ndef.cachedNdefMessage
                val payload = ndefMessage.records[0].payload
                val data = String(payload, 1, payload.size - 1, Charsets.UTF_8)
                ndef.close()
                _nfcState.value = NfcState.ReadSuccess(data)
            }
        }, NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null)
    }

    fun disableReaderMode() {
        nfcAdapter?.disableReaderMode(activity)
    }

    fun enableWriteMode(customMessage: String) {
        messageToWrite = customMessage // Store the custom message

        val intent = Intent(activity, activity.javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = android.app.PendingIntent.getActivity(activity, 0, intent, android.app.PendingIntent.FLAG_MUTABLE)
        val intentFilters = arrayOf(android.content.IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))
        nfcAdapter?.enableForegroundDispatch(activity, pendingIntent, intentFilters, null)
    }

    fun disableWriteMode() {
        nfcAdapter?.disableForegroundDispatch(activity)
    }

    fun handleIntent(intent: Intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
            val tag: Tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) ?: return
            // Use the stored messageToWrite to create NDEF message
            val message = createNdefMessage(messageToWrite)
            writeNdefMessage(tag, message)
        }
    }

    private fun createNdefMessage(data: String): NdefMessage {
        val record = NdefRecord.createTextRecord(null, data)
        return NdefMessage(arrayOf(record))
    }

    private fun writeNdefMessage(tag: Tag, message: NdefMessage) {
        try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (ndef.isWritable) {
                    ndef.writeNdefMessage(message)
                    ndef.close()
                    _nfcState.value = NfcState.WriteSuccess
                }
            } else {
                val ndefFormatable = NdefFormatable.get(tag)
                if (ndefFormatable != null) {
                    ndefFormatable.connect()
                    ndefFormatable.format(message)
                    ndefFormatable.close()
                    _nfcState.value = NfcState.WriteSuccess
                }
            }
        } catch (e: Exception) {
            _nfcState.value = NfcState.Error(e.message ?: "Unknown error occurred")
        }
    }

    sealed class NfcState {
        object Idle : NfcState()
        data class ReadSuccess(val data: String) : NfcState()
        object WriteSuccess : NfcState()
        data class Error(val message: String) : NfcState()
    }
}