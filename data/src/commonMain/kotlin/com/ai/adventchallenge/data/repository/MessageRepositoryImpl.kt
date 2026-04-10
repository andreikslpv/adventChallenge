package com.ai.adventchallenge.data.repository

import com.ai.adventchallenge.data.datasource.MessageDataSource
import com.ai.adventchallenge.domain.model.Message
import com.ai.adventchallenge.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow

class MessageRepositoryImpl(
    private val messageDataSource: MessageDataSource
) : MessageRepository {
    override fun getMessagesBySessionId(sessionId: String): Flow<List<Message>> = 
        messageDataSource.getMessagesBySessionId(sessionId)

    override suspend fun getMessagesBySessionIdSync(sessionId: String): List<Message> = 
        messageDataSource.getMessagesBySessionIdSync(sessionId)

    override suspend fun saveMessage(message: Message, sessionId: String) {
        messageDataSource.insertMessage(message, sessionId)
    }

    override suspend fun saveMessages(messages: List<Message>, sessionId: String) {
        messageDataSource.insertMessages(messages, sessionId)
    }

    override suspend fun deleteMessagesBySessionId(sessionId: String) = 
        messageDataSource.deleteMessagesBySessionId(sessionId)

    override suspend fun deleteMessage(messageId: String) = 
        messageDataSource.deleteMessage(messageId)

    override suspend fun deleteAllMessages() = 
        messageDataSource.deleteAllMessages()
}
