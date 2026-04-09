package com.ai.adventchallenge.domain.repositories

import com.ai.adventchallenge.data.api.dtos.ChatMessage
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getMessagesBySessionId(sessionId: String): Flow<List<ChatMessage>>
    suspend fun getMessagesBySessionIdSync(sessionId: String): List<ChatMessage>
    suspend fun saveMessage(message: ChatMessage, sessionId: String)
    suspend fun saveMessages(messages: List<ChatMessage>, sessionId: String)
    suspend fun deleteMessagesBySessionId(sessionId: String)
    suspend fun deleteMessage(messageId: String)
    suspend fun deleteAllMessages()
}
