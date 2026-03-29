package com.ai.adventchallenge.platform

import java.io.File
import java.io.FileInputStream
import java.util.Properties

actual class ApiKeyProvider {
    actual fun getApiKey(): String {
        val properties = Properties()
        // Try multiple possible locations for local.properties
        val possiblePaths = listOf(
            "local.properties",
            "../local.properties",
            "../../local.properties"
        )
        
        for (path in possiblePaths) {
            val file = File(path)
            if (file.exists()) {
                try {
                    FileInputStream(file).use { stream ->
                        properties.load(stream)
                    }
                    val apiKey = properties.getProperty("zaiApiKey", "")
                    if (apiKey.isNotBlank()) {
                        return apiKey
                    }
                } catch (e: Exception) {
                    // Continue to next path
                }
            }
        }
        
        return ""
    }
}
