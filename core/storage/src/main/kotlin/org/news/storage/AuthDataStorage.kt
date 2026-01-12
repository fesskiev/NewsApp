package org.news.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.news.model.StoredAuthData
import kotlin.io.encoding.Base64

private const val AUTH_DATA_STORE_NAME = "auth_data"

interface AuthDataStorage {
    suspend fun save(authData: StoredAuthData)
    fun get(): Flow<StoredAuthData?>
}

class AuthDataStorageImpl(
    private val dispatcher: CoroutineDispatcher,
    context: Context
) : AuthDataStorage {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = AUTH_DATA_STORE_NAME)
    private val authDataStore: DataStore<Preferences> = context.dataStore

    private object PreferencesKeys {
        val encryptedAccessTokenKey = stringPreferencesKey("encrypted_access_token")
        val encryptedRefreshTokenKey = stringPreferencesKey("encrypted_refresh_token")
        val accessTokenExpiresAtKey = longPreferencesKey("access_token_expires_at")
        val refreshTokenExpiresAtKey = longPreferencesKey("refresh_token_expires_at")
    }

    override suspend fun save(authData: StoredAuthData) {
        withContext(dispatcher) {
            authDataStore.edit { preferences ->
                preferences[PreferencesKeys.encryptedAccessTokenKey] = Base64.encode(authData.encryptedAccessToken)
                preferences[PreferencesKeys.encryptedRefreshTokenKey] = Base64.encode(authData.encryptedRefreshToken)
                preferences[PreferencesKeys.accessTokenExpiresAtKey] = authData.accessTokenExpiresAt
                preferences[PreferencesKeys.refreshTokenExpiresAtKey] = authData.refreshTokenExpiresAt
            }
        }
    }

    override fun get(): Flow<StoredAuthData?> {
        return authDataStore.data.map { preferences ->
            val encryptedAccessTokenString = preferences[PreferencesKeys.encryptedAccessTokenKey]
            val encryptedRefreshTokenString = preferences[PreferencesKeys.encryptedRefreshTokenKey]
            val accessTokenExpiresAt = preferences[PreferencesKeys.accessTokenExpiresAtKey]
            val refreshTokenExpiresAt = preferences[PreferencesKeys.refreshTokenExpiresAtKey]

            if (encryptedAccessTokenString != null && encryptedRefreshTokenString != null &&
                accessTokenExpiresAt != null && refreshTokenExpiresAt != null
            ) {
                StoredAuthData(
                    encryptedAccessToken =  Base64.decode(encryptedAccessTokenString),
                    encryptedRefreshToken =  Base64.decode(encryptedRefreshTokenString),
                    accessTokenExpiresAt = accessTokenExpiresAt,
                    refreshTokenExpiresAt = refreshTokenExpiresAt
                )
            } else {
                null
            }
        }.flowOn(dispatcher)
    }
}