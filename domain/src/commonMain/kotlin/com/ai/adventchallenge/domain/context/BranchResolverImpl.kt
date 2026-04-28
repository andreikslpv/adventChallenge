package com.ai.adventchallenge.domain.context

import com.ai.adventchallenge.domain.model.Message
import com.ai.adventchallenge.domain.repository.MessageRepository

class BranchResolverImpl(
    private val messageRepository: MessageRepository
) : BranchResolver {
    override suspend fun buildBranch(lastMessageId: String?): List<Message> {
        if (lastMessageId == null) return emptyList()

        val result = mutableListOf<Message>()
        var currentId: String? = lastMessageId

        while (currentId != null) {
            val message = messageRepository.getMessageById(currentId) ?: break
            result.add(message)
            currentId = message.parentId
        }

        return result.reversed()
    }
}
