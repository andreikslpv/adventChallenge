package com.ai.adventchallenge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ai.adventchallenge.domain.model.Agent
import com.ai.adventchallenge.domain.model.Message
import com.ai.adventchallenge.platform.copyToClipboard
import com.ai.adventchallenge.platform.getClipboardContext
import com.ai.adventchallenge.ui.components.AgentsRow
import com.ai.adventchallenge.ui.components.BranchSelectorDialog
import com.ai.adventchallenge.ui.components.MessageBubble
import com.ai.adventchallenge.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

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
    var showSessionSettings by remember { mutableStateOf(false) }
    var branchSelectorParentId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    var alternativeBranches by remember { mutableStateOf<List<Message>>(emptyList()) }

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
                    IconButton(onClick = { showSessionSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки сессии")
                    }
                    TextButton(onClick = { viewModel.clearSession() }) {
                        Text("Очистить")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .width(250.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFF5F5F5))
                    .padding(8.dp)
            ) {
                Button(
                    onClick = { viewModel.createNewSession() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New session")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.sessions) { session ->
                        val isSelected = session.id == uiState.selectedSessionId
                        val hasBranches = uiState.branchPoints.isNotEmpty() && session.id == uiState.selectedSessionId
                        val backgroundColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(backgroundColor)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF2196F3) else Color(0xFFE0E0E0),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectSession(session.id) }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (hasBranches) {
                                            Text(
                                                text = "\uD83C\uDF3F ",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Text(
                                            text = session.name,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = formatTimestamp(session.createdAt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.deleteSession(session.id) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete session",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8E8E8))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("Токены: ${viewModel.getSessionTokenCount()} / ${viewModel.getSelectedAgentMaxContextWindow()}")
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val currentBranchIds = remember(uiState.messages) {
                        uiState.messages.map { it.id }.toSet()
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.messages) { message ->
                            val childCount = uiState.branchPoints[message.id] ?: 0
                            MessageBubble(
                                message = message,
                                agent = uiState.agents.find { it.id == message.agentId },
                                branchChildCount = childCount,
                                isBranchPoint = childCount > 1,
                                onLongPress = { text ->
                                    copyToClipboard(text, context)
                                },
                                onReplyFromHere = {
                                    viewModel.switchBranch(message.id)
                                },
                                onShowBranches = if (childCount > 1) {
                                    {
                                        scope.launch {
                                            alternativeBranches = viewModel.getAlternativeBranches(message.id)
                                            branchSelectorParentId = message.id
                                        }
                                    }
                                } else null
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
                            placeholder = { Text("Enter message...") },
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
                                Text("All")
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
                            Text("Отправить")
                        }
                    }
                }
            }
        }
    }

    val currentSession = viewModel.getCurrentSession()
    if (showSessionSettings && currentSession != null) {
        SessionSettingsDialog(
            session = currentSession,
            onDismiss = { showSessionSettings = false },
            onSettingsChanged = { settings ->
                viewModel.updateSessionContextSettings(settings)
            }
        )
    }

    if (branchSelectorParentId != null) {
        BranchSelectorDialog(
            branches = alternativeBranches,
            currentBranchMessageIds = remember(uiState.messages, alternativeBranches) {
                val currentIds = uiState.messages.map { it.id }.toSet()
                currentIds
            },
            onSelectBranch = { messageId ->
                viewModel.switchBranch(messageId)
                branchSelectorParentId = null
                alternativeBranches = emptyList()
            },
            onDismiss = {
                branchSelectorParentId = null
                alternativeBranches = emptyList()
            }
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.day.toString().padStart(2, '0')}.${dateTime.month.number.toString().padStart(2, '0')}.${dateTime.year} ${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
}
