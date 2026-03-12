package com.example.snapmemo.data.remote.api

import com.example.snapmemo.data.remote.dto.ai.AiSummaryRequest
import com.example.snapmemo.data.remote.dto.ai.AiSummaryResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming

interface AiServiceApi {

    /** 非流式：返回完整摘要 */
    @POST("v1/chat/completions")
    suspend fun generateSummary(@Body request: AiSummaryRequest): AiSummaryResponse

    /** 流式：通过 SSE 返回文本块（ResponseBody 由调用方手动读取） */
    @Streaming
    @POST("v1/chat/completions")
    suspend fun generateSummaryStream(@Body request: AiSummaryRequest): okhttp3.ResponseBody
}
