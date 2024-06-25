/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.bank.BankDestination
import ru.rutoken.tech.ui.components.AppIcons
import ru.rutoken.tech.ui.components.NavDrawerItem
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight

@Composable
fun MainDrawerContent(
    currentDestination: String,
    onCloseDrawer: () -> Unit,
    onNavigateToBank: () -> Unit,
    onNavigateToCa: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    Column(modifier = Modifier.padding(12.dp)) {
        Column(
            modifier = Modifier
                .height(56.dp)
                .padding(start = 16.dp, end = 8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            AppIcons.MenuTitleLogo()
        }

        Column(
            modifier = Modifier
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(id = R.string.menu_title),
                fontWeight = W500,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleSmall
            )
        }

        val isBankSelected = currentDestination.contains(AppSectionDestination.Bank.route)
        NavDrawerItem(
            label = stringResource(id = R.string.tab_bank),
            selected = isBankSelected,
            icon = { AppIcons.BankMenuItem(isBankSelected) },
            onClick = {
                if (!isBankSelected) {
                    onCloseDrawer()
                    onNavigateToBank()
                }
            }
        )

        val isCaSelected = currentDestination.contains(AppSectionDestination.Ca.route)
        NavDrawerItem(
            label = stringResource(id = R.string.ca_menu_item),
            selected = isCaSelected,
            icon = { AppIcons.CaMenuItem(isCaSelected) },
            onClick = {
                if (!isCaSelected) {
                    onCloseDrawer()
                    onNavigateToCa()
                }
            }
        )

        val isAboutSelected = currentDestination.contains(AppSectionDestination.About.route)
        NavDrawerItem(
            label = stringResource(id = R.string.tab_about),
            selected = isAboutSelected,
            icon = { AppIcons.AboutMenuItem(isAboutSelected) },
            onClick = {
                if (!isAboutSelected) {
                    onCloseDrawer()
                    onNavigateToAbout()
                }
            }
        )
    }
}

@PreviewLight
@PreviewDark
@Composable
private fun MainDrawerContentPreview() {
    RutokenTechTheme {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(drawerState) {
                    MainDrawerContent(
                        currentDestination = BankDestination.Start.route,
                        onCloseDrawer = {},
                        onNavigateToBank = {},
                        onNavigateToCa = {},
                        onNavigateToAbout = {}
                    )
                }
            },
        ) {}
    }
}