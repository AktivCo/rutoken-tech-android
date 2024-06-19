/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.components.TextGroupItem
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight

@Composable
fun CertificateCard(
    name: String,
    position: String,
    certificateExpirationDate: String,
    organization: String? = null,
    algorithm: String? = null,
    errorText: String? = null,
    onClick: () -> Unit
) {
    val isError = !errorText.isNullOrEmpty()
    val alphaTint = if (isError) 0.5f else 1f
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = if (isError)
                    MaterialTheme.colorScheme.surfaceContainer
                else
                    MaterialTheme.colorScheme.surfaceContainerHighest,
            )
            .clickable(enabled = !isError, onClick = onClick)
            .padding(16.dp)
    )
    {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.alpha(alphaTint)
        )

        TextGroupItem(
            item = TextGroupItem(stringResource(id = R.string.position), position),
            alphaTint = alphaTint
        )

        organization?.let {
            TextGroupItem(
                item = TextGroupItem(stringResource(id = R.string.organization), organization),
                alphaTint = alphaTint
            )
        }

        algorithm?.let {
            TextGroupItem(
                item = TextGroupItem(stringResource(id = R.string.key_pair_algorithm), algorithm),
                alphaTint = alphaTint
            )
        }

        TextGroupItem(
            item = TextGroupItem(stringResource(id = R.string.certificate_expires), certificateExpirationDate),
            alphaTint = alphaTint
        )

        if (isError) {
            Text(
                text = errorText!!,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
@PreviewLight
@PreviewDark
private fun CertificateCardPreview() {
    RutokenTechTheme {
        Surface {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                CertificateCard(
                    name = "Иванов Михаил Романович",
                    position = "Дизайнер",
                    certificateExpirationDate = "07.03.2024",
                    onClick = {}
                )
                CertificateCard(
                    name = "Иванов Михаил Романович",
                    position = "Дизайнер",
                    certificateExpirationDate = "07.03.2024",
                    errorText = stringResource(id = R.string.certificate_is_expired),
                    onClick = {}
                )
                CertificateCard(
                    name = "Иванов Михаил Романович",
                    position = "Дизайнер",
                    certificateExpirationDate = "07.03.2024",
                    organization = "Рутокен",
                    algorithm = stringResource(id = R.string.gost256_algorithm),
                    errorText = stringResource(id = R.string.certificate_not_yet_valid, "07.03.2024"),
                    onClick = {}
                )
            }
        }
    }
}