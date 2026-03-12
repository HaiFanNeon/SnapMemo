package com.example.snapmemo.domain.usecase.memo

import com.example.snapmemo.data.local.db.entity.MemoVisibility
import com.example.snapmemo.domain.model.Memo
import com.example.snapmemo.domain.repository.MemoRepository
import javax.inject.Inject

class CreateMemoUseCase @Inject constructor(
    private val memoRepository: MemoRepository
) {
    suspend operator fun invoke(
        content: String,
        visibility: MemoVisibility = MemoVisibility.PRIVATE,
        tags: List<String> = emptyList()
    ): Result<Memo> = memoRepository.createMemo(content, visibility, tags)
}
