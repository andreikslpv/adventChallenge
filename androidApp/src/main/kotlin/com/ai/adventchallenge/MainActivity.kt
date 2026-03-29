package com.ai.adventchallenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ai.adventchallenge.platform.ApiKeyProvider
import com.ai.adventchallenge.ui.ComposeApp

class MainActivity : ComponentActivity() {
    private val apiKeyProvider = ApiKeyProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        apiKeyProvider.setApiKey(BuildConfig.API_KEY)

        setContent {
            ComposeApp()
        }
    }
}
