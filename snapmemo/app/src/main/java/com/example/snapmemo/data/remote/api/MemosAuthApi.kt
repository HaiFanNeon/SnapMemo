package com.example.snapmemo.data.remote.api

import com.example.snapmemo.data.remote.dto.auth.SignInRequest
import com.example.snapmemo.data.remote.dto.auth.SignInResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface MemosAuthApi {

    @POST("api/v1/auth/signin")
    suspend fun signIn(@Body request: SignInRequest): SignInResponse

    @POST("api/v1/auth/signout")
    suspend fun signOut()

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(): SignInResponse
}
