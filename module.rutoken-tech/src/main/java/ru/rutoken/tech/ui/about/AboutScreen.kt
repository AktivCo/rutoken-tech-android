/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import ru.rutoken.tech.BuildConfig
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.components.ScreenTopAppBar
import ru.rutoken.tech.ui.components.TextGroup
import ru.rutoken.tech.ui.components.TextGroupItem
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight
import ru.rutoken.tech.ui.utils.launchCustomTabsUrl

private const val PRIVACY_POLICY_URL = "https://www.rutoken.ru/company/policy/rutech-android.html"

@Composable
fun AboutScreen(openDrawer: () -> Unit) {
    Scaffold(
        topBar = {
            ScreenTopAppBar(screenName = stringResource(id = R.string.about_app_menu_item), openDrawer = openDrawer)
        }
    ) { innerPadding ->
        CompositionLocalProvider(
            LocalOverscrollConfiguration provides null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppData()
                BuildInfo()
                TechnicalSupport()
                ActionButton()
            }
        }
    }
}

@Composable
private fun AppData() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.app_icon_merged),
            contentDescription = null,
            modifier = Modifier.size(96.dp)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                stringResource(id = R.string.app_name),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                stringResource(id = R.string.aktiv_company),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun BuildInfo() {
    var buildVersion by remember { mutableStateOf("") }
    TextGroup(
        items = listOf(
            TextGroupItem(title = stringResource(id = R.string.build_version), value = buildVersion),
            TextGroupItem(title = stringResource(id = R.string.commit_id), value = BuildConfig.COMMIT_HASH)
        ),
        backgroundColor = MaterialTheme.colorScheme.surfaceContainer
    )

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        buildVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }
}

@Composable
private fun TechnicalSupport() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.technical_support),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge
        )
        val context = LocalContext.current
        val phoneNumber = stringResource(id = R.string.technical_support_number)
        Text(
            text = phoneNumber,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable {
                with(Intent(Intent.ACTION_DIAL)) {
                    data = Uri.parse("tel:$phoneNumber")
                    startActivity(context, this, null)
                }
            }
        )
    }
}

@Composable
private fun ActionButton() {
    val context = LocalContext.current

    TextButton(onClick = { context.launchCustomTabsUrl(Uri.parse(PRIVACY_POLICY_URL)) }) {
        Text(text = stringResource(id = R.string.privacy_policy))
    }
}

@PreviewLight
@PreviewDark
@Composable
private fun AboutScreenPreview() {
    RutokenTechTheme {
        AboutScreen(openDrawer = {})
    }
}