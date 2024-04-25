package ru.rutoken.tech.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import ru.rutoken.tech.R

object AppIcons {
    @Composable
    fun Logout() {
        Icon(
            painter = painterResource(id = R.drawable.ic_logout),
            contentDescription = "Logout icon",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    @Composable
    fun Menu() {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu Icon"
        )
    }

    @Composable
    fun MenuTitleLogo() {
        Icon(
            painter = painterResource(id = R.drawable.menu_logo),
            contentDescription = "Menu Title Logo",
            tint = MaterialTheme.colorScheme.tertiary
        )
    }

    @Composable
    fun CaMenuItem() {
        Icon(
            painter = painterResource(id = R.drawable.ic_ca_menu_item),
            contentDescription = "CA Menu Icon",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    @Composable
    fun AboutMenuItem() {
        Icon(
            painter = painterResource(id = R.drawable.ic_about_menu_item),
            contentDescription = "About Menu Icon",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}