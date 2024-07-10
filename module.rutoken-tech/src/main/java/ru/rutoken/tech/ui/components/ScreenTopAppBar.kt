/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.components

import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import ru.rutoken.tech.ui.utils.Modifiers

@Composable
fun ScreenTopAppBar(
    screenName: String,
    navigationIcon: @Composable () -> Unit,
    onNavigationIconClick: () -> Unit,
    trailingIcon: (@Composable () -> Unit)? = null,
    onTrailingIconClick: () -> Unit = {}
) {
    LargeTopAppBar(
        title = {
            Text(
                text = screenName,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineMedium
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick, modifier = Modifiers.appBarIconSize, content = navigationIcon)
        },
        actions = {
            if (trailingIcon != null) {
                IconButton(onClick = onTrailingIconClick, modifier = Modifiers.appBarIconSize, content = trailingIcon)
            }
        }
    )
}

@Composable
fun MenuScreenTopAppBar(
    screenName: String,
    openDrawer: () -> Unit,
    trailingIcon: (@Composable () -> Unit)? = null,
    onTrailingIconClick: () -> Unit = {}
) {
    ScreenTopAppBar(
        screenName = screenName,
        navigationIcon = { AppIcons.Menu() },
        onNavigationIconClick = openDrawer,
        trailingIcon = trailingIcon,
        onTrailingIconClick = onTrailingIconClick
    )
}
