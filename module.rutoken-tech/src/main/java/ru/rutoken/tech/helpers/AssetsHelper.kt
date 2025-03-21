/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.helpers

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssetsHelper(private val context: Context) {
    suspend fun loadAsset(assetName: String): ByteArray =
        withContext(Dispatchers.IO) { context.assets.open(assetName).use { it.readBytes() } }
}
