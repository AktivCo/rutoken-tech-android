package ru.rutoken.tech.ui.components

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Bottom padding to compensate edge-to-edge application UI
 */
@Composable
fun NavigationBarSpacer() {
    Spacer(
        if (VERSION.SDK_INT < VERSION_CODES.R) {
            Modifier.height(WindowInsets.systemBarsIgnoringVisibility.asPaddingValues().calculateBottomPadding())
        } else {
            Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
        }
    )
}