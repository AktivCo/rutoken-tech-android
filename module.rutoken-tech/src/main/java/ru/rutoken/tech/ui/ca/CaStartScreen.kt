package ru.rutoken.tech.ui.ca

import androidx.compose.foundation.layout.*
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.components.ScreenTopAppBar
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.theme.bodyMediumOnSurfaceVariant
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight
import ru.rutoken.tech.ui.utils.figmaPadding

@Composable
fun CaStartScreen(onClickConnectToken: () -> Unit) {
    Scaffold(
        topBar = {
            ScreenTopAppBar(screenName = stringResource(id = R.string.tab_certificate_authority))
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .figmaPadding(0.dp, 16.dp, 16.dp, 16.dp)
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.connect_rutoken),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = stringResource(id = R.string.connect_rutoken_tip),
                    textAlign = TextAlign.Center,
                    style = bodyMediumOnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FilledTonalButton(
                onClick = onClickConnectToken,
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.connect),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@PreviewLight
@PreviewDark
@Composable
private fun CaStartScreenPreview() {
    RutokenTechTheme {
        CaStartScreen(onClickConnectToken = {})
    }
}
