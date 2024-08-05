/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.repository.shift

import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.cert.X509CertificateHolder
import ru.rutoken.tech.database.shift.ShiftUserEntity
import ru.rutoken.tech.utils.checkSubjectRdns
import ru.rutoken.tech.utils.getFullName
import ru.rutoken.tech.utils.getIssuerRdnValue
import java.util.Date

data class ShiftUser(
    val userEntity: ShiftUserEntity,
    val fullName: String,
    val position: String?,
    val organization: String?,
    val certificateNotBefore: Date,
    val certificateNotAfter: Date
)

fun makeShiftUser(userEntity: ShiftUserEntity): ShiftUser {
    val certificate = X509CertificateHolder(userEntity.certificateDerValue).also { it.checkSubjectRdns() }
    return ShiftUser(
        userEntity = userEntity,
        fullName = certificate.getFullName(),
        position = certificate.getIssuerRdnValue(BCStyle.T),
        organization = certificate.getIssuerRdnValue(BCStyle.O),
        certificateNotBefore = certificate.notBefore,
        certificateNotAfter = certificate.notAfter
    )
}