package com.ai.adventchallenge

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.CanvasBasedWindow
import com.ai.adventchallenge.ui.AgentSettingsDialog
import com.ai.adventchallenge.ui.AppTheme
import com.ai.adventchallenge.ui.ChatScreen
import com.ai.adventchallenge.viewmodel.Agent
import com.ai.adventchallenge.viewmodel.ChatViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

fun main() {
    initKoin()
    
    CanvasBasedWindow(canvasElementId = "root") {
        AppTheme {
            val viewModel by inject<ChatViewModel>()
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

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                ChatScreen(
                    viewModel = viewModel,
                    showSettingsDialog = { selectedAgent = it }
                )
            }
        }
    }
}
