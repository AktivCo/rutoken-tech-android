/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.theme.RutokenTechTheme

data class ButtonContent(val text: String, val onClick: () -> Unit)

@Composable
fun BottomSheetTitle(title: String, buttonContent: ButtonContent? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1
        )
        buttonContent?.let {
            TextButton(
                onClick = it.onClick,
                modifier = Modifier.height(40.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(it.text, style = MaterialTheme.typography.labelLarge, maxLines = 1)
            }
        }

    }
}

@Composable
fun BottomSheetDragHandle(sheetState: SheetState) {
    val alpha by animateFloatAsState(
        targetValue = if (sheetState.targetValue != SheetValue.Expanded) {
            1f
        } else {
            0f
        }, animationSpec = tween(300),
        label = "dragHandleAlpha"
    )
    BottomSheetDefaults.DragHandle(
        Modifier.alpha(alpha)
    )
}

@Composable
fun bottomSheetCornerShape(sheetState: SheetState): Shape {
    val corners by animateDpAsState(
        targetValue = if (sheetState.targetValue != SheetValue.Expanded) {
            28.dp
        } else {
            0.dp
        },
        animationSpec = tween(300),
        label = "cornersRoundDp"
    )

    return RoundedCornerShape(corners)
}

@PreviewLightDark
@Composable
private fun BottomSheetTitlePreview() {
    RutokenTechTheme {
        Surface(Modifier.fillMaxWidth()) {
            BottomSheetTitle(stringResource(id = R.string.key_pair_title))
        }
    }
}

@PreviewLightDark
@Composable
private fun BottomSheetTitleButtonPreview() {
    RutokenTechTheme {
        Surface(Modifier.fillMaxWidth()) {
            BottomSheetTitle(stringResource(id = R.string.key_pair_title), ButtonContent("Close") {})
        }
    }
}