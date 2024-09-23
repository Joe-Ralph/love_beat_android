package com.euphoria.lovebeatandroid.services

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class StorageService(private val context: Context) {
    private val myUuidKey = stringPreferencesKey("my_uuid")
    private val partnerUuidKey = stringPreferencesKey("partner_uuid")

    suspend fun getMyUuid(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[myUuidKey]
        }.first()
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

    // TODO: use once wiping is implemented, until then use for testing purposes
    suspend fun clearPartnerUuid() {
        context.dataStore.edit { preferences ->
            preferences.remove(partnerUuidKey)
        }
    }

    suspend fun clearMyUuid() {
        context.dataStore.edit { preferences ->
            preferences.remove(myUuidKey)
        }
    }

    suspend fun saveMyUuid(uuid: String) {
        context.dataStore.edit { preferences ->
            preferences[myUuidKey] = uuid
        }
    }
}