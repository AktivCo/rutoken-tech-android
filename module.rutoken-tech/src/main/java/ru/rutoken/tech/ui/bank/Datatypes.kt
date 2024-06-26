/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank

import ru.rutoken.tech.session.CkaIdString

class BankUser(
    val id: Int,
    val name: String,
    val position: String,
    val certificateExpirationDate: String,
    val errorText: String? = null
)

class BankCertificate(
    val ckaIdString: CkaIdString,
    val bytes: ByteArray,
    val name: String,
    val position: String,
    val certificateExpirationDate: String,
    val organization: String,
    val algorithm: String,
    val errorText: String? = null
)
