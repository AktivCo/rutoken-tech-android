/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun SegmentedButtonRow(
    onLeftSectionClicked: () -> Unit,
    onRightSectionClicked: () -> Unit,
    isLeftSectionSelected: Boolean,
    leftSectionText: String,
    rightSectionText: String
) {
    var selectedIndex by remember { mutableIntStateOf(if (isLeftSectionSelected) 0 else 1) }
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        listOf(leftSectionText, rightSectionText).forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                onClick = {
                    selectedIndex = index
                    if (selectedIndex == 0) onLeftSectionClicked() else onRightSectionClicked()
                },
                selected = index == selectedIndex
            ) {
                Text(text = label)
            }
        }
    }
}