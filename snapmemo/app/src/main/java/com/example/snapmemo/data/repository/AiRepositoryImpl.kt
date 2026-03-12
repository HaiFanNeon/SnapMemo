package com.example.snapmemo.data.repository

import com.example.snapmemo.data.local.db.dao.AiCacheDao
import com.example.snapmemo.data.local.db.entity.AiCacheEntity
import com.example.snapmemo.domain.model.AiSummary
import com.example.snapmemo.domain.repository.AiRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepositoryImpl @Inject constructor(
    private val aiCacheDao: AiCacheDao,
    private val gson: Gson
) : AiRepository {

    override suspend fun generateSummary(
        memoId: String,
        content: String,
        forceRefresh: Boolean
    ): AiSummary {
        if (!forceRefresh) {
            val cached = aiCacheDao.getByMemoId(memoId)
            if (cached != null) {
                val keywords = gson.fromJson<List<String>>(
                    cached.keywords,
                    object : TypeToken<List<String>>() {}.type
                ) ?: emptyList()
                return AiSummary(
                    memoId = cached.memoId,
                    summary = cached.summary,
                    keywords = keywords,
                    sentiment = cached.sentiment,
                    modelVersion = cached.modelVersion
                )
            }
        }

        // AI 服务暂未对接，返回占位数据
        val placeholderSummary = AiSummary(
            memoId = memoId,
            summary = "AI 总结功能即将上线，敬请期待。",
            keywords = emptyList(),
            sentiment = null,
            modelVersion = "placeholder"
        )

        aiCacheDao.insert(
            AiCacheEntity(
                memoId = memoId,
                summary = placeholderSummary.summary,
                keywords = "[]",
                sentiment = null,
                generatedAt = System.currentTimeMillis(),
                modelVersion = "placeholder"
            )
        )

        return placeholderSummary
    }
}
