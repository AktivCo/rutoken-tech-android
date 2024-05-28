/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.rutoken.tech.ui.utils.navigationBarPaddingHeight

/**
 * Bottom padding to compensate edge-to-edge application UI
 */
@Composable
fun NavigationBarSpacer() {
    Spacer(Modifier.navigationBarPaddingHeight())
}
