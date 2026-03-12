package com.example.snapmemo.data.remote.dto.memo

import com.google.gson.annotations.SerializedName

data class CreateMemoRequest(
    @SerializedName("content")
    val content: String,
    @SerializedName("state")
    val state: String = "NORMAL",
    @SerializedName("visibility")
    val visibility: String = "PRIVATE",
    @SerializedName("pinned")
    val pinned: Boolean = false,
    @SerializedName("attachments")
    val attachments: List<AttachmentRefDto>? = null,
    @SerializedName("location")
    val location: LocationDto? = null
)
