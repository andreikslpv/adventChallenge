package com.ai.adventchallenge.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val timestamp: Long,
    val role: String,
    val content: String,
    val systemPrompt: String?,
    val agentId: String?,
    val characterCount: Int?,
    val tokenCount: Int?
) {
    companion object {
        fun fromDomain(
            id: String,
            sessionId: String,
            timestamp: Long,
            role: String,
            content: String,
            systemPrompt: String?,
            agentId: String?,
            characterCount: Int?,
            tokenCount: Int?
        ): MessageEntity {
            return MessageEntity(
                id = id,
                sessionId = sessionId,
                timestamp = timestamp,
                role = role,
                content = content,
                systemPrompt = systemPrompt,
                agentId = agentId,
                characterCount = characterCount,
                tokenCount = tokenCount
            )
        }
    }
}
