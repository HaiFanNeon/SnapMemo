package com.example.snapmemo.domain.usecase.memo

import com.example.snapmemo.domain.repository.MemoRepository
import javax.inject.Inject

class PinMemoUseCase @Inject constructor(
    private val memoRepository: MemoRepository
) {
    suspend operator fun invoke(id: String, pinned: Boolean): Result<Unit> =
        memoRepository.pinMemo(id, pinned)
}
