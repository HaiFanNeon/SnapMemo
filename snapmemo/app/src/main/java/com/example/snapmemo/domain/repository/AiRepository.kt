package com.example.snapmemo.domain.repository

import com.example.snapmemo.domain.model.AiSummary

interface AiRepository {
    suspend fun generateSummary(memoId: String, content: String, forceRefresh: Boolean = false): AiSummary
}
