package com.example.snapmemo.domain.model

import java.time.Instant

data class Attachment(
    val id: String,
    val filename: String,
    val mimeType: String,
    val size: Long,
    val localPath: String?,
    val remoteUrl: String?,
    val createTime: Instant
)
