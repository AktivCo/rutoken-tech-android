/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import ru.rutoken.tech.ui.utils.Modifiers

data class RutokenTechTopAppBarAction(val actionContent: @Composable () -> Unit, val onActionClick: () -> Unit)

@Composable
fun RutokenTechTopAppBar(
    titleText: String,
    navigationIcon: @Composable () -> Unit,
    onNavigationIconClick: () -> Unit,
    actions: List<RutokenTechTopAppBarAction> = emptyList(),
    colors: TopAppBarColors = TopAppBarDefaults.mediumTopAppBarColors(),
) {
    TopAppBar(
        title = {
            Text(
                text = titleText,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick, modifier = Modifiers.appBarIconSize, content = navigationIcon)
        },
        actions = {
            actions.forEach {
                IconButton(onClick = it.onActionClick, modifier = Modifiers.appBarIconSize, content = it.actionContent)
            }
        },
        windowInsets = WindowInsets.systemBarsIgnoringVisibility.only(WindowInsetsSides.Top),
        colors = colors
    )
}

@Composable
fun RutokenTechLargeTopAppBar(
    titleText: String,
    navigationIcon: @Composable () -> Unit,
    onNavigationIconClick: () -> Unit,
    actions: List<RutokenTechTopAppBarAction> = emptyList(),
    colors: TopAppBarColors = TopAppBarDefaults.largeTopAppBarColors()
) {
    LargeTopAppBar(
        title = {
            Text(
                text = titleText,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineMedium
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick, modifier = Modifiers.appBarIconSize, content = navigationIcon)
        },
        actions = {
            actions.forEach {
                IconButton(onClick = it.onActionClick, modifier = Modifiers.appBarIconSize, content = it.actionContent)
            }
        },
        windowInsets = WindowInsets.systemBarsIgnoringVisibility.only(WindowInsetsSides.Top),
        colors = colors
    )
}

@Composable
fun MenuScreenTopAppBar(
    screenName: String,
    openDrawer: () -> Unit,
    actions: List<RutokenTechTopAppBarAction> = emptyList(),
) {
    RutokenTechLargeTopAppBar(
        titleText = screenName,
        navigationIcon = { AppIcons.Menu() },
        onNavigationIconClick = openDrawer,
        actions = actions
    )
}
