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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ai.adventchallenge.agent.Agent
import com.ai.adventchallenge.api.dtos.ChatMessage
import kotlinx.serialization.json.Json

private val json = Json { prettyPrint = true }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessage,
    agent: Agent?,
    onLongPress: (String) -> Unit
) {
    val isUser = message.role == "user"
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
                color = color,
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
}