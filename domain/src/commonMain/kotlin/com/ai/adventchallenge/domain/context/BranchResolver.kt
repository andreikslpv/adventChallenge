package com.ai.adventchallenge.domain.context

import com.ai.adventchallenge.domain.model.Message

interface BranchResolver {
    suspend fun buildBranch(lastMessageId: String?): List<Message>
}
