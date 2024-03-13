package ru.rutoken.tech.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.theme.bodyMediumOnSurfaceVariant
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight

data class TextGroupItem(val title: String, val value: String? = null)

@Composable
fun TextGroupBox(
    items: List<TextGroupItem>,
    padding: PaddingValues = PaddingValues(16.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh
) {
    Box(Modifier.padding(padding)) {
        TextGroup(items, backgroundColor)
    }
}

@Composable
fun TextGroup(
    items: List<TextGroupItem>,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(16.dp),
    ) {
        itemsIndexed(items) { index, item ->
            TextGroupItem(item)
            if (index != items.lastIndex)
                HorizontalDivider(Modifier.padding(vertical = 16.dp))
        }
    }
}

@Composable
fun TextGroupItem(item: TextGroupItem) {
    Column {
        Text(
            text = item.title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge
        )
        if (item.value != null) {
            Text(
                text = item.value,
                style = bodyMediumOnSurfaceVariant
            )
        }
    }
}

@PreviewLight
@PreviewDark
@Composable
fun TextGroupPreview() {
    RutokenTechTheme {
        Surface {
            Column {
                TextGroupBox(
                    items = listOf(
                        TextGroupItem(title = "Title 1", value = "Value 1"),
                        TextGroupItem(title = "Title 2", value = "Value 2")
                    ),
                )

                TextGroupBox(
                    items = listOf(
                        TextGroupItem(title = "Title 3"),
                        TextGroupItem(title = "Title 4")
                    ),
                )
            }
        }
    }
}