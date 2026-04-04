package com.ai.adventchallenge.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

actual fun copyToClipboard(text: String, context: Any?) {
    val androidContext = context as Context
    val clipboard = androidContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied text", text)
    clipboard.setPrimaryClip(clip)
}
