package com.ai.adventchallenge.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ai.adventchallenge.domain.model.Agent
import com.ai.adventchallenge.platform.copyToClipboard
import com.ai.adventchallenge.platform.getClipboardContext
import com.ai.adventchallenge.ui.components.AgentsRow
import com.ai.adventchallenge.ui.components.MessageBubble
import com.ai.adventchallenge.viewmodel.ChatViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    showSettingsDialog: (Agent) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = getClipboardContext()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Chat") },
                actions = {
                    TextButton(onClick = { viewModel.clearSession() }) {
                        Text("Очистить")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                AgentsRow(
                    agents = uiState.agents,
                    selectedAgentId = uiState.selectedAgentId,
                    savedAgents = uiState.savedAgents,
                    showAgentSelector = uiState.showAgentSelector,
                    onAgentSelected = { viewModel.selectAgent(it) },
                    onAgentSettings = { showSettingsDialog(it) },
                    onAddAgent = { viewModel.showAgentSelector() },
                    onRemoveAgent = { viewModel.removeAgent(it) },
                    onCloseAgent = { viewModel.closeAgent(it) },
                    onHideAgentSelector = { viewModel.hideAgentSelector() },
                    onSelectSavedAgent = { agentId ->
                        if (agentId == "null") {
                            viewModel.addAgent(null)
                        } else {
                            viewModel.addAgent(agentId)
                        }
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Введите сообщение...") },
                        enabled = !uiState.isLoading && !uiState.isSendingToAll && uiState.selectedAgentId.isNotEmpty()
                    )

                    if (uiState.agents.size > 1) {
                        Button(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessageToAll(inputText)
                                    inputText = ""
                                }
                            },
                            enabled = inputText.isNotBlank() && !uiState.isLoading && !uiState.isSendingToAll && uiState.selectedAgentId.isNotEmpty()
                        ) {
                            Text("Всем")
                        }
                    }

                    Button(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !uiState.isLoading && !uiState.isSendingToAll && uiState.selectedAgentId.isNotEmpty()
                    ) {
                        Text("📤")
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.messages) { message ->
                    MessageBubble(
                        message = message,
                        agent = uiState.agents.find { it.id == message.agentId },
                        onLongPress = { text ->
                            copyToClipboard(text, context)
                        }
                    )
                }
            }
            
            if (uiState.isLoading || uiState.isSendingToAll) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
            }
        }
    }
}
