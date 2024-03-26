package ru.rutoken.tech.ui.utils

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.utils.Workaround

object Modifiers {
    val appBarIconSize = Modifier.size(48.dp)
}

fun Modifier.figmaPadding(top: Dp, right: Dp, bottom: Dp, left: Dp) = this.padding(left, top, right, bottom)

@Composable
fun Modifier.statusBarsPaddingHeight(): Modifier {
    return if (VERSION.SDK_INT < VERSION_CODES.R) {
        this.statusBarsPaddingWorkaround()
    } else {
        this.statusBarsPadding()
    }
}

@Composable
fun Modifier.navigationBarPaddingHeight(): Modifier {
    return if (VERSION.SDK_INT < VERSION_CODES.R) {
        this.navigationBarPaddingWorkaround()
    } else {
        this.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
    }
}

/**
 * Adds correct status bars padding on API 29 and older
 */
@Composable
@Workaround
private fun Modifier.statusBarsPaddingWorkaround(): Modifier =
    this.padding(WindowInsets.systemBarsIgnoringVisibility.asPaddingValues().calculateTopPadding())

/**
 * Adds correct navigation bar padding on API 29 and older
 */
@Composable
@Workaround
private fun Modifier.navigationBarPaddingWorkaround(): Modifier =
    this.height(WindowInsets.systemBarsIgnoringVisibility.asPaddingValues().calculateBottomPadding())
