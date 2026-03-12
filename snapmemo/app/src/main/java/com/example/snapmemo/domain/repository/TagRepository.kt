package com.example.snapmemo.domain.repository

import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun getAllTags(): Flow<List<String>>
    fun getTagsForMemo(memoId: String): Flow<List<String>>
    fun getTagCount(): Flow<Int>
}
