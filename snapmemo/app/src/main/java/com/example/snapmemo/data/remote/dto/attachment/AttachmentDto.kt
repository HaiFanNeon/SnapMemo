package com.example.snapmemo.data.remote.dto.attachment

import com.google.gson.annotations.SerializedName

data class AttachmentDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("createTime")
    val createTime: String?,
    @SerializedName("filename")
    val filename: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("size")
    val size: String?,
    @SerializedName("externalLink")
    val externalLink: String?,
    @SerializedName("memo")
    val memo: String?
)
