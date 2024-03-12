package ru.rutoken.tech.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight

@Composable
fun ErrorAlertDialog(
    title: String,
    text: String,
    onDismissOrConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismissOrConfirm() },
        confirmButton = {
            TextButton(onClick = { onDismissOrConfirm() }) {
                Text(stringResource(id = R.string.ok))
            }
        },
        modifier = Modifier.width(312.dp),
        title = { Text(text = title) },
        text = { Text(text = text) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

@PreviewLight
@PreviewDark
@Composable
fun ErrorAlertDialogPreview() {
    RutokenTechTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ErrorAlertDialog(
                title = stringResource(id = R.string.pin_blocked_title),
                text = stringResource(id = R.string.pin_blocked_text),
                onDismissOrConfirm = { /* Nothing to do */ }
            )
        }
    }
}
