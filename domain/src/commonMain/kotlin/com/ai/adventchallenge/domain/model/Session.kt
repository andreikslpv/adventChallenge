package com.ai.adventchallenge.domain.model

import kotlin.time.Clock

data class Session(
    val id: String,
    val name: String,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val selectedAgentId: String = ""
)