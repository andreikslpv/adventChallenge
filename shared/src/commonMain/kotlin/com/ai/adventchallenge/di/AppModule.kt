package com.ai.adventchallenge.di

import com.ai.adventchallenge.data.api.ZaiApiService
import com.ai.adventchallenge.domain.repositories.AgentRepository
import com.ai.adventchallenge.domain.repositories.MessageRepository
import com.ai.adventchallenge.data.repositories.AgentRepositoryImpl
import com.ai.adventchallenge.data.repositories.MessageRepositoryImpl
import com.ai.adventchallenge.platform.ApiKeyProvider
import com.ai.adventchallenge.viewmodel.ChatViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

fun initKoin() {
    startKoin {
        modules(listOf(appModule, platformModule))
    }
}

val appModule = module {
    single { createHttpClient() }
    single { ApiKeyProvider() }
    single { ZaiApiService(get()) }
    single<AgentRepository> { AgentRepositoryImpl(get()) }
    single<MessageRepository> { MessageRepositoryImpl(get()) }
    single { ChatViewModel(get(), get(), get()) }
}

expect val platformModule: Module

fun createHttpClient(): HttpClient {
    return HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = false
                isLenient = true
                encodeDefaults = true
            })
        }

        install(Logging) {
            level = LogLevel.ALL
            logger = Logger.SIMPLE
        }

        install(HttpTimeout) {
            connectTimeoutMillis = 30000
            requestTimeoutMillis = 180000
            socketTimeoutMillis = 180000
        }

        defaultRequest {
            header("Content-Type", "application/json")
            contentType(ContentType.Application.Json)
        }
    }
}
