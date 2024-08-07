/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.ca.tokeninfo

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.components.AppIcons
import ru.rutoken.tech.ui.components.MenuScreenTopAppBar
import ru.rutoken.tech.ui.components.TextGroup
import ru.rutoken.tech.ui.components.TextGroupItem
import ru.rutoken.tech.ui.components.alertdialog.SimpleAlertDialog
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.DialogState
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight
import ru.rutoken.tech.ui.utils.figmaPadding

@Composable
fun CaTokenInfoScreen(
    viewModel: CaTokenInfoViewModel,
    onNavigateToGenerateKeyPair: () -> Unit,
    onNavigateToGenerateCertificate: () -> Unit,
    onLogout: () -> Unit,
    openDrawer: () -> Unit
) {
    val tokenInfoUiState by viewModel.uiState.observeAsState()
    val shouldNavigateToCertGeneration by viewModel.navigateToCertGenerationEvent.observeAsState(false)

    if (shouldNavigateToCertGeneration) {
        onNavigateToGenerateCertificate()
        viewModel.resetNavigateToCertGenerationEvent()
    }

    NoKeyPairsOnTokenDialog(viewModel)

    TokenInfoScreen(
        uiState = tokenInfoUiState!!,
        onNavigateToGenerateKeyPair = onNavigateToGenerateKeyPair,
        onNavigateToGenerateCertificate = viewModel::onNavigateToGenerateCertificate,
        onLogout = onLogout,
        openDrawer = openDrawer
    )
}

@Composable
private fun TokenInfoScreen(
    uiState: CaTokenInfoUiState,
    onNavigateToGenerateKeyPair: () -> Unit,
    onNavigateToGenerateCertificate: () -> Unit,
    onLogout: () -> Unit,
    openDrawer: () -> Unit
) {
    Scaffold(
        topBar = {
            MenuScreenTopAppBar(
                screenName = stringResource(id = R.string.tab_certificate_authority),
                openDrawer = openDrawer,
                trailingIcon = { AppIcons.Logout() },
                onTrailingIconClick = onLogout
            )
        }
    ) { innerPadding ->
        CompositionLocalProvider(
            LocalOverscrollConfiguration provides null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .figmaPadding(0.dp, 16.dp, 16.dp, 16.dp)
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top)
            ) {
                TokenImage(uiState.tokenType)
                TokenInfo(uiState)
                Actions(onNavigateToGenerateKeyPair, onNavigateToGenerateCertificate)
            }
        }
    }
}

@Composable
private fun TokenImage(tokenType: TokenType) {
    Box(
        modifier = Modifier
            .height(152.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 20.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(tokenType.imageDrawable),
            contentDescription = "$tokenType token image"
        )
    }
}

private val TokenType.imageDrawable: Int
    get() {
        return when (this) {
            TokenType.Usb -> R.drawable.usb_token
            TokenType.IsoNfc -> R.drawable.nfc_token
            TokenType.UsbNfcDual -> R.drawable.usb_nfc_token
        }
    }

@Composable
private fun TokenInfo(uiState: CaTokenInfoUiState) {
    TextGroup(
        items = listOf(
            TextGroupItem(stringResource(R.string.token_label), uiState.label),
            TextGroupItem(stringResource(R.string.token_model), uiState.model),
            TextGroupItem(stringResource(R.string.token_serial), uiState.serial)
        ),
        backgroundColor = MaterialTheme.colorScheme.surfaceContainer
    )
}

@Composable
private fun Actions(onNavigateToGenerateKeyPair: () -> Unit, onNavigateToGenerateCertificate: () -> Unit) {
    Row(
        modifier = Modifier.height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        ActionButton(
            R.string.generate_key_pair,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(topStart = 100.dp, bottomStart = 100.dp),
            onClick = onNavigateToGenerateKeyPair
        )
        ActionButton(
            R.string.generate_certificate,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(topEnd = 100.dp, bottomEnd = 100.dp),
            onClick = onNavigateToGenerateCertificate
        )
    }
}

@Composable
private fun ActionButton(@StringRes textId: Int, modifier: Modifier = Modifier, shape: Shape, onClick: () -> Unit) {
    FilledTonalButton(
        modifier = modifier.fillMaxHeight(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
        shape = shape,
        onClick = onClick
    ) {
        Text(stringResource(textId), textAlign = TextAlign.Center)
    }
}

@Composable
private fun NoKeyPairsOnTokenDialog(viewModel: CaTokenInfoViewModel) {
    val dialogState by viewModel.noKeyPairsOnTokenDialogState.observeAsState(DialogState())

    if (dialogState.showDialog) {
        SimpleAlertDialog(
            text = stringResource(id = dialogState.data.text!!),
            onDismissOrConfirm = viewModel::onNoKeyPairsOnTokenDialogDismiss
        )
    }
}

@PreviewLight
@PreviewDark
@Composable
private fun UsbTokenInfoScreenPreview() {
    TokenInfoScreenPreview(
        uiState = CaTokenInfoUiState(
            TokenType.Usb, label = "Usb token", model = "Рутокен ЭЦП 2.0", serial = "7755345354"
        )
    )
}

@PreviewLight
@PreviewDark
@Composable
private fun NfcTokenInfoScreenPreview() {
    TokenInfoScreenPreview(
        uiState = CaTokenInfoUiState(
            TokenType.IsoNfc, label = "Nfc token", model = "Рутокен ЭЦП 3.0 NFC", serial = "1098751567"
        )
    )
}

@PreviewLight
@PreviewDark
@Composable
private fun UsbNfcTokenInfoScreenPreview() {
    TokenInfoScreenPreview(
        uiState = CaTokenInfoUiState(
            TokenType.UsbNfcDual, label = "Usb+Nfc token", model = "Рутокен ЭЦП 3.0 NFC (3100)", serial = "1098751567"
        )
    )
}

@Composable
private fun TokenInfoScreenPreview(uiState: CaTokenInfoUiState) {
    RutokenTechTheme {
        TokenInfoScreen(
            uiState = uiState,
            onNavigateToGenerateKeyPair = {},
            onNavigateToGenerateCertificate = {},
            onLogout = {},
            openDrawer = {}
        )
    }
}