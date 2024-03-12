package ru.rutoken.tech.ui.utils

import android.content.res.Configuration
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview

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
