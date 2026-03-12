package com.example.snapmemo.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        val KEY_SERVER_URL = stringPreferencesKey("server_url")
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        val KEY_AVATAR_URL = stringPreferencesKey("avatar_url")
        val KEY_APP_MODE = stringPreferencesKey("app_mode")
        val KEY_LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_AI_BASE_URL = stringPreferencesKey("ai_base_url")
        val KEY_AI_API_KEY = stringPreferencesKey("ai_api_key")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { it[KEY_ACCESS_TOKEN] }
    val serverUrl: Flow<String?> = context.dataStore.data.map { it[KEY_SERVER_URL] }
    val username: Flow<String?> = context.dataStore.data.map { it[KEY_USERNAME] }
    val displayName: Flow<String?> = context.dataStore.data.map { it[KEY_DISPLAY_NAME] }
    val appMode: Flow<AppMode> = context.dataStore.data.map {
        AppMode.valueOf(it[KEY_APP_MODE] ?: AppMode.OFFLINE.name)
    }
    val lastSyncTime: Flow<Long?> = context.dataStore.data.map { it[KEY_LAST_SYNC_TIME] }
    val userId: Flow<String?> = context.dataStore.data.map { it[KEY_USER_ID] }
    val aiBaseUrl: Flow<String?> = context.dataStore.data.map { it[KEY_AI_BASE_URL] }
    val aiApiKey: Flow<String?> = context.dataStore.data.map { it[KEY_AI_API_KEY] }

    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { it[KEY_ACCESS_TOKEN] = token }
    }

    suspend fun saveServerUrl(url: String) {
        context.dataStore.edit { it[KEY_SERVER_URL] = url }
    }

    suspend fun saveUserInfo(userId: String, username: String, displayName: String, avatarUrl: String?) {
        context.dataStore.edit {
            it[KEY_USER_ID] = userId
            it[KEY_USERNAME] = username
            it[KEY_DISPLAY_NAME] = displayName
            if (avatarUrl != null) it[KEY_AVATAR_URL] = avatarUrl
        }
    }

    suspend fun setAppMode(mode: AppMode) {
        context.dataStore.edit { it[KEY_APP_MODE] = mode.name }
    }

    suspend fun updateLastSyncTime(time: Long) {
        context.dataStore.edit { it[KEY_LAST_SYNC_TIME] = time }
    }

    suspend fun saveAiConfig(baseUrl: String, apiKey: String) {
        context.dataStore.edit {
            it[KEY_AI_BASE_URL] = baseUrl
            it[KEY_AI_API_KEY] = apiKey
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}

enum class AppMode { OFFLINE, ONLINE }
