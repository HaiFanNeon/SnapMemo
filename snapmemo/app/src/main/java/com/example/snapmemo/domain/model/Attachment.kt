package com.example.snapmemo.domain.model

data class Attachment(
    val id: String,
    val memoId: String?,
    val filename: String,
    val mimeType: String,
    val size: Long,
    val localPath: String?,
    val remoteUrl: String?,
    val createTime: Long
)
