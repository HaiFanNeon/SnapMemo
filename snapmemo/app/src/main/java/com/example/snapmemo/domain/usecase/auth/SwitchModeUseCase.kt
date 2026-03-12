package com.example.snapmemo.domain.usecase.auth

import com.example.snapmemo.domain.repository.AuthRepository
import javax.inject.Inject

class SwitchModeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun toOffline() = authRepository.switchToOfflineMode()
    suspend fun toOnline(serverUrl: String, username: String, password: String): Result<Unit> =
        authRepository.switchToOnlineMode(serverUrl, username, password)
}
