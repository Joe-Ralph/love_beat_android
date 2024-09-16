package com.euphoria.lovebeatandroid.services

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.euphoria.lovebeatandroid.models.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "settings")

class StorageService(private val context: Context) {
    private val myUuidKey = stringPreferencesKey("my_uuid")
    private val partnerUuidKey = stringPreferencesKey("partner_uuid")

    suspend fun getMyUuid(): String {
        val uuid = context.dataStore.data.map { preferences ->
            preferences[myUuidKey]
        }.first()

        return uuid ?: UUID.randomUUID().toString().also { newUuid ->
            context.dataStore.edit { preferences ->
                preferences[myUuidKey] = newUuid
            }
        }
    }

    suspend fun getPartnerUuid(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[partnerUuidKey]
        }.first()
    }


    suspend fun savePartnerUuid(uuid: String) {
        context.dataStore.edit { preferences ->
            preferences[partnerUuidKey] = uuid
        }
    }
    // TODO: Add other storage-related methods here
    suspend fun clearPartnerUuid() {
        context.dataStore.edit { preferences ->
            preferences.remove(partnerUuidKey)
        }
    }
    // For Testing Purposes
    suspend fun saveMyUuid(uuid: String) {
        context.dataStore.edit { preferences ->
            preferences[myUuidKey] = uuid
        }
    }
}