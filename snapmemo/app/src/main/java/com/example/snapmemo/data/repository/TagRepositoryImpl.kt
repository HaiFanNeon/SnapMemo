package com.example.snapmemo.data.repository

import com.example.snapmemo.data.local.db.dao.TagDao
import com.example.snapmemo.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao
) : TagRepository {

    override fun getAllTags(): Flow<List<String>> = tagDao.getAllTags()

    override fun getTagsForMemo(memoId: String): Flow<List<String>> =
        tagDao.getTagsForMemo(memoId)

    override fun getTagCount(): Flow<Int> = tagDao.getTagCount()
}
