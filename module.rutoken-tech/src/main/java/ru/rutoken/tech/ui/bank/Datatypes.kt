/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank

import androidx.annotation.StringRes

class BankUser(
    val id: Int,
    val name: String,
    val position: String?,
    val certificateExpirationDate: String,
    val errorText: String? = null
)

class BankCertificate(
    val ckaId: ByteArray,
    val bytes: ByteArray,
    val name: String,
    val position: String?,
    val certificateExpirationDate: String,
    val organization: String?,
    @StringRes val algorithm: Int,
    val errorText: String? = null
)
