package com.example.snapmemo.domain.usecase.auth

import com.example.snapmemo.domain.model.User
import com.example.snapmemo.domain.repository.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(serverUrl: String, username: String, password: String): Result<User> =
        authRepository.signIn(serverUrl, username, password)
}
