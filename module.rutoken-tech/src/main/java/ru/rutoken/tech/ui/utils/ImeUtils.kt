/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.utils

import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.SoftwareKeyboardController

fun getHideKeyboardAction(focusManager: FocusManager, keyboardController: SoftwareKeyboardController?): () -> Unit {
    return {
        keyboardController?.hide()
        focusManager.clearFocus(true)
    }
}