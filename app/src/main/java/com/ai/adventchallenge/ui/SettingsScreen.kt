package com.ai.adventchallenge.ui

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
import com.ai.adventchallenge.viewmodel.ChatSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: ChatSettings,
    onBack: () -> Unit,
    onSave: (ChatSettings) -> Unit
) {
    var systemPrompt by remember { mutableStateOf(settings.systemPrompt) }
    var temperature by remember { mutableFloatStateOf(settings.temperature) }
    var maxCharacterCount by remember { mutableStateOf(settings.maxCharacterCount) }
    var maxTokens by remember { mutableStateOf(settings.maxTokens) }
    var responseType by remember { mutableStateOf(settings.responseType) }
    var stopWord by remember { mutableStateOf(settings.stopWord) }
    var maxTokensError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    valueRange = 0f..2f,
                    steps = 19
                )
                Text(
                    text = "0.0 - более консервативный, 2.0 - более креативный",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = maxCharacterCount,
                onValueChange = { 
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        maxCharacterCount = it
                    }
                },
                label = { Text("Максимальная длина ответа в символах") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                supportingText = { Text("Оставьте пустым, чтобы не ограничивать") }
            )

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
                        onSave(
                            ChatSettings(
                                systemPrompt = systemPrompt,
                                temperature = temperature,
                                maxCharacterCount = maxCharacterCount,
                                maxTokens = maxTokens,
                                responseType = responseType,
                                stopWord = stopWord
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }
        }
    }
}

private fun Float.format(decimals: Int): String {
    return "%.${decimals}f".format(this)
}
