package com.ai.adventchallenge.datasources

import com.ai.adventchallenge.data.datasource.MessageDataSource
import com.ai.adventchallenge.db.dao.MessageDao
import com.ai.adventchallenge.db.entities.MessageEntity
import com.ai.adventchallenge.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MessageDataSourceImpl(
    private val messageDao: MessageDao
) : MessageDataSource {
    override fun getMessagesBySessionId(sessionId: String): Flow<List<Message>> =
        messageDao.getMessagesBySessionId(sessionId).map { entities ->
            entities.map { it.toDomainMessage() }
        }

    override suspend fun getMessagesBySessionIdSync(sessionId: String): List<Message> =
        messageDao.getMessagesBySessionIdSync(sessionId).map { entity ->
            entity.toDomainMessage()
        }

    override suspend fun insertMessage(message: Message) {
        val entity = message.toEntity()
        messageDao.insertMessage(entity)
    }

    override suspend fun insertMessages(messages: List<Message>) {
        val entities = messages.map { it.toEntity() }
        messageDao.insertMessages(entities)
    }

    override suspend fun deleteMessagesBySessionId(sessionId: String) =
        messageDao.deleteMessagesBySessionId(sessionId)

    override suspend fun deleteMessage(messageId: String) =
        messageDao.deleteMessage(messageId)

    override suspend fun deleteAllMessages() =
        messageDao.deleteAllMessages()
}

private fun MessageEntity.toDomainMessage(): Message {
    return Message(
        id = id,
        role = role,
        content = content,
        systemPrompt = systemPrompt ?: "",
        agentId = agentId ?: "",
        characterCount = characterCount ?: 0,
        tokenCount = tokenCount ?: 0,
        timestamp = timestamp
    )
}

private fun Message.toEntity(): MessageEntity {
    return MessageEntity(
        id = id,
        sessionId = "",
        timestamp = timestamp,
        role = role,
        content = content,
        systemPrompt = systemPrompt.ifEmpty { null },
        agentId = agentId.ifEmpty { null },
        characterCount = if (characterCount > 0) characterCount else null,
        tokenCount = if (tokenCount > 0) tokenCount else null
    )
}