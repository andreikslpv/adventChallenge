package com.ai.adventchallenge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ai.adventchallenge.data.api.AIModel
import com.ai.adventchallenge.agent.Agent
import com.ai.adventchallenge.agent.AgentSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentSettingsDialog(
    agent: Agent,
    onDismiss: () -> Unit,
    onSettingsChanged: (AgentSettings) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var systemPrompt by remember { mutableStateOf(agent.settings.systemPrompt) }
        var temperature by remember { mutableFloatStateOf(agent.settings.temperature) }
        var selectedModel by remember { mutableStateOf(agent.settings.selectedModel) }
        var maxTokens by remember { mutableStateOf(agent.settings.maxTokens) }
        var responseType by remember { mutableStateOf(agent.settings.responseType) }
        var stopWord by remember { mutableStateOf(agent.settings.stopWord) }
        var maxTokensError by remember { mutableStateOf<String?>(null) }

        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Настройки") },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Назад"
                                )
                            }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = systemPrompt,
                        onValueChange = { systemPrompt = it },
                        label = { Text("Системный промпт") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        maxLines = 10
                    )

                    Column {
                        Text(
                            text = "Температура: ${temperature.format(2)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Slider(
                            value = temperature,
                            onValueChange = { temperature = it },
                            valueRange = 0f..1f,
                            steps = 9
                        )
                        Text(
                            text = "0.0 - более консервативный, 1.0 - более креативный",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column {
                        Text(
                            text = "Модель ИИ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        AIModel.AVAILABLE_MODELS.forEach { model ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedModel = model },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedModel == model,
                                    onClick = { selectedModel = model }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(model.displayName)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = maxTokens,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.all { char -> char.isDigit() }) {
                                maxTokens = input
                                maxTokensError = null
                            }
                        },
                        label = { Text("Максимальная длина ответа в токенах") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = maxTokensError != null,
                        supportingText = {
                            if (maxTokensError != null) {
                                Text(maxTokensError!!, color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("Оставьте пустым, чтобы не ограничивать (диапазон: 1-8192)")
                            }
                        }
                    )

                    Column {
                        Text(
                            text = "Тип ответа",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = responseType == "text",
                                    onClick = { responseType = "text" }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Текст")
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = responseType == "json",
                                    onClick = { responseType = "json" }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("JSON")
                            }
                        }
                    }

                    OutlinedTextField(
                        value = stopWord,
                        onValueChange = { stopWord = it },
                        label = { Text("Стоп слово") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        supportingText = { Text("Оставьте пустым, чтобы не использовать") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val tokenCount = maxTokens.toIntOrNull()
                            if (tokenCount != null && (tokenCount !in 1..8192)) {
                                maxTokensError = "Значение должно быть от 1 до 8192"
                            } else {
                                onSettingsChanged(
                                    AgentSettings(
                                        systemPrompt = systemPrompt,
                                        temperature = temperature,
                                        selectedModel = selectedModel,
                                        maxTokens = maxTokens,
                                        responseType = responseType,
                                        stopWord = stopWord
                                    )
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    }
}

fun Float.format(decimals: Int): String {
    val s = this.toString()

    val separatorIndex = s.indexOfAny(charArrayOf('.', ','))
    if (separatorIndex == -1) return s

    val endIndex = (separatorIndex + 1 + decimals).coerceAtMost(s.length)
    return s.substring(0, endIndex)
}
