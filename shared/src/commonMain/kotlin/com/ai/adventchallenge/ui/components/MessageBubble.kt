package com.ai.adventchallenge.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ai.adventchallenge.domain.model.Agent
import com.ai.adventchallenge.domain.model.Message
import com.ai.adventchallenge.domain.model.Role
import kotlinx.serialization.json.Json

private val json = Json { prettyPrint = true }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    agent: Agent?,
    branchChildCount: Int = 0,
    isBranchPoint: Boolean = false,
    onLongPress: (String) -> Unit,
    onReplyFromHere: (() -> Unit)? = null,
    onShowBranches: (() -> Unit)? = null
) {
    val isUser = message.role == Role.USER
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val color =
        if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor =
        if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    val prettyJson = remember(message.content) {
        try {
            json.parseToJsonElement(message.content).toString()
        } catch (_: Exception) {
            null
        }
    }

    val displayContent = prettyJson ?: message.content
    var showMenu by remember { mutableStateOf(false) }

    Column {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = alignment
        ) {
            BoxWithConstraints {
                val maxWidth = maxWidth * 2 / 3
                val modelName = agent?.settings?.selectedModel?.modelName ?: ""
                val systemPromptText = if (modelName.isNotEmpty()) {
                    "$modelName: ${message.systemPrompt}"
                } else {
                    message.systemPrompt
                }

                Surface(
                    color = if (isBranchPoint) color.copy(alpha = 0.85f) else color,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .widthIn(max = maxWidth)
                        .combinedClickable(
                            onClick = { },
                            onLongClick = { onLongPress(displayContent) }
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        if (!isUser && systemPromptText.isNotEmpty()) {
                            Text(
                                text = "$systemPromptText:",
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
                        val hasStats = if (isUser) {
                            message.characterCount > 0 || message.outgoingTokenCount > 0
                        } else {
                            message.characterCount > 0 || message.tokenCount > 0
                        }

                        if (hasStats || onReplyFromHere != null) {
                            Spacer(modifier = Modifier.size(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (isUser) {
                                    if (message.outgoingTokenCount > 0) {
                                        Text(
                                            text = "Исходящие: ${message.outgoingTokenCount} токенов",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = contentColor.copy(alpha = 0.7f)
                                        )
                                    }
                                    if (message.characterCount > 0) {
                                        Text(
                                            text = "${message.characterCount} символов",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = contentColor.copy(alpha = 0.7f)
                                        )
                                    }
                                } else {
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

                                if (onReplyFromHere != null) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable { showMenu = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = "Actions",
                                            modifier = Modifier.size(16.dp),
                                            tint = contentColor
                                        )
                                        DropdownMenu(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Reply from here") },
                                                onClick = {
                                                    showMenu = false
                                                    onReplyFromHere()
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.Reply,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

        if (branchChildCount > 1 && onShowBranches != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 2.dp, bottom = 4.dp)
                    .combinedClickable(
                        onClick = onShowBranches,
                        onLongClick = {}
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "\u21B3 $branchChildCount alternative replies",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}
