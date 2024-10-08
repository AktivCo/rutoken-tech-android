/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.utils

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder

fun figmaPaddingValues(top: Dp, right: Dp, bottom: Dp, left: Dp) = PaddingValues(left, top, right, bottom)

@Preview(
    device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420",
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
)
annotation class PreviewLight

@Preview(
    device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
annotation class PreviewDark

@Composable
fun expandedSheetState(skipPartiallyExpanded: Boolean = true) = SheetState(
    skipPartiallyExpanded = skipPartiallyExpanded,
    density = LocalDensity.current,
    initialValue = SheetValue.Expanded
)

@Composable
fun bottomSheetWindowInsets(): WindowInsets {
    return WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
}

fun NavOptionsBuilder.clearNavGraph(navController: NavHostController) {
    popUpTo(navController.graph.id) { inclusive = true }
}
