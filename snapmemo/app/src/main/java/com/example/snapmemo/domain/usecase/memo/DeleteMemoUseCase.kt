package com.example.snapmemo.domain.usecase.memo

import com.example.snapmemo.domain.repository.MemoRepository
import javax.inject.Inject

class DeleteMemoUseCase @Inject constructor(
    private val memoRepository: MemoRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = memoRepository.deleteMemo(id)
}
