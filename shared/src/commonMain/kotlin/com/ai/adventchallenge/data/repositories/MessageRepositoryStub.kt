package com.ai.adventchallenge.data.repositories

import com.ai.adventchallenge.data.api.dtos.ChatMessage
import com.ai.adventchallenge.domain.repositories.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class MessageRepositoryStub : MessageRepository {
    private val sessionMessages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())

    override fun getMessagesBySessionId(sessionId: String): Flow<List<ChatMessage>> = 
        sessionMessages.map { it[sessionId] ?: emptyList() }

    override suspend fun getMessagesBySessionIdSync(sessionId: String): List<ChatMessage> = 
        sessionMessages.value[sessionId] ?: emptyList()

    override suspend fun saveMessage(message: ChatMessage, sessionId: String) {
        val current = sessionMessages.value.toMutableMap()
        val messages = current[sessionId]?.toMutableList() ?: mutableListOf()
        val index = messages.indexOfFirst { it.id == message.id }
        if (index >= 0) {
            messages[index] = message
        } else {
            messages.add(message)
        }
        current[sessionId] = messages
        sessionMessages.value = current
    }

    override suspend fun saveMessages(messages: List<ChatMessage>, sessionId: String) {
        val current = sessionMessages.value.toMutableMap()
        val existingMessages = current[sessionId]?.toMutableList() ?: mutableListOf()
        messages.forEach { message ->
            val index = existingMessages.indexOfFirst { it.id == message.id }
            if (index >= 0) {
                existingMessages[index] = message
            } else {
                existingMessages.add(message)
            }
        }
        current[sessionId] = existingMessages
        sessionMessages.value = current
    }

    override suspend fun deleteMessagesBySessionId(sessionId: String) {
        val current = sessionMessages.value.toMutableMap()
        current.remove(sessionId)
        sessionMessages.value = current
    }

    override suspend fun deleteMessage(messageId: String) {
        val current = sessionMessages.value.toMutableMap()
        current.forEach { (sessionId, messages) ->
            current[sessionId] = messages.filterNot { it.id == messageId }
        }
        sessionMessages.value = current
    }

    override suspend fun deleteAllMessages() {
        sessionMessages.value = emptyMap()
    }
}
