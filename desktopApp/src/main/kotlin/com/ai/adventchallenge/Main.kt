package com.ai.adventchallenge

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.ai.adventchallenge.di.initKoin
import com.ai.adventchallenge.ui.AgentSettingsDialog
import com.ai.adventchallenge.ui.AppTheme
import com.ai.adventchallenge.ui.ChatScreen
import com.ai.adventchallenge.viewmodel.Agent
import com.ai.adventchallenge.viewmodel.ChatViewModel

fun main() {
    val koinApp = initKoin()
    
    application {
        Window(onCloseRequest = ::exitApplication) {
            AppTheme {
                val viewModel = koinApp.koin.get<ChatViewModel>()
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
