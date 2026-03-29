package com.ai.adventchallenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ai.adventchallenge.platform.ApiKeyProvider
import com.ai.adventchallenge.ui.AgentSettingsDialog
import com.ai.adventchallenge.ui.AppTheme
import com.ai.adventchallenge.ui.ChatScreen
import com.ai.adventchallenge.viewmodel.Agent
import com.ai.adventchallenge.viewmodel.ChatViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    private val apiKeyProvider = ApiKeyProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        apiKeyProvider.setApiKey(BuildConfig.API_KEY)
        
        setContent {
            AppTheme {
                val viewModel: ChatViewModel = koinViewModel()
                var selectedAgent by remember { mutableStateOf<Agent?>(null) }

                if (selectedAgent != null) {
                    AgentSettingsDialog(
                        agent = selectedAgent!!,
                        onDismiss = { selectedAgent = null },
                        onSettingsChanged = { settings ->
                            viewModel.updateAgentSettings(selectedAgent!!.id, settings)
                        }
                    )
                }

                ChatScreen(
                    viewModel = viewModel,
                    showSettingsDialog = { selectedAgent = it }
                )
            }
        }
    }
}
