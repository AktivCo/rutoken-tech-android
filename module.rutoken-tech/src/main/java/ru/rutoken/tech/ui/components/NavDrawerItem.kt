/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.unit.dp

@Composable
fun NavDrawerItem(
    label: String,
    selected: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        label = { Text(text = label, fontWeight = if (selected) W600 else W500) },
        icon = icon,
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(100.dp),
    )
}