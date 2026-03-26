package com.ai.adventchallenge.ui

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ai.adventchallenge.api.dtos.ChatMessage
import com.ai.adventchallenge.viewmodel.Agent
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    agents: List<Agent>,
    selectedAgentId: String,
    messages: List<ChatMessage>,
    isLoading: Boolean,
    isSendingToAll: Boolean,
    onSelectAgent: (String) -> Unit,
    onAddAgent: () -> Unit,
    onRemoveAgent: (String) -> Unit,
    onOpenSettings: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onSendToAll: (String) -> Unit,
    onClearSession: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Чат") },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Меню")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Очистить текущую сессию") },
                                onClick = {
                                    onClearSession()
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AgentsRow(
                agents = agents,
                selectedAgentId = selectedAgentId,
                onSelectAgent = onSelectAgent,
                onAddAgent = onAddAgent,
                onRemoveAgent = onRemoveAgent,
                onOpenSettings = onOpenSettings
            )

            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    val clipboardManager = LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    MessageBubble(
                        message = message,
                        onLongPress = { text ->
                            android.content.ClipData.newPlainText("message", text).let { clip ->
                                clipboardManager.setPrimaryClip(clip)
                            }
                        }
                    )
                }
                if (isLoading || isSendingToAll) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Введите сообщение...") },
                    maxLines = 4
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (agents.size > 1) {
                    TextButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendToAll(inputText.trim())
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !isLoading && !isSendingToAll
                    ) {
                        Text("Отправить всем")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText.trim())
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !isLoading && !isSendingToAll
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Отправить")
                }
            }
        }
    }
}

private val json = Json { prettyPrint = true }

@Composable
fun AgentsRow(
    agents: List<Agent>,
    selectedAgentId: String,
    onSelectAgent: (String) -> Unit,
    onAddAgent: () -> Unit,
    onRemoveAgent: (String) -> Unit,
    onOpenSettings: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(agents) { agent ->
            val isSelected = agent.id == selectedAgentId
            AgentCard(
                agent = agent,
                isSelected = isSelected,
                canDelete = agents.size > 1,
                onSelect = { onSelectAgent(agent.id) },
                onConfigure = { onOpenSettings(agent.id) },
                onDelete = { onRemoveAgent(agent.id) }
            )
        }
        item {
            AddAgentButton(onAdd = onAddAgent)
        }
    }
}

@Composable
fun AgentCard(
    agent: Agent,
    isSelected: Boolean,
    canDelete: Boolean,
    onSelect: () -> Unit,
    onConfigure: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        onClick = onSelect
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = agent.settings.systemPrompt.ifEmpty { "Без промпта" },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onConfigure,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Настроить", style = MaterialTheme.typography.bodySmall)
                }
                IconButton(
                    onClick = onDelete,
                    enabled = canDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        modifier = Modifier.size(20.dp),
                        tint = if (canDelete) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun AddAgentButton(onAdd: () -> Unit) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .padding(vertical = 4.dp),
        onClick = onAdd,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Добавить агента",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(message: ChatMessage, onLongPress: (String) -> Unit) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    val prettyJson = remember(message.content) {
        try {
            json.parseToJsonElement(message.content).toString()
        } catch (_: Exception) {
            null
        }
    }

    val displayContent = prettyJson ?: message.content

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Surface(
            color = color,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .combinedClickable(
                    onClick = { },
                    onLongClick = { onLongPress(displayContent) }
                )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                if (!isUser && message.systemPrompt.isNotEmpty()) {
                    Text(
                        text = "${message.systemPrompt}:",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                }
                Text(
                    text = displayContent,
                    color = contentColor
                )
                if (!isUser && (message.characterCount > 0 || message.tokenCount > 0)) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (message.characterCount > 0) {
                            Text(
                                text = "${message.characterCount} символов",
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.7f)
                            )
                        }
                        if (message.tokenCount > 0) {
                            Text(
                                text = "${message.tokenCount} токенов",
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}
