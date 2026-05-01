package com.ai.adventchallenge.domain.context

import com.ai.adventchallenge.domain.model.Message
import com.ai.adventchallenge.domain.repository.MessageRepository

class BranchResolverImpl(
    private val messageRepository: MessageRepository
) : BranchResolver {
    override suspend fun buildBranch(lastMessageId: String?): List<Message> {
        if (lastMessageId == null) return emptyList()

        val backward = mutableListOf<Message>()
        var currentId: String? = lastMessageId

        while (currentId != null) {
            val message = messageRepository.getMessageById(currentId) ?: break
            backward.add(message)
            currentId = message.parentId
        }

        val baseChain = backward.reversed()
        if (baseChain.isEmpty()) return emptyList()

        val forward = buildForwardChain(baseChain.last())

        return baseChain + forward
    }

    private suspend fun buildForwardChain(start: Message): List<Message> {
        val result = mutableListOf<Message>()
        var current = start

        while (true) {
            val children = messageRepository.getMessagesByParentId(current.id)
            if (children.isEmpty()) break
            val next = children.first()
            result.add(next)
            current = next
        }

        return result
    }
}
