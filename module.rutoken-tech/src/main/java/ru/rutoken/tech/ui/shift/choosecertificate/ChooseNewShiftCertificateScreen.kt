/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.shift.choosecertificate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.Certificate
import ru.rutoken.tech.ui.components.BottomSheetDragHandle
import ru.rutoken.tech.ui.components.BottomSheetTitle
import ru.rutoken.tech.ui.components.CertificateCard
import ru.rutoken.tech.ui.components.NavigationBarSpacer
import ru.rutoken.tech.ui.components.ProgressIndicatorDialog
import ru.rutoken.tech.ui.components.alertdialog.ErrorAlertDialog
import ru.rutoken.tech.ui.components.bottomSheetCornerShape
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.bottomSheetWindowInsets
import ru.rutoken.tech.ui.utils.expandedSheetState

@Composable
fun ChooseNewShiftCertificateScreen(
    viewModel: ChooseNewShiftCertificateViewModel,
    onNavigateToDocumentsScreen: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    var showBottomSheet by remember { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val certificates by viewModel.certificates.observeAsState(listOf())

    if (certificates.isEmpty()) {
        ErrorAlertDialog(
            title = stringResource(id = R.string.rutoken_has_no_certificates),
            text = stringResource(id = R.string.use_ca_to_create_certificate),
            onDismissOrConfirm = onNavigateBack
        )
    } else {
        if (showBottomSheet) {
            ChooseNewCertificateBottomSheet(
                certificates = certificates,
                sheetState = sheetState,
                onCertificateClicked = viewModel::onCertificateClicked,
                onDismissRequest = onNavigateBack
            )
        }
    }

    ProgressIndicatorDialog(viewModel)

    val userAdded by viewModel.isUserAdded.observeAsState(false)
    val scope = rememberCoroutineScope()

    LaunchedEffect(userAdded) {
        if (userAdded) {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                showBottomSheet = false
                onNavigateToDocumentsScreen()
            }
        }
    }
}

@Composable
private fun ChooseNewCertificateBottomSheet(
    certificates: List<Certificate>,
    sheetState: SheetState,
    onCertificateClicked: (Certificate) -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentWindowInsets = { bottomSheetWindowInsets() },
        dragHandle = { BottomSheetDragHandle(sheetState = sheetState) },
        shape = bottomSheetCornerShape(sheetState = sheetState),
    ) {
        BottomSheetTitle(title = stringResource(id = R.string.choose_certificate))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top)
        ) {
            certificates.forEach { certificate ->
                CertificateCard(
                    name = certificate.name,
                    position = certificate.position ?: stringResource(R.string.not_set),
                    certificateExpirationDate = certificate.certificateExpirationDate,
                    organization = certificate.organization ?: stringResource(R.string.not_set),
                    algorithm = stringResource(certificate.algorithm),
                    errorText = certificate.errorText,
                    onClick = { onCertificateClicked(certificate) }
                )
            }
            NavigationBarSpacer()
        }
    }
}

@Composable
private fun ProgressIndicatorDialog(viewModel: ChooseNewShiftCertificateViewModel) {
    val showProgress by viewModel.showProgress.observeAsState(false)

    if (showProgress) {
        ProgressIndicatorDialog()
    }
}

@Composable
@PreviewLightDark
private fun ChooseNewCertificateBottomSheetPreview() {
    RutokenTechTheme {
        val name = "Иванов Михаил Романович"
        val algorithm = R.string.gost256_algorithm
        val error = "Срок действия сертификата ещё не наступил"
        val derBytes = byteArrayOf(0, 0, 0, 0)
        val ckaIdBytes = byteArrayOf(1, 2, 3)
        val certificates = listOf(
            Certificate(ckaIdBytes, derBytes, name, "Дизайнер", "07.03.2024", "Рутокен", algorithm),
            Certificate(ckaIdBytes, derBytes, name, "Дизайнер", "07.03.2024", "Рутокен", algorithm, error),
            Certificate(ckaIdBytes, derBytes, name, "Дизайнер", "07.03.2024", "Рутокен", algorithm),
            Certificate(ckaIdBytes, derBytes, name, "Дизайнер", "07.03.2024", "Рутокен", algorithm, error)
        )
        ChooseNewCertificateBottomSheet(
            certificates = certificates,
            sheetState = expandedSheetState(),
            onCertificateClicked = {},
            onDismissRequest = {}
        )
    }
}