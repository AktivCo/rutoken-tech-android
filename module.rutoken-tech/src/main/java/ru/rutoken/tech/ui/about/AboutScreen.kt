package ru.rutoken.tech.ui.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.components.ScreenTopAppBar
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight

// TODO: Finish this screen
@Composable
fun AboutScreen(openDrawer: () -> Unit) {
    Scaffold(
        topBar = { ScreenTopAppBar(screenName = stringResource(id = R.string.tab_about), openDrawer = openDrawer) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            Text("About screen content")
        }
    }
}

@PreviewLight
@PreviewDark
@Composable
private fun AboutScreenPreview() {
    RutokenTechTheme {
        AboutScreen {}
    }
}