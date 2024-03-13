package ru.rutoken.tech.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight

@Composable
fun PrimaryButtonBox(
    text: String,
    enabled: Boolean = true,
    padding: PaddingValues = PaddingValues(16.dp),
    onClick: () -> Unit
) {
    Box(Modifier.padding(padding)) {
        PrimaryButton(text, enabled = enabled, onClick)
    }
}

@Composable
fun PrimaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        )
    ) {
        Text(text)
    }
}

@PreviewLight
@PreviewDark
@Composable
fun PrimaryButtonPreview() {
    RutokenTechTheme {
        Surface {
            Column {
                PrimaryButtonBox("Text") {}
                PrimaryButtonBox(text = "Text disabled", enabled = false) {}
            }
        }
    }
}