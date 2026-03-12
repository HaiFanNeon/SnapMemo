package com.example.snapmemo.data.remote.dto.attachment

import com.google.gson.annotations.SerializedName

data class CreateAttachmentRequest(
    @SerializedName("filename")
    val filename: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("memo")
    val memo: String?
)
