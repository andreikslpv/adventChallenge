package com.ai.adventchallenge.domain.repository

import com.ai.adventchallenge.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getMessagesBySessionId(sessionId: String): Flow<List<Message>>
    suspend fun getMessagesBySessionIdSync(sessionId: String): List<Message>
    suspend fun getMessageById(messageId: String): Message?
    suspend fun saveMessage(message: Message, sessionId: String)
    suspend fun saveMessages(messages: List<Message>, sessionId: String)
    suspend fun deleteMessagesBySessionId(sessionId: String)
    suspend fun deleteMessage(messageId: String)
    suspend fun deleteAllMessages()
}
