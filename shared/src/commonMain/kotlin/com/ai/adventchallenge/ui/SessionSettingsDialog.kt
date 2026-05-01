package com.ai.adventchallenge.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ai.adventchallenge.domain.context.ContextSettings
import com.ai.adventchallenge.domain.context.ContextStrategyType
import com.ai.adventchallenge.domain.model.Session

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSettingsDialog(
    session: Session,
    onDismiss: () -> Unit,
    onSettingsChanged: (ContextSettings) -> Unit
) {
    val settings = session.contextSettings
    var selectedStrategy by remember(settings) { mutableStateOf(settings.strategy) }

    var slidingWindowSize by remember(settings) { mutableStateOf(TextFieldValue(settings.slidingWindowSize.toString())) }
    var factsWindowSize by remember(settings) { mutableStateOf(TextFieldValue(settings.factsWindowSize.toString())) }
    var summaryTriggerSize by remember(settings) { mutableStateOf(TextFieldValue(settings.summaryTriggerSize.toString())) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = true)
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp,
            modifier = Modifier.width(600.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                TopAppBar(
                    title = { Text("Настройки сессии") },
                    actions = {
                        Button(onClick = onDismiss) {
                            Text("Закрыть")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Стратегия контекста",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    ContextStrategyType.entries.forEach { strategy ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = selectedStrategy == strategy,
                                onClick = { selectedStrategy = strategy }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(getStrategyDisplayName(strategy))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedStrategy) {
                    ContextStrategyType.SLIDING_WINDOW -> {
                        Text(
                            text = "Размер окна",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = slidingWindowSize,
                            onValueChange = { slidingWindowSize = it },
                            label = { Text("Количество последних сообщений") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    ContextStrategyType.STICKY_FACTS -> {
                        Text(
                            text = "Размер окна фактов",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = factsWindowSize,
                            onValueChange = { factsWindowSize = it },
                            label = { Text("Количество последних сообщений с фактами") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    ContextStrategyType.SUMMARY -> {
                        Text(
                            text = "Размер триггера",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = summaryTriggerSize,
                            onValueChange = { summaryTriggerSize = it },
                            label = { Text("Количество сообщений до сжатия") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    ContextStrategyType.FULL_HISTORY -> {
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val settings = ContextSettings(
                            strategy = selectedStrategy,
                            slidingWindowSize = slidingWindowSize.text.toIntOrNull() ?: 10,
                            factsWindowSize = factsWindowSize.text.toIntOrNull() ?: 6,
                            summaryTriggerSize = summaryTriggerSize.text.toIntOrNull() ?: 20
                        )
                        onSettingsChanged(settings)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}

private fun getStrategyDisplayName(strategy: ContextStrategyType): String {
    return when (strategy) {
        ContextStrategyType.FULL_HISTORY -> "Полная история"
        ContextStrategyType.SLIDING_WINDOW -> "Скользящее окно"
        ContextStrategyType.STICKY_FACTS -> "Липкие факты"
        ContextStrategyType.SUMMARY -> "Сжатие (Summary)"
    }
}
