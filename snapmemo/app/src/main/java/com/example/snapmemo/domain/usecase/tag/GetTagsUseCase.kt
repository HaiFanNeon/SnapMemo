package com.example.snapmemo.domain.usecase.tag

import com.example.snapmemo.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTagsUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    operator fun invoke(): Flow<List<String>> = tagRepository.getAllTags()
}
