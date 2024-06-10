/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle

val bodyMediumOnSurfaceVariant: TextStyle
    @Composable
    get() = MaterialTheme.typography.bodyMedium.merge(color = MaterialTheme.colorScheme.onSurfaceVariant)