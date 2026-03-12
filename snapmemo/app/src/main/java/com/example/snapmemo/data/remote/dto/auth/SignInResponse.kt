package com.example.snapmemo.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class SignInResponse(
    @SerializedName("user")
    val user: UserDto?,
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("accessTokenExpiresAt")
    val accessTokenExpiresAt: String?
)

data class UserDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("role")
    val role: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String?,
    @SerializedName("displayName")
    val displayName: String,
    @SerializedName("avatarUrl")
    val avatarUrl: String?,
    @SerializedName("state")
    val state: String,
    @SerializedName("createTime")
    val createTime: String?,
    @SerializedName("updateTime")
    val updateTime: String?
)
