package com.ai.adventchallenge.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ai.adventchallenge.domain.model.Agent

@Composable
fun AgentsRow(
    agents: List<Agent>,
    selectedAgentId: String,
    savedAgents: List<Agent>,
    showAgentSelector: Boolean,
    onAgentSelected: (String) -> Unit,
    onAgentSettings: (Agent) -> Unit,
    onAddAgent: () -> Unit,
    onRemoveAgent: (String) -> Unit,
    onCloseAgent: (String) -> Unit,
    onHideAgentSelector: () -> Unit,
    onSelectSavedAgent: (String) -> Unit
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
                onSelect = { onAgentSelected(agent.id) },
                onConfigure = { onAgentSettings(agent) },
                onDelete = { onRemoveAgent(agent.id) },
                onClose = { onCloseAgent(agent.id) }
            )
        }
        item {
            Box {
                AddAgentButton(onAdd = onAddAgent)
                AgentSelectorDropdown(
                    expanded = showAgentSelector,
                    savedAgents = savedAgents,
                    onDismiss = onHideAgentSelector,
                    onSelectAgent = onSelectSavedAgent
                )
            }
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
    onDelete: () -> Unit,
    onClose: () -> Unit
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
                IconButton(
                    onClick = onConfigure,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Настроить",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    enabled = canDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = if (canDelete) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.3f
                        )
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

@Composable
fun AgentSelectorDropdown(
    expanded: Boolean,
    savedAgents: List<Agent>,
    onDismiss: () -> Unit,
    onSelectAgent: (String) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.width(200.dp)
    ) {
        DropdownMenuItem(
            text = { Text("Новый агент") },
            onClick = {
                onSelectAgent(null.toString())
                onDismiss()
            }
        )
        
        if (savedAgents.isNotEmpty()) {
            savedAgents.forEach { agent ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = agent.settings.systemPrompt.ifEmpty { "Без промпта" },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    onClick = {
                        onSelectAgent(agent.id)
                        onDismiss()
                    }
                )
            }
        }
    }
}
