/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.ca.tokeninfo.model

import android.content.Context
import androidx.annotation.StringRes

interface BaseTokenModel : TokenModel {
    @get:StringRes
    val modelNameId: Int
    override fun getModelName(context: Context) = context.getString(modelNameId)
}