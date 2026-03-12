package com.example.snapmemo.domain.usecase.memo

import com.example.snapmemo.data.local.db.entity.MemoVisibility
import com.example.snapmemo.domain.model.Memo
import com.example.snapmemo.domain.repository.MemoRepository
import javax.inject.Inject

class UpdateMemoUseCase @Inject constructor(
    private val memoRepository: MemoRepository
) {
    suspend operator fun invoke(
        id: String,
        content: String,
        visibility: MemoVisibility,
        tags: List<String>
    ): Result<Memo> = memoRepository.updateMemo(id, content, visibility, tags)
}
