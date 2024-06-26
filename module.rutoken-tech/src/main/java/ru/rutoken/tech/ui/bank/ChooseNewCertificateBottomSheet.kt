/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.components.BottomSheetDragHandle
import ru.rutoken.tech.ui.components.BottomSheetTitle
import ru.rutoken.tech.ui.components.NavigationBarSpacer
import ru.rutoken.tech.ui.components.bottomSheetCornerShape
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight
import ru.rutoken.tech.ui.utils.bottomSheetWindowInsets
import ru.rutoken.tech.ui.utils.expandedSheetState

@Composable
fun ChooseNewCertificateScreen(
    certificates: List<BankCertificate>,
    onCertificateClicked: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ChooseNewCertificateBottomSheet(
        certificates = certificates,
        sheetState = sheetState,
        onCertificateClicked = onCertificateClicked,
        onDismissRequest = onNavigateBack
    )
}

@Composable
private fun ChooseNewCertificateBottomSheet(
    certificates: List<BankCertificate>,
    sheetState: SheetState,
    onCertificateClicked: (Int) -> Unit,
    onDismissRequest: () -> Unit
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
            certificates.forEachIndexed { index, certificate ->
                CertificateCard(
                    name = certificate.name,
                    position = certificate.position,
                    certificateExpirationDate = certificate.certificateExpirationDate,
                    organization = certificate.organization,
                    algorithm = certificate.algorithm,
                    errorText = certificate.errorText,
                    onClick = { onCertificateClicked(index) }
                )
            }
            NavigationBarSpacer()
        }
    }
}

@Composable
@PreviewLight
@PreviewDark
private fun ChooseNewCertifficateBottomSheetPreview() {
    RutokenTechTheme {
        val name = "Иванов Михаил Романович"
        val algorithm = "ГОСТ Р 34.10-2012 256"
        val error = "Срок действия сертификата ещё не наступил"
        val derBytes = byteArrayOf(0, 0, 0, 0)
        val certificates = listOf(
            BankCertificate("id", derBytes, name, "Дизайнер", "07.03.2024", "Рутокен", algorithm),
            BankCertificate("id", derBytes, name, "Дизайнер", "07.03.2024", "Рутокен", algorithm, error),
            BankCertificate("id", derBytes, name, "Дизайнер", "07.03.2024", "Рутокен", algorithm),
            BankCertificate("id", derBytes, name, "Дизайнер", "07.03.2024", "Рутокен", algorithm, error)

        )
        ChooseNewCertificateBottomSheet(
            certificates = certificates,
            sheetState = expandedSheetState(),
            onCertificateClicked = {},
            onDismissRequest = {}
        )
    }
}