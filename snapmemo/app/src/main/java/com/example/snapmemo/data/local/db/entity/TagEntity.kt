package com.example.snapmemo.data.local.db.entity

import androidx.room.Entity

@Entity(
    tableName = "memo_tags",
    primaryKeys = ["memoId", "tag"]
)
data class TagEntity(
    val memoId: String,
    val tag: String
)
