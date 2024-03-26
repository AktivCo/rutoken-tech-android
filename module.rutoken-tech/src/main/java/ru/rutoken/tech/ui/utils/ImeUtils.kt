package ru.rutoken.tech.ui.utils

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.SoftwareKeyboardController

fun getHideKeyboardAction(focusManager: FocusManager, keyboardController: SoftwareKeyboardController?): () -> Unit {
    return {
        keyboardController?.hide()
        if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU || VERSION.SDK_INT < VERSION_CODES.R)
            focusManager.clearFocus(true) // Focus clearing breaks sheet resize on Android 11-12
    }
}
