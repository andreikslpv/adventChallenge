package com.ai.adventchallenge.domain.context

import kotlinx.serialization.Serializable

@Serializable
data class Facts(
    val goal: String? = null,
    val constraints: String? = null,
    val preferences: String? = null,
    val decisions: String? = null,
    val requirements: String? = null,
    val other: String? = null
)
