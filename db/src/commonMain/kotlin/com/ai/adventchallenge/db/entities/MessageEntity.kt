package com.ai.adventchallenge.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["parentId"]),
        Index(value = ["sessionId"])
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val parentId: String? = null,
    val timestamp: Long,
    val role: String,
    val content: String,
    val systemPrompt: String?,
    val agentId: String?,
    val characterCount: Int?,
    val tokenCount: Int?,
    val outgoingTokenCount: Int?
)
