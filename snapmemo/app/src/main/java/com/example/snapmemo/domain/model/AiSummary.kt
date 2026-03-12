package com.example.snapmemo.domain.model

data class AiSummary(
    val memoId: String,
    val summary: String,
    val keywords: List<String>,
    val sentiment: String?,
    val modelVersion: String
)
