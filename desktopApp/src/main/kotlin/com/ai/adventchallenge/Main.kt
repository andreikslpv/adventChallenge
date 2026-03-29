package com.ai.adventchallenge

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.ai.adventchallenge.di.initKoin
import com.ai.adventchallenge.ui.ComposeApp

fun main() {
    initKoin()

    application {
        Window(onCloseRequest = ::exitApplication) {
            ComposeApp()
        }
    }
}
