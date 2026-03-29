package com.ai.adventchallenge.platform

actual class ApiKeyProvider {
    private var apiKey: String? = null

    fun setApiKey(key: String) {
        apiKey = key
    }

    actual fun getApiKey(): String {
        return apiKey ?: ""
    }
}
