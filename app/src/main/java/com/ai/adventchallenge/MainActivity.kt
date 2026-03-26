package com.ai.adventchallenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ai.adventchallenge.ui.ChatScreen
import com.ai.adventchallenge.ui.SettingsScreen
import com.ai.adventchallenge.ui.theme.AdventChallengeTheme
import com.ai.adventchallenge.viewmodel.ChatViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdventChallengeTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ChatViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var showSettings by remember { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            showError = true
        }
    }

    LaunchedEffect(showError) {
        if (showError && uiState.error != null) {
            snackbarHostState.showSnackbar(
                message = uiState.error ?: "Ошибка",
                actionLabel = "ОК"
            )
            viewModel.clearError()
            showError = false
        }
    }

    if (showSettings != null) {
        val agent = uiState.agents.find { it.id == showSettings }
        if (agent != null) {
            SettingsScreen(
                settings = agent.settings,
                onBack = { showSettings = null },
                onSave = { settings ->
                    viewModel.updateAgentSettings(showSettings!!, settings)
                    showSettings = null
                }
            )
        }
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) {
            ChatScreen(
                agents = uiState.agents,
                selectedAgentId = uiState.selectedAgentId,
                messages = uiState.messages,
                isLoading = uiState.isLoading,
                isSendingToAll = uiState.isSendingToAll,
                onSelectAgent = { viewModel.selectAgent(it) },
                onAddAgent = { viewModel.addAgent() },
                onRemoveAgent = { viewModel.removeAgent(it) },
                onOpenSettings = { showSettings = it },
                onSendMessage = { viewModel.sendMessage(it) },
                onSendToAll = { viewModel.sendMessageToAll(it) },
                onClearSession = { viewModel.clearSession() }
            )
        }
    }
}
