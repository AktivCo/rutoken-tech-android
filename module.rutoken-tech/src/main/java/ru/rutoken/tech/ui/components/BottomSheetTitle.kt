package ru.rutoken.tech.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight

@Composable
fun BottomSheetTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(16.dp)
    )
}

@PreviewLight
@PreviewDark
@Composable
fun BottomSheetTitlePreview() {
    RutokenTechTheme {
        Surface(Modifier.fillMaxWidth()) {
            BottomSheetTitle(stringResource(id = R.string.key_pair_title))
        }
    }
}