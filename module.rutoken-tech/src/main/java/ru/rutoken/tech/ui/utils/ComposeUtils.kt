package ru.rutoken.tech.ui.utils

import android.content.res.Configuration
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.launch
import ru.rutoken.tech.utils.Workaround

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

/**
 * Wraps focusable content and fixes keyboard content overlap on 30, 31 and 32 API versions.
 */
@Composable
@Workaround
fun ImeFocusHelper(itemToKeepVisible: @Composable (() -> Unit)) {
    if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU || VERSION.SDK_INT < VERSION_CODES.R) {
        itemToKeepVisible()
    } else {
        val scope = rememberCoroutineScope()
        val view = LocalView.current
        val bringIntoViewRequester = remember { BringIntoViewRequester() }

        DisposableEffect(view) {
            val listener = ViewTreeObserver.OnGlobalLayoutListener {
                scope.launch { bringIntoViewRequester.bringIntoView() }
            }
            view.viewTreeObserver.addOnGlobalLayoutListener(listener)
            onDispose { view.viewTreeObserver.removeOnGlobalLayoutListener(listener) }
        }

        Box(Modifier.bringIntoViewRequester(bringIntoViewRequester)) {
            itemToKeepVisible()
        }
    }
}

@Composable
fun bottomSheetWindowInsets(): WindowInsets {
    return if (VERSION.SDK_INT < VERSION_CODES.R) {
        workaroundInsets()
    } else {
        WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
    }
}

/**
 * Gets WindowInsets that fixes keyboard overlapping on API 29 and older
 */
@Composable
@Workaround
private fun workaroundInsets(): WindowInsets = WindowInsets.Companion.ime
