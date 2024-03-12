package ru.rutoken.tech.ui.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Modifiers {
    val appBarIconSize = Modifier.size(48.dp)
}

fun Modifier.figmaPadding(top: Dp, right: Dp, bottom: Dp, left: Dp) = this.padding(left, top, right, bottom)