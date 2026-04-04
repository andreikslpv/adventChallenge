package com.ai.adventchallenge.platform

actual fun copyToClipboard(text: String, context: Any?) {
    val navigator = js("navigator")
    navigator.clipboard.writeText(text)
}
