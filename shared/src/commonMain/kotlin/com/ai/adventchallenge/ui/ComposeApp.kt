package com.ai.adventchallenge.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ai.adventchallenge.domain.model.Agent
import com.ai.adventchallenge.viewmodel.ChatViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ComposeApp() {
    MaterialTheme {
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
