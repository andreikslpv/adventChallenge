package com.ai.adventchallenge.datasources

import com.ai.adventchallenge.db.dao.MessageDao
import com.ai.adventchallenge.db.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

class MessageDataSourceImpl(
    private val messageDao: MessageDao
) : MessageDataSource {
    override fun getMessagesBySessionId(sessionId: String): Flow<List<MessageEntity>> = 
        messageDao.getMessagesBySessionId(sessionId)

    override suspend fun getMessagesBySessionIdSync(sessionId: String): List<MessageEntity> = 
        messageDao.getMessagesBySessionIdSync(sessionId)

    override suspend fun insertMessage(message: MessageEntity) = messageDao.insertMessage(message)

    override suspend fun insertMessages(messages: List<MessageEntity>) = messageDao.insertMessages(messages)

    override suspend fun deleteMessagesBySessionId(sessionId: String) = 
        messageDao.deleteMessagesBySessionId(sessionId)

    override suspend fun deleteMessage(messageId: String) = messageDao.deleteMessage(messageId)

    override suspend fun deleteAllMessages() = messageDao.deleteAllMessages()
}
