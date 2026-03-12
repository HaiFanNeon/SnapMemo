package com.example.snapmemo.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class SignInRequest(
    @SerializedName("passwordCredentials")
    val passwordCredentials: PasswordCredentials
)

data class PasswordCredentials(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)
