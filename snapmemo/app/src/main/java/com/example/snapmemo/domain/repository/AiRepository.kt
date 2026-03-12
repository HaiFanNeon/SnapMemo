package com.example.snapmemo.domain.repository

import com.example.snapmemo.domain.model.AiSummary
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    suspend fun generateSummary(memoId: String, content: String): Result<AiSummary>
    fun streamSummary(memoId: String, content: String): Flow<String>
}
