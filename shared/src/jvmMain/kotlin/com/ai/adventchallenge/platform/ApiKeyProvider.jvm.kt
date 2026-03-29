package com.ai.adventchallenge.platform

import java.io.File
import java.io.FileInputStream
import java.util.Properties

actual class ApiKeyProvider {
    actual fun getApiKey(): String {
        val properties = Properties()
        val localPropertiesFile = File("local.properties")
        
        if (localPropertiesFile.exists()) {
            FileInputStream(localPropertiesFile).use { stream ->
                properties.load(stream)
            }
            return properties.getProperty("zaiApiKey", "")
        }
        
        return ""
    }
}
