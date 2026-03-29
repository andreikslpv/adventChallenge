package com.ai.adventchallenge.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ai.adventchallenge.viewmodel.Agent
import com.ai.adventchallenge.viewmodel.ChatSettings

@Composable
fun AgentSettingsDialog(
    agent: Agent,
    onDismiss: () -> Unit,
    onSettingsChanged: (ChatSettings) -> Unit
) {
    var systemPrompt by remember { mutableStateOf(agent.settings.systemPrompt) }
    var temperature by remember { mutableStateOf(agent.settings.temperature.toString()) }
    var maxCharacterCount by remember { mutableStateOf(agent.settings.maxCharacterCount) }
    var maxTokens by remember { mutableStateOf(agent.settings.maxTokens) }
    var responseType by remember { mutableStateOf(agent.settings.responseType) }
    var stopWord by remember { mutableStateOf(agent.settings.stopWord) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройки агента") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = { systemPrompt = it },
                    label = { Text("Системный промпт") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                OutlinedTextField(
                    value = temperature,
                    onValueChange = {
                        if (it.isEmpty() || it.toFloatOrNull() != null) {
                            temperature = it
                        }
                    },
                    label = { Text("Температура (0.0-2.0)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = maxCharacterCount,
                    onValueChange = {
                        if (it.isEmpty() || it.toIntOrNull() != null) {
                            maxCharacterCount = it
                        }
                    },
                    label = { Text("Макс. символов (опционально)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = maxTokens,
                    onValueChange = {
                        if (it.isEmpty() || it.toIntOrNull() != null) {
                            maxTokens = it
                        }
                    },
                    label = { Text("Макс. токенов (1-8192, опционально)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Тип ответа:")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = responseType == "text",
                                onClick = { responseType = "text" }
                            )
                            Text("Текст")
                        }
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = responseType == "json",
                                onClick = { responseType = "json" }
                            )
                            Text("JSON")
                        }
                    }
                }

                OutlinedTextField(
                    value = stopWord,
                    onValueChange = { stopWord = it },
                    label = { Text("Стоп слово (опционально)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSettingsChanged(
                        ChatSettings(
                            systemPrompt = systemPrompt,
                            temperature = temperature.toFloatOrNull() ?: 0.7f,
                            maxCharacterCount = maxCharacterCount,
                            maxTokens = maxTokens,
                            responseType = responseType,
                            stopWord = stopWord
                        )
                    )
                    onDismiss()
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
