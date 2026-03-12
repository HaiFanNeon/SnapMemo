package com.example.snapmemo.data.repository

import com.example.snapmemo.data.local.datastore.AppMode
import com.example.snapmemo.data.local.datastore.UserPreferences
import com.example.snapmemo.data.remote.api.MemosAuthApi
import com.example.snapmemo.data.remote.dto.auth.PasswordCredentials
import com.example.snapmemo.data.remote.dto.auth.SignInRequest
import com.example.snapmemo.domain.model.User
import com.example.snapmemo.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userPreferences: UserPreferences,
    private val authApi: MemosAuthApi
) : AuthRepository {

    override fun isLoggedIn(): Flow<Boolean> = userPreferences.appMode.map { mode ->
        mode == AppMode.OFFLINE || userPreferences.accessToken != null
    }

    override fun getAppMode(): Flow<AppMode> = userPreferences.appMode

    override suspend fun signIn(
        serverUrl: String,
        username: String,
        password: String
    ): Result<User> = runCatching {
        userPreferences.saveServerUrl(serverUrl)
        val response = authApi.signIn(
            SignInRequest(PasswordCredentials(username, password))
        )
        userPreferences.saveAccessToken(response.accessToken)
        userPreferences.setAppMode(AppMode.ONLINE)

        val userDto = response.user
        val userId = userDto?.name?.substringAfterLast("/") ?: username
        if (userDto != null) {
            userPreferences.saveUserInfo(userId, userDto.username, userDto.displayName, userDto.avatarUrl)
        }

        User(
            id = userId,
            username = userDto?.username ?: username,
            displayName = userDto?.displayName ?: username,
            email = userDto?.email,
            avatarUrl = userDto?.avatarUrl,
            role = userDto?.role ?: "USER",
            createTime = Instant.now()
        )
    }

    override suspend fun signOut() {
        runCatching { authApi.signOut() }
        userPreferences.clearAll()
    }

    override suspend fun switchToOfflineMode() {
        userPreferences.setAppMode(AppMode.OFFLINE)
    }

    override suspend fun switchToOnlineMode(
        serverUrl: String,
        username: String,
        password: String
    ): Result<Unit> = signIn(serverUrl, username, password).map { }
}
