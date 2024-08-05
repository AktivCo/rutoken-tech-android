/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.repository.bank

import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.cert.X509CertificateHolder
import ru.rutoken.tech.database.bank.BankUserEntity
import ru.rutoken.tech.utils.checkSubjectRdns
import ru.rutoken.tech.utils.getFullName
import ru.rutoken.tech.utils.getIssuerRdnValue
import java.util.Date

data class BankUser(
    val userEntity: BankUserEntity,
    val fullName: String,
    val position: String?,
    val certificateNotBefore: Date,
    val certificateNotAfter: Date
)

fun makeBankUser(userEntity: BankUserEntity): BankUser {
    val certificate = X509CertificateHolder(userEntity.certificateDerValue).also { it.checkSubjectRdns() }
    return BankUser(
        userEntity = userEntity,
        fullName = certificate.getFullName(),
        position = certificate.getIssuerRdnValue(BCStyle.T),
        certificateNotBefore = certificate.notBefore,
        certificateNotAfter = certificate.notAfter
    )
}