package com.ai.adventchallenge.platform

expect class ApiKeyProvider() {
    fun getApiKey(): String
}
