# Работа с агентами

## Обзор

Агенты — это настроенные экземпляры ИИ, которые могут выполнять запросы с уникальными параметрами. Каждый агент имеет свой системный промпт, настройки температуры и другие параметры, позволяя создавать специализированных ассистентов для разных задач.

## Архитектура

### Основные компоненты

1. **Agent** (`domain/model/Agent.kt`) — модель агента с настройками
2. **AgentSettings** (`domain/model/Agent.kt`) — параметры конфигурации агента
3. **MainAgent** (`domain/service/MainAgent.kt`) — сервис для обработки запросов агента
4. **AgentRepository** (`domain/repository/AgentRepository.kt`) — репозиторий для сохранения агентов
5. **ChatViewModel** — управление жизненным циклом агентов в UI

### Структура данных

```kotlin
data class Agent(
    val id: String,
    val settings: AgentSettings = AgentSettings()
)

data class AgentSettings(
    val systemPrompt: String = "",
    val temperature: Float = 0.7f,
    val selectedModel: AIModel = AIModel.AVAILABLE_MODELS[1],
    val maxTokens: String = "",
    val responseType: String = "text",
    val stopWord: String = ""
)
```

## Параметры агента

### 1. System Prompt (Системный промпт)

Описание роли и поведения ИИ. Определяет, как агент будет отвечать на запросы.

Примеры:
- `"Ты полезный ассистент по программированию"`
- `"Ты эксперт по финансовым вопросам"`
- `"Ты креативный писатель фантастики"`

### 2. Temperature (Температура)

Параметр, контролирующий креативность ответов:
- **0.0** — более консервативные, предсказуемые ответы
- **0.7** — баланс между креативностью и точностью (значение по умолчанию)
- **2.0** — максимально креативные, менее предсказуемые ответы

### 3. Selected Model (Выбранная модель)

Модель ИИ для обработки запросов. Доступные модели определены в `AIModel.AVAILABLE_MODELS`.

### 4. Max Tokens (Максимальное количество токенов)

Ограничение на длину ответа в токенах (1-8192). Позволяет контролировать стоимость API-запросов.

### 5. Response Type (Тип ответа)

Формат возвращаемого ответа:
- `"text"` — текстовый формат (по умолчанию)
- `"json"` — JSON формат (для структурированных данных)

### 6. Stop Word (Стоп-слово)

Слово или фраза, при появлении которой генерация ответа прекращается.

## Жизненный цикл агента

### Создание агента

```kotlin
fun addAgent(agentId: String? = null) {
    if (agentId != null) {
        viewModelScope.launch {
            val agent = agentRepository.getAgentById(agentId)
            if (agent != null) {
                _uiState.value = _uiState.value.copy(
                    agents = _uiState.value.agents + agent,
                    selectedAgentId = agent.id,
                    showAgentSelector = false
                )
                loadMessagesForSession(_uiState.value.selectedSessionId)
            }
        }
    } else {
        val newAgent = Agent(id = Uuid.random().toString())
        _uiState.value = _uiState.value.copy(
            agents = _uiState.value.agents + newAgent,
            selectedAgentId = newAgent.id,
            showAgentSelector = false
        )
    }
}
```

### Обновление настроек

```kotlin
fun updateAgentSettings(agentId: String, settings: AgentSettings) {
    val updatedAgents = _uiState.value.agents.map { agent ->
        if (agent.id == agentId) {
            agent.copy(settings = settings)
        } else {
            agent
        }
    }
    _uiState.value = _uiState.value.copy(agents = updatedAgents)

    viewModelScope.launch {
        val agent = updatedAgents.find { it.id == agentId }
        if (agent != null) {
            agentRepository.saveAgent(agent)
        }
    }
}
```

### Удаление агента

```kotlin
fun removeAgent(agentId: String) {
    val newAgents = _uiState.value.agents.filterNot { it.id == agentId }
    val newSelectedId = if (_uiState.value.selectedAgentId == agentId) {
        newAgents.firstOrNull()?.id ?: ""
    } else {
        _uiState.value.selectedAgentId
    }
    _uiState.value = _uiState.value.copy(agents = newAgents, selectedAgentId = newSelectedId)
    
    viewModelScope.launch {
        agentRepository.deleteAgent(agentId)
    }
}
```

### Закрытие агента (удаление из текущей сессии)

```kotlin
fun closeAgent(agentId: String) {
    val newAgents = _uiState.value.agents.filterNot { it.id == agentId }
    if (newAgents.isEmpty()) {
        _uiState.value = _uiState.value.copy(
            agents = emptyList(),
            selectedAgentId = "",
            messages = emptyList()
        )
    } else if (_uiState.value.selectedAgentId == agentId) {
        _uiState.value = _uiState.value.copy(
            agents = newAgents,
            selectedAgentId = newAgents.first.id,
            messages = emptyList()
        )
    } else {
        _uiState.value = _uiState.value.copy(agents = newAgents)
    }
}
```

## Обработка запросов

### MainAgent

Основной сервис для обработки запросов к агенту:

```kotlin
class MainAgent(
    private val aiService: AIService
) {
    suspend fun processRequest(
        agent: Agent,
        userMessage: String,
        conversationHistory: List<Message>,
        sessionSummary: String
    ): AgentResponse {
        return try {
            val messagesToSend = buildMessageList(
                agent.settings.systemPrompt,
                sessionSummary,
                userMessage,
                conversationHistory
            )

            val maxTokens = agent.settings.maxTokens.toIntOrNull()
            val responseType = if (agent.settings.responseType == "json") "json_object" else "text"
            val stopWord = agent.settings.stopWord.takeIf { it.isNotBlank() }

            val result = aiService.sendMessage(
                messages = messagesToSend,
                temperature = agent.settings.temperature,
                maxTokens = maxTokens,
                responseType = responseType,
                stopWord = stopWord,
                model = agent.settings.selectedModel
            )

            result.fold(
                onSuccess = { (message, tokenCount) ->
                    AgentResponse.Success(message, tokenCount)
                },
                onFailure = { exception ->
                    AgentResponse.Error(exception.message ?: "Unknown error")
                }
            )
        } catch (e: Exception) {
            AgentResponse.Error("Processing failed: ${e.message}")
        }
    }
}
```

### Формирование списка сообщений

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

## Типы ответов

### AgentResponse

```kotlin
sealed class AgentResponse {
    data class Success(val message: Message, val tokenCount: Int) : AgentResponse()
    data class Error(val message: String) : AgentResponse()
}
```

- **Success**: Успешный ответ с сообщением и количеством токенов
- **Error**: Ошибка с описанием проблемы

## Режимы работы

### 1. Одиночный агент

Отправка сообщения выбранному агенту:

```kotlin
fun sendMessage(userMessage: String) {
    if (userMessage.isBlank()) return

    val selectedAgent = _uiState.value.agents.find { it.id == _uiState.value.selectedAgentId } ?: return
    sendMessageToAgent(userMessage, selectedAgent)
}
```

### 2. Отправка всем агентам

Одновременная отправка запроса всем активным агентам:

```kotlin
fun sendMessageToAll(userMessage: String) {
    if (userMessage.isBlank()) return
    viewModelScope.launch {
        val outgoingTokens = userMessage.length / 4
        val currentMessages = _uiState.value.messages.toMutableList()
        currentMessages.add(Message(
            id = Uuid.random().toString(),
            role = "user",
            content = userMessage,
            characterCount = userMessage.length,
            outgoingTokenCount = outgoingTokens
        ))
        _uiState.value = _uiState.value.copy(messages = currentMessages, isSendingToAll = true, error = null)

        val userMessages = currentMessages.filter { it.role == "user" }

        _uiState.value.agents.forEach { agent ->
            sendRequestToAgent(userMessage, agent, userMessages)
        }

        _uiState.value = _uiState.value.copy(isSendingToAll = false)
    }
}
```

## Связь с сессиями

Каждая сессия может иметь выбранного агента:

```kotlin
data class Session(
    val id: String,
    val name: String,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val selectedAgentId: String = "",
    val summary: String = "",
    val isCompressionEnabled: Boolean = false
)
```

При выборе сессии загружаются соответствующие агенты:

```kotlin
private fun loadAgentsForSession(sessionId: String) {
    viewModelScope.launch {
        val messages = messageRepository.getMessagesBySessionIdSync(sessionId)
        val agentIds = messages.map { it.agentId }.distinct()
        val agents = agentIds.mapNotNull { agentId ->
            _uiState.value.savedAgents.find { it.id == agentId }
        }.distinct()
        
        if (agents.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                agents = agents,
                selectedAgentId = agents.first.id
            )
        }
    }
}
```

## Хранение агентов

### AgentRepository

Интерфейс для работы с репозиторием агентов:

```kotlin
interface AgentRepository {
    fun getAllAgents(): Flow<List<Agent>>
    suspend fun getAgentById(id: String): Agent?
    suspend fun saveAgent(agent: Agent)
    suspend fun deleteAgent(id: String)
    suspend fun deleteAllAgents()
}
```

### Сохранение при ответе

При успешном ответе агент сохраняется с обновленными настройками:

```kotlin
when (response) {
    is AgentResponse.Success -> {
        // ... обработка сообщения ...
        
        val selectedAgent = _uiState.value.agents.find { it.id == agent.id }
        if (selectedAgent != null) {
            agentRepository.saveAgent(selectedAgent)
        }
    }
}
```

## UI компоненты

### AgentSettingsDialog

Диалоговое окно для настройки параметров агента:

- Поле ввода системного промпта
- Слайдер для настройки температуры
- Выпадающий список для выбора модели
- Поле ввода для максимального количества токенов
- Выбор типа ответа (текст/JSON)
- Поле ввода для стоп-слова

### AgentsRow

Горизонтальный список агентов с возможностью:
- Выбора активного агента
- Настройки агента
- Удаления агента
- Добавления нового агента

## Примеры использования

### Создание агента программиста

```kotlin
val programmerAgent = Agent(
    id = Uuid.random().toString(),
    settings = AgentSettings(
        systemPrompt = "Ты эксперт по разработке программного обеспечения. Помогай с написанием кода, отладкой и архитектурными решениями.",
        temperature = 0.3f,
        maxTokens = "4096",
        responseType = "text"
    )
)
```

### Создание агента для JSON

```kotlin
val jsonAgent = Agent(
    id = Uuid.random().toString(),
    settings = AgentSettings(
        systemPrompt = "Ты всегда отвечаешь в формате JSON с полями 'answer' и 'confidence'.",
        temperature = 0.1f,
        responseType = "json"
    )
)
```

## Преимущества архитектуры

1. **Модульность**: Каждый агент независим и имеет свои настройки
2. **Переиспользование**: Агенты можно сохранять и использовать в разных сессиях
3. **Гибкость**: Широкий набор настроек для различных сценариев
4. **Мультиагентность**: Возможность одновременной работы с несколькими агентами
5. **Сохранение состояния**: Настройки агентов сохраняются в базе данных

## Дополнительные файлы

- `domain/model/Agent.kt` — модели Agent и AgentSettings
- `domain/service/MainAgent.kt` — сервис обработки запросов
- `domain/repository/AgentRepository.kt` — интерфейс репозитория
- `data/repository/AgentRepositoryImpl.kt` — реализация репозитория
- `shared/viewmodel/ChatViewModel.kt` — управление агентами в UI
- `shared/ui/AgentSettingsDialog.kt` — диалог настроек агента
- `shared/ui/components/AgentsRow.kt` — список агентов
