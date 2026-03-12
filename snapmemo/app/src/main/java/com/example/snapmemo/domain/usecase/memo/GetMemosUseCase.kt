package com.example.snapmemo.domain.usecase.memo

import com.example.snapmemo.domain.model.Memo
import com.example.snapmemo.domain.repository.MemoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMemosUseCase @Inject constructor(
    private val memoRepository: MemoRepository
) {
    operator fun invoke(): Flow<List<Memo>> = memoRepository.getMemos()
}
