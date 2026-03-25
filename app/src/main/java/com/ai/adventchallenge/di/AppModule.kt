package com.ai.adventchallenge.di

import com.ai.adventchallenge.BuildConfig
import com.ai.adventchallenge.api.ZaiApiService
import com.ai.adventchallenge.viewmodel.ChatViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
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
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = false
                    isLenient = true
                    encodeDefaults = true
                })
            }

            install(Logging) {
                level = if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE
                logger = Logger.SIMPLE
            }

            install(HttpTimeout) {
                connectTimeoutMillis = 30000
                requestTimeoutMillis = 60000
                socketTimeoutMillis = 60000
            }

            defaultRequest {
                header("Content-Type", "application/json")
                contentType(ContentType.Application.Json)
            }
        }
    }

    single {
        ZaiApiService(
            client = get()
        )
    }

    viewModel {
        ChatViewModel(
            get()
        )
    }
}
