# Суммаризация сессий

## Обзор

Суммаризация — это механизм сжатия истории сообщений в сессии, который позволяет эффективно управлять контекстом при длительных беседах с ИИ. В приложении реализована инкрементальная суммаризация через специальный агент-суммаризатор.

## Назначение

Суммаризация решает следующие задачи:

- **Экономия токенов**: Сжатие истории беседы позволяет отправлять меньше токенов в запросах к API
- **Сохранение контекста**: Важная информация сохраняется в сжатом виде и используется в будущих запросах
- **Улучшение качества ответов**: ИИ получает релевантный контекст без перегрузки деталями

## Архитектура

### Компоненты системы суммаризации

1. **SummarizerAgent** (`domain/service/SummarizerAgent.kt`) — специальный агент для создания и обновления суммаризации
2. **Session** (`domain/model/Session.kt`) — модель сессии, содержащая поле `summary` для хранения суммаризации
3. **MessageRepository** — репозиторий для работы с сообщениями, включая поиск несуммированных сообщений
4. **ChatViewModel** — управляет процессом суммаризации при отправке сообщений

### Поток данных

```
Пользовательское сообщение → Сохранение → Проверка сжатия → 
Суммаризация (если включено) → Отправка в ИИ → Ответ ИИ → Обновление UI
```

## Принципы работы

### 1. Включение сжатия

Сжатие включается на уровне сессии через флаг `isCompressionEnabled`:

```kotlin
data class Session(
    val id: String,
    val name: String,
    val createdAt: Long,
    val selectedAgentId: String = "",
    val summary: String = "",
    val isCompressionEnabled: Boolean = false  // Включение сжатия
)
```

### 2. Инкрементальная суммаризация

Суммаризация выполняется инкрементально — каждые 10 несуммированных сообщений:

```kotlin
private suspend fun performSummarization(sessionId: String) {
    val session = sessionRepository.getSessionById(sessionId) ?: return
    
    if (!session.isCompressionEnabled) {
        return
    }

    val batchSize = 10
    var hasMoreMessages = true

    while (hasMoreMessages) {
        val unsummarizedMessages = messageRepository.getUnsummarizedMessages(sessionId, batchSize)
        
        if (unsummarizedMessages.size < batchSize) {
            hasMoreMessages = false
            if (unsummarizedMessages.isEmpty()) {
                break
            }
        }

        val result = summarizerAgent.summarize(session.summary, unsummarizedMessages)
        
        result.fold(
            onSuccess = { newSummary ->
                sessionRepository.updateSessionSummary(sessionId, newSummary)
                val messageIds = unsummarizedMessages.map { it.id }
                messageRepository.markMessagesAsSummarized(messageIds)
            },
            onFailure = { exception ->
                println("Summarization failed: ${exception.message}")
            }
        )
    }
}
```

### 3. Использование суммаризации в запросах

При отправке запроса к ИИ суммаризация добавляется как системное сообщение:

```kotlin
private fun buildMessageList(
    systemPrompt: String,
    sessionSummary: String,
    userMessage: String,
    conversationHistory: List<Message>
): List<Message> {
    val messages = mutableListOf<Message>()

    if (systemPrompt.isNotBlank()) {
        messages.add(Message(role = "system", content = systemPrompt))
    }

    if (sessionSummary.isNotBlank()) {
        messages.add(Message(
            role = "system",
            content = "Conversation summary:\n\n$sessionSummary"
        ))
    }

    messages.addAll(conversationHistory)
    messages.add(Message(role = "user", content = userMessage))

    return messages
}
```

## SummarizerAgent

### Системный промпт

Агент-суммаризатор использует детальный системный промпт, который определяет правила создания суммаризации:

```kotlin
private const val SYSTEM_PROMPT = "You are a context summarization agent.\n" +
    "Your task is to maintain a compact, accurate, and up-to-date summary of a conversation.\n" +
    "Rules:\n" +
    "- You MUST compress conversation history into a concise summary.\n" +
    "- You MUST preserve important facts, user goals, constraints, and ongoing tasks.\n" +
    "- You MUST remove irrelevant details, repetitions, and small talk.\n" +
    "- You MUST NOT invent information.\n" +
    "- You MUST NOT include instructions or meta commentary.\n" +
    "- You MUST write in a neutral, factual style.\n" +
    "Language:\n" +
    "The summary must be written in the same language as the messages.\n" +
    "IMPORTANT:\n" +
    "- The summary is used as long-term memory for another AI agent.\n" +
    "- It must be self-contained and understandable without original messages.\n" +
    "- It must be updated incrementally using previous summary + new messages.\n" +
    "- Keep the summary under 1500 characters.\n" +
    "Focus on extracting:\n" +
    "- User intent and goals\n" +
    "- Key entities (projects, technologies, objects)\n" +
    "- Decisions made\n" +
    "- Current state of work\n" +
    "- Open problems\n" +
    "Output format:\n" +
    "Plain text summary only. No markdown. No explanations.\n" +
    "Keep the summary under 1500 characters."
```

### Пользовательский промпт

Для каждого запроса на суммаризацию формируется пользовательский промпт:

```kotlin
private const val USER_PROMPT_TEMPLATE = "Update the conversation summary.\n\n" +
    "Previous summary:\n{OLD_SUMMARY}\n\n" +
    "New messages:\n{MESSAGES_BLOCK}\n\n" +
    "Instructions:\n" +
    "- Merge previous summary with new messages\n" +
    "- Keep it concise but informative\n" +
    "- Preserve important details\n" +
    "- Remove redundancy\n" +
    "- Maintain continuity\n\n" +
    "The summary must be written in the same language as the messages.\n\n" +
    "Return updated summary only."
```

### Метод суммаризации

Основной метод `summarize` выполняет следующие шаги:

1. Проверяет наличие новых сообщений
2. Формирует текст сообщений для отправки
3. Создает пользовательский промпт с предыдущей суммаризацией и новыми сообщениями
4. Отправляет запрос к API с температурой 0.3 (для более детерминированных результатов)
5. Возвращает обновленную суммаризацию

```kotlin
suspend fun summarize(
    previousSummary: String,
    messages: List<Message>
): Result<String> {
    if (messages.isEmpty()) {
        return Result.success(previousSummary)
    }

    return try {
        val messagesText = messages.joinToString("\n\n") { message ->
            "${message.role}: ${message.content}"
        }

        val userPrompt = USER_PROMPT_TEMPLATE
            .replace("{OLD_SUMMARY}", previousSummary.ifEmpty { "None" })
            .replace("{MESSAGES_BLOCK}", messagesText)

        val messagesToSend = listOf(
            Message(role = "system", content = SYSTEM_PROMPT),
            Message(role = "user", content = userPrompt)
        )

        val result = aiService.sendMessage(
            messages = messagesToSend,
            temperature = 0.3f,
            maxTokens = 1000,
            responseType = "text",
            stopWord = null,
            model = SUMMARIZER_MODEL
        )

        result.map { (message, _) -> message.content }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

## Жизненный цикл суммаризации

### Создание сессии

```kotlin
fun createNewSession() {
    val sessionId = Uuid.random().toString()
    val newSession = Session(
        id = sessionId,
        name = "Новая сессия ${_uiState.value.sessions.size + 1}",
        selectedAgentId = _uiState.value.selectedAgentId,
        summary = "",  // Пустая суммаризация
        isCompressionEnabled = false  // Сжатие отключено по умолчанию
    )
    viewModelScope.launch {
        sessionRepository.saveSession(newSession)
        selectSession(sessionId)
    }
}
```

### Отправка сообщения с суммаризацией

```kotlin
private fun sendMessageToAgent(userMessage: String, agent: Agent) {
    // ... сохранение сообщения пользователя ...
    
    viewModelScope.launch {
        val sessionId = _uiState.value.selectedSessionId
        
        messageRepository.saveMessage(userMsg, sessionId)
        
        val session = sessionRepository.getSessionById(sessionId)
        if (session != null && session.isCompressionEnabled) {
            performSummarization(sessionId)  // Выполнение суммаризации
        }
        
        // ... получение ответа от ИИ с использованием суммаризации ...
    }
}
```

### Очистка сессии

```kotlin
fun clearSession() {
    val sessionId = _uiState.value.selectedSessionId
    _uiState.value = _uiState.value.copy(messages = emptyList())

    if (sessionId.isNotEmpty()) {
        viewModelScope.launch {
            messageRepository.deleteMessagesBySessionId(sessionId)
            sessionRepository.updateSessionSummary(sessionId, "")  // Очистка суммаризации
        }
    }
}
```

## Управление сжатием

### Включение/выключение сжатия

```kotlin
fun updateSessionCompressionEnabled(isEnabled: Boolean) {
    val sessionId = _uiState.value.selectedSessionId
    if (sessionId.isNotEmpty()) {
        viewModelScope.launch {
            sessionRepository.updateSessionCompressionEnabled(sessionId, isEnabled)
        }
    }
}
```

### Маркировка сообщений

Сообщения маркируются как суммированные через репозиторий:

```kotlin
suspend fun markMessagesAsSummarized(messageIds: List<String>) {
    // Реализация в MessageRepositoryImpl
}
```

## Преимущества подхода

1. **Инкрементальность**: Обновляется только часть контекста, а не вся история
2. **Автоматизация**: Процесс происходит автоматически при отправке сообщений
3. **Гибкость**: Можно включать/выключать сжатие для каждой сессии
4. **Эффективность**: Пакетная обработка (по 10 сообщений) снижает количество API-запросов
5. **Сохранение языка**: Суммаризация сохраняет язык оригинальных сообщений

## Ограничения

- Максимальная длина суммаризации: 1500 символов
- Размер пакета для суммаризации: 10 сообщений
- Суммаризация работает только если включен флаг `isCompressionEnabled` для сессии

## Дополнительные файлы

- `domain/service/SummarizerAgent.kt` — реализация агента-суммаризатора
- `domain/model/Session.kt` — модель сессии с полем summary
- `domain/repository/SessionRepository.kt` — репозиторий для работы с сессиями
- `domain/repository/MessageRepository.kt` — репозиторий для работы с сообщениями
- `shared/viewmodel/ChatViewModel.kt` — ViewModel с логикой управления суммаризацией
