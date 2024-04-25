package ru.rutoken.tech.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.ca.CaDestination
import ru.rutoken.tech.ui.components.AppIcons
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight

@Composable
fun MainDrawerContent(
    currentDestination: String,
    onCloseDrawer: () -> Unit,
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleSmall
            )
        }

        NavigationDrawerItem(
            label = { Text(text = stringResource(id = R.string.ca_menu_item)) },
            icon = { AppIcons.CaMenuItem() },
            selected = currentDestination.contains(AppSectionDestination.Ca.route),
            onClick = {
                if (!currentDestination.contains(AppSectionDestination.Ca.route)) {
                    onCloseDrawer()
                    onNavigateToCa()
                }
            },
            shape = RoundedCornerShape(100.dp),
        )

        NavigationDrawerItem(
            label = { Text(text = stringResource(id = R.string.tab_about)) },
            icon = { AppIcons.AboutMenuItem() },
            selected = currentDestination.contains(AppSectionDestination.About.route),
            onClick = {
                if (!currentDestination.contains(AppSectionDestination.About.route)) {
                    onCloseDrawer()
                    onNavigateToAbout()
                }
            },
            shape = RoundedCornerShape(100.dp),
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
                        currentDestination = CaDestination.Start.route,
                        onCloseDrawer = { },
                        onNavigateToCa = {},
                        onNavigateToAbout = {}
                    )
                }
            },
        ) {}
    }
}