package com.ai.adventchallenge

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.ai.adventchallenge.di.initKoin
import com.ai.adventchallenge.ui.ComposeApp

fun main() {
    initKoin()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            state = androidx.compose.ui.window.WindowState(
                placement = androidx.compose.ui.window.WindowPlacement.Maximized
            )
        ) {
            ComposeApp()
        }
    }
}
