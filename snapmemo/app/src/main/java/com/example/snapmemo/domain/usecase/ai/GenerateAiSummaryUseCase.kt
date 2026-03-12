package com.example.snapmemo.domain.usecase.ai

import com.example.snapmemo.domain.model.AiSummary
import com.example.snapmemo.domain.repository.AiRepository
import javax.inject.Inject

class GenerateAiSummaryUseCase @Inject constructor(
    private val aiRepository: AiRepository
) {
    suspend operator fun invoke(memoId: String, content: String, forceRefresh: Boolean = false): AiSummary =
        aiRepository.generateSummary(memoId, content, forceRefresh)
}
