package com.example.snapmemo.data.remote.dto.memo

import com.google.gson.annotations.SerializedName

data class UpdateMemoRequest(
    @SerializedName("content")
    val content: String?,
    @SerializedName("state")
    val state: String?,
    @SerializedName("visibility")
    val visibility: String?,
    @SerializedName("pinned")
    val pinned: Boolean?
)
