package com.example.snapmemo.data.remote.dto.ai

import com.google.gson.annotations.SerializedName

data class AiSummaryRequest(
    @SerializedName("model") val model: String = "gpt-4o-mini",
    @SerializedName("messages") val messages: List<AiMessage>,
    @SerializedName("stream") val stream: Boolean = false,
    @SerializedName("max_tokens") val maxTokens: Int = 512,
    @SerializedName("temperature") val temperature: Double = 0.7
)

data class AiMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class AiSummaryResponse(
    @SerializedName("id") val id: String,
    @SerializedName("object") val objectType: String,
    @SerializedName("choices") val choices: List<AiChoice>
)

data class AiChoice(
    @SerializedName("index") val index: Int,
    @SerializedName("message") val message: AiMessage?,
    @SerializedName("delta") val delta: AiDelta?,
    @SerializedName("finish_reason") val finishReason: String?
)

data class AiDelta(
    @SerializedName("content") val content: String?
)
