package com.ai.adventchallenge.data.datasource

import com.ai.adventchallenge.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageDataSource {
    fun getMessagesBySessionId(sessionId: String): Flow<List<Message>>
    suspend fun getMessagesBySessionIdSync(sessionId: String): List<Message>
    suspend fun getMessageById(messageId: String): Message?
    suspend fun insertMessage(message: Message, sessionId: String)
    suspend fun insertMessages(messages: List<Message>, sessionId: String)
    suspend fun deleteMessagesBySessionId(sessionId: String)
    suspend fun deleteMessage(messageId: String)
    suspend fun deleteAllMessages()
}
