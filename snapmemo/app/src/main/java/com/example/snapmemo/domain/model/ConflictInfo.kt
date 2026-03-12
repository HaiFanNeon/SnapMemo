package com.example.snapmemo.domain.model

data class ConflictInfo(
    val id: Long,
    val memoId: String,
    val localContent: String,
    val remoteContent: String,
    val conflictFields: List<String>
)
