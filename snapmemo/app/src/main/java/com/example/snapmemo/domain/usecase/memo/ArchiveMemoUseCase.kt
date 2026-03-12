package com.example.snapmemo.domain.usecase.memo

import com.example.snapmemo.domain.repository.MemoRepository
import javax.inject.Inject

class ArchiveMemoUseCase @Inject constructor(
    private val memoRepository: MemoRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = memoRepository.archiveMemo(id)
    suspend fun unarchive(id: String): Result<Unit> = memoRepository.unarchiveMemo(id)
}
