package com.example.snapmemo.data.repository

import android.util.Log
import com.example.snapmemo.data.local.db.dao.AiCacheDao
import com.example.snapmemo.data.local.db.entity.AiCacheEntity
import com.example.snapmemo.data.remote.api.AiServiceApi
import com.example.snapmemo.data.remote.dto.ai.AiMessage
import com.example.snapmemo.data.remote.dto.ai.AiSummaryRequest
import com.example.snapmemo.domain.model.AiSummary
import com.example.snapmemo.domain.repository.AiRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Singleton

@Singleton
class AiRepositoryImpl constructor(
    private val aiCacheDao: AiCacheDao,
    private val gson: Gson,
    private val aiServiceApi: AiServiceApi? = null
) : AiRepository {

    companion object {
        private const val TAG = "AiRepository"
        private const val CACHE_TTL_MS = 24 * 60 * 60 * 1000L
        private const val DEFAULT_MODEL = "gpt-4o-mini"
        private const val SYSTEM_PROMPT = """你是一个专业的笔记助理。请对用户提供的笔记内容进行简洁的摘要，
要求：
1. 用中文输出
2. 3-5句话概括核心内容
3. 提取关键词（不超过5个），格式：关键词：词1，词2，词3
4. 判断情感倾向（积极/中性/消极），格式：情感：积极
5. 指出内容所属的分类"""
    }

    override suspend fun generateSummary(memoId: String, content: String): Result<AiSummary> {
        val cached = getCachedSummary(memoId)
        if (cached != null) return Result.success(cached)

        if (aiServiceApi == null) {
            return Result.success(
                AiSummary(
                    memoId = memoId,
                    summary = "AI 服务未配置，请在设置中配置 API Key。",
                    keywords = emptyList(),
                    sentiment = null,
                    modelVersion = DEFAULT_MODEL
                )
            )
        }

        return runCatching {
            val request = AiSummaryRequest(
                messages = listOf(
                    AiMessage("system", SYSTEM_PROMPT),
                    AiMessage("user", content)
                )
            )
            val response = aiServiceApi?.generateSummary(request)
            val summaryText = response?.choices?.firstOrNull()?.message?.content
                ?: throw IllegalStateException("AI 返回内容为空")
            val summary = AiSummary(
                memoId = memoId,
                summary = summaryText,
                keywords = extractKeywords(summaryText),
                sentiment = extractSentiment(summaryText),
                modelVersion = DEFAULT_MODEL
            )
            cacheSummary(summary)
            summary
        }.onFailure { e -> Log.e(TAG, "AI summary failed", e) }
    }

    override fun streamSummary(memoId: String, content: String): Flow<String> = flow {
        if (aiServiceApi == null) {
            emit("AI 服务未配置。")
            return@flow
        }
        val request = AiSummaryRequest(
            messages = listOf(
                AiMessage("system", SYSTEM_PROMPT),
                AiMessage("user", content)
            ),
            stream = true
        )
        try {
            val responseBody = aiServiceApi.generateSummaryStream(request)
            val source = responseBody.source()
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                if (line.startsWith("data: ") && !line.contains("[DONE]")) {
                    val json = line.removePrefix("data: ")
                    val chunk = gson.fromJson(
                        json,
                        com.example.snapmemo.data.remote.dto.ai.AiSummaryResponse::class.java
                    )
                    val delta = chunk.choices.firstOrNull()?.delta?.content
                    if (!delta.isNullOrEmpty()) emit(delta)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "AI stream failed", e)
            emit("生成失败：${e.message}")
        }
    }

    private suspend fun getCachedSummary(memoId: String): AiSummary? {
        val entity = aiCacheDao.getByMemoId(memoId) ?: return null
        val expiry = entity.generatedAt + CACHE_TTL_MS
        if (System.currentTimeMillis() > expiry) {
            aiCacheDao.deleteByMemoId(memoId)
            return null
        }
        return AiSummary(
            memoId = entity.memoId,
            summary = entity.summary,
            keywords = entity.keywords.split(",").filter { it.isNotBlank() },
            sentiment = entity.sentiment,
            modelVersion = entity.modelVersion
        )
    }

    private suspend fun cacheSummary(summary: AiSummary) {
        val entity = AiCacheEntity(
            memoId = summary.memoId,
            summary = summary.summary,
            keywords = summary.keywords.joinToString(","),
            sentiment = summary.sentiment,
            generatedAt = System.currentTimeMillis(),
            modelVersion = summary.modelVersion
        )
        aiCacheDao.insert(entity)
    }

    private fun extractKeywords(text: String): List<String> {
        val match = Regex("""关键词[：:]\s*(.+)""").find(text)
        return match?.groupValues?.get(1)
            ?.split("[,，、]".toRegex())
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }

    private fun extractSentiment(text: String): String? {
        val match = Regex("""情感[：:]\s*(\S+)""").find(text)
        return match?.groupValues?.get(1)
    }
}
