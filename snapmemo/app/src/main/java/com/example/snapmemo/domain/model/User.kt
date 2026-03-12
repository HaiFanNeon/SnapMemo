package com.example.snapmemo.domain.model

import java.time.Instant

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String?,
    val avatarUrl: String?,
    val role: String,
    val createTime: Instant
)
