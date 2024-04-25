package ru.rutoken.tech.ui.utils

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.SoftwareKeyboardController
import kotlinx.coroutines.launch
import ru.rutoken.tech.utils.Workaround

fun getHideKeyboardAction(focusManager: FocusManager, keyboardController: SoftwareKeyboardController?): () -> Unit {
    return {
        keyboardController?.hide()
        if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU || VERSION.SDK_INT < VERSION_CODES.R)
            focusManager.clearFocus(true) // Focus clearing breaks sheet resize on Android 11-12
    }
}

/**
 * Wraps focusable content and fixes keyboard content overlap on 30, 31 and 32 API versions.
 */
@Composable
@Workaround
fun ImeFocusHelper(itemToKeepVisible: @Composable (() -> Unit)) {
    if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU || VERSION.SDK_INT < VERSION_CODES.R) {
        itemToKeepVisible()
    } else {
        val scope = rememberCoroutineScope()
        val view = LocalView.current
        val bringIntoViewRequester = remember { BringIntoViewRequester() }

        DisposableEffect(view) {
            val listener = ViewTreeObserver.OnGlobalLayoutListener {
                scope.launch { bringIntoViewRequester.bringIntoView() }
            }
            view.viewTreeObserver.addOnGlobalLayoutListener(listener)
            onDispose { view.viewTreeObserver.removeOnGlobalLayoutListener(listener) }
        }

        Box(Modifier.bringIntoViewRequester(bringIntoViewRequester)) {
            itemToKeepVisible()
        }
    }
}