package com.example.snapmemo.data.remote.dto.memo

import com.google.gson.annotations.SerializedName

data class MemoDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("state")
    val state: String,
    @SerializedName("creator")
    val creator: String?,
    @SerializedName("createTime")
    val createTime: String?,
    @SerializedName("updateTime")
    val updateTime: String?,
    @SerializedName("displayTime")
    val displayTime: String?,
    @SerializedName("content")
    val content: String,
    @SerializedName("visibility")
    val visibility: String,
    @SerializedName("tags")
    val tags: List<String>?,
    @SerializedName("pinned")
    val pinned: Boolean,
    @SerializedName("attachments")
    val attachments: List<AttachmentRefDto>?,
    @SerializedName("property")
    val property: MemoPropertyDto?,
    @SerializedName("snippet")
    val snippet: String?,
    @SerializedName("location")
    val location: LocationDto?
)

data class AttachmentRefDto(
    @SerializedName("name")
    val name: String
)

data class MemoPropertyDto(
    @SerializedName("hasLink")
    val hasLink: Boolean,
    @SerializedName("hasTaskList")
    val hasTaskList: Boolean,
    @SerializedName("hasCode")
    val hasCode: Boolean,
    @SerializedName("hasIncompleteTasks")
    val hasIncompleteTasks: Boolean
)

data class LocationDto(
    @SerializedName("placeholder")
    val placeholder: String?,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?
)
