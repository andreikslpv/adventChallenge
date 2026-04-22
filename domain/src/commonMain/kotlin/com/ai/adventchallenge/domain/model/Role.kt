package com.ai.adventchallenge.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    @SerialName("system")
    SYSTEM,

    @SerialName("user")
    USER,

    @SerialName("assistant")
    ASSISTANT;

    fun value(): String = name.lowercase()
}
