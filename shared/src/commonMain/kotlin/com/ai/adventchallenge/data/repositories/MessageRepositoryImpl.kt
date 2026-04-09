package com.ai.adventchallenge.data.repositories

import com.ai.adventchallenge.data.api.dtos.ChatMessage
import com.ai.adventchallenge.data.datasources.MessageDataSource
import com.ai.adventchallenge.domain.repositories.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MessageRepositoryImpl(
    private val messageDataSource: MessageDataSource
) : MessageRepository {
    override fun getMessagesBySessionId(sessionId: String): Flow<List<ChatMessage>> = 
        messageDataSource.getMessagesBySessionId(sessionId).map { entities ->
            entities.map { entity ->
                entity.toDomainChatMessage()
            }
        }

    override suspend fun getMessagesBySessionIdSync(sessionId: String): List<ChatMessage> = 
        messageDataSource.getMessagesBySessionIdSync(sessionId).map { entity ->
            entity.toDomainChatMessage()
        }

    override suspend fun saveMessage(message: ChatMessage, sessionId: String) {
        val entity = MessageEntity.fromDomain(
            id = message.id,
            sessionId = sessionId,
            timestamp = message.timestamp,
            role = message.role,
            content = message.content,
            systemPrompt = message.systemPrompt.ifEmpty { null },
            agentId = message.agentId.ifEmpty { null },
            characterCount = if (message.characterCount > 0) message.characterCount else null,
            tokenCount = if (message.tokenCount > 0) message.tokenCount else null
        )
        messageDataSource.insertMessage(entity)
    }

    override suspend fun saveMessages(messages: List<ChatMessage>, sessionId: String) {
        val entities = messages.map { message ->
            MessageEntity.fromDomain(
                id = message.id,
                sessionId = sessionId,
                timestamp = message.timestamp,
                role = message.role,
                content = message.content,
                systemPrompt = message.systemPrompt.ifEmpty { null },
                agentId = message.agentId.ifEmpty { null },
                characterCount = if (message.characterCount > 0) message.characterCount else null,
                tokenCount = if (message.tokenCount > 0) message.tokenCount else null
            )
        }
        messageDataSource.insertMessages(entities)
    }

    override suspend fun deleteMessagesBySessionId(sessionId: String) = 
        messageDataSource.deleteMessagesBySessionId(sessionId)

    override suspend fun deleteMessage(messageId: String) = 
        messageDataSource.deleteMessage(messageId)

    override suspend fun deleteAllMessages() = 
        messageDataSource.deleteAllMessages()
}

private fun MessageEntity.toDomainChatMessage(): ChatMessage {
    return ChatMessage(
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
