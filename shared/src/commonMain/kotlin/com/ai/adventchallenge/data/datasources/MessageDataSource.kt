package com.ai.adventchallenge.data.datasources

import com.ai.adventchallenge.db.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

interface MessageDataSource {
    fun getMessagesBySessionId(sessionId: String): Flow<List<MessageEntity>>
    suspend fun getMessagesBySessionIdSync(sessionId: String): List<MessageEntity>
    suspend fun insertMessage(message: MessageEntity)
    suspend fun insertMessages(messages: List<MessageEntity>)
    suspend fun deleteMessagesBySessionId(sessionId: String)
    suspend fun deleteMessage(messageId: String)
    suspend fun deleteAllMessages()
}
