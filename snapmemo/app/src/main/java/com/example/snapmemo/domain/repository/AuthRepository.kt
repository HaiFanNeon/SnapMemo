package com.example.snapmemo.domain.repository

import com.example.snapmemo.data.local.datastore.AppMode
import com.example.snapmemo.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun isLoggedIn(): Flow<Boolean>
    fun getAppMode(): Flow<AppMode>
    suspend fun signIn(serverUrl: String, username: String, password: String): Result<User>
    suspend fun signOut()
    suspend fun switchToOfflineMode()
    suspend fun switchToOnlineMode(serverUrl: String, username: String, password: String): Result<Unit>
}
