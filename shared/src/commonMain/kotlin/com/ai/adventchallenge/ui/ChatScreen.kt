package com.ai.adventchallenge.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.ai.adventchallenge.viewmodel.Agent
import com.ai.adventchallenge.viewmodel.ChatViewModel

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    showSettingsDialog: (Agent) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

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
                AgentSelector(
                    agents = uiState.agents,
                    selectedAgentId = uiState.selectedAgentId,
                    onAgentSelected = { viewModel.selectAgent(it) },
                    onAgentSettings = { showSettingsDialog(it) },
                    onAddAgent = { viewModel.addAgent() },
                    onRemoveAgent = { viewModel.removeAgent(it) }
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
                        enabled = !uiState.isLoading && !uiState.isSendingToAll
                    )

                    if (uiState.agents.size > 1) {
                        Button(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessageToAll(inputText)
                                    inputText = ""
                                }
                            },
                            enabled = inputText.isNotBlank() && !uiState.isLoading && !uiState.isSendingToAll
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
                        enabled = inputText.isNotBlank() && !uiState.isLoading && !uiState.isSendingToAll
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
            if (uiState.isLoading || uiState.isSendingToAll) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.messages) { message ->
                        MessageItem(
                            message = message,
                            agent = uiState.agents.find { it.id == message.agentId }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AgentSelector(
    agents: List<Agent>,
    selectedAgentId: String,
    onAgentSelected: (String) -> Unit,
    onAgentSettings: (Agent) -> Unit,
    onAddAgent: () -> Unit,
    onRemoveAgent: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        agents.forEach { agent ->
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (agent.id == selectedAgentId) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                onClick = { onAgentSelected(agent.id) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Агент ${agent.id.takeLast(4)}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row {
                        Button(
                            onClick = { onAgentSettings(agent) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text("⚙️", modifier = Modifier.size(16.dp))
                        }
                        if (agents.size > 1) {
                            Button(
                                onClick = { onRemoveAgent(agent.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text("🗑️", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .width(80.dp)
                .height(80.dp),
            onClick = onAddAgent
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

@Composable
fun MessageItem(
    message: com.ai.adventchallenge.api.dtos.ChatMessage,
    agent: Agent?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        if (message.role == "assistant") {
            agent?.let {
                Text(
                    text = "Системный промпт: ${it.settings.systemPrompt}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (message.characterCount > 0 || message.tokenCount > 0) {
                Text(
                    text = "Символов: ${message.characterCount}, Токенов: ${message.tokenCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (message.role == "user") {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
