package com.example.snapmemo.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_cache")
data class AiCacheEntity(
    @PrimaryKey
    val memoId: String,
    val summary: String,
    val keywords: String,
    val sentiment: String?,
    val generatedAt: Long,
    val modelVersion: String
)
