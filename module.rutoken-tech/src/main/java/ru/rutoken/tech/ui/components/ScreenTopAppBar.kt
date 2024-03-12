package ru.rutoken.tech.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import ru.rutoken.tech.ui.utils.Modifiers

@Composable
fun ScreenTopAppBar(
    screenName: String,
    trailingIcon: ImageVector? = null,
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
            IconButton(
                onClick = { /* TODO */ },
                modifier = Modifiers.appBarIconSize
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Navigation Icon"
                )
            }
        },
        actions = {
            IconButton(
                onClick = onTrailingIconClick,
                enabled = trailingIcon != null,
                modifier = Modifiers.appBarIconSize
            ) {
                if (trailingIcon != null) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = "Trailing Icon"
                    )
                }
            }
        }
    )
}
