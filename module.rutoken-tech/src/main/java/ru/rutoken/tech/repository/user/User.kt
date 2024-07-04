/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.repository.user

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.cert.X509CertificateHolder
import ru.rutoken.tech.database.user.UserEntity
import ru.rutoken.tech.utils.checkSubjectRdns
import ru.rutoken.tech.utils.getFullName
import ru.rutoken.tech.utils.getIssuerRdnValue
import java.util.Date

data class User(
    val userEntity: UserEntity,
    val fullName: String,
    val position: String?,
    val organization: String?,
    val certificateNotBefore: Date,
    val certificateNotAfter: Date,
    val inn: String?,
    val innle: String?,
    val ogrn: String?,
    val ogrnip: String?,
    val algorithmId: String?
)

private const val INN_OID = "1.2.643.3.131.1.1"
private const val INNLE_OID = "1.2.643.100.4"
private const val OGRN_OID = "1.2.643.100.1"
private const val OGRNIP_OID = "1.2.643.100.5"

fun makeUser(
    userEntity: UserEntity
): User {
    val certificate = X509CertificateHolder(userEntity.certificateDerValue).also { it.checkSubjectRdns() }
    return User(
        userEntity = userEntity,
        fullName = certificate.getFullName(),
        position = certificate.getIssuerRdnValue(BCStyle.T),
        organization = certificate.getIssuerRdnValue(BCStyle.O),
        certificateNotBefore = certificate.notBefore,
        certificateNotAfter = certificate.notAfter,
        inn = certificate.getIssuerRdnValue(ASN1ObjectIdentifier(INN_OID)),
        innle = certificate.getIssuerRdnValue(ASN1ObjectIdentifier(INNLE_OID)),
        ogrn = certificate.getIssuerRdnValue(ASN1ObjectIdentifier(OGRN_OID)),
        ogrnip = certificate.getIssuerRdnValue(ASN1ObjectIdentifier(OGRNIP_OID)),
        algorithmId = certificate.subjectPublicKeyInfo.algorithm.algorithm.id
    )
}