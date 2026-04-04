package com.ai.adventchallenge.platform

import android.content.Context
import androidx.compose.runtime.Composable

@Composable
actual fun getClipboardContext(): Any? {
    return androidx.compose.ui.platform.LocalContext.current
}
