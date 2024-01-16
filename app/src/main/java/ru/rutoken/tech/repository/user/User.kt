package ru.rutoken.tech.repository.user

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.cert.X509CertificateHolder
import ru.rutoken.tech.database.user.UserEntity
import java.text.SimpleDateFormat
import java.util.*

data class User(
    val userEntity: UserEntity,
    val fullName: String,
    val position: String?,
    val organization: String?,
    val certificateExpires: String,
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
    val certificate = X509CertificateHolder(userEntity.certificateDerValue)

    check(certificate.subject.rdNs.all { !it.isMultiValued }) { "Multiple RDN values with the same type" }

    val cn = certificate.getIssuerRdnValue(BCStyle.CN)
    val surname = certificate.getIssuerRdnValue(BCStyle.SURNAME)
    val givenName = certificate.getIssuerRdnValue(BCStyle.GIVENNAME)

    val hasFullName = surname != null && givenName != null
    check(hasFullName || cn != null) { "Suitable RDNs are not found" }

    return User(
        userEntity = userEntity,
        fullName = if (hasFullName) "$surname $givenName" else cn!!,
        position = certificate.getIssuerRdnValue(BCStyle.T),
        organization = certificate.getIssuerRdnValue(BCStyle.O),
        certificateExpires = SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(certificate.notAfter),
        inn = certificate.getIssuerRdnValue(ASN1ObjectIdentifier(INN_OID)),
        innle = certificate.getIssuerRdnValue(ASN1ObjectIdentifier(INNLE_OID)),
        ogrn = certificate.getIssuerRdnValue(ASN1ObjectIdentifier(OGRN_OID)),
        ogrnip = certificate.getIssuerRdnValue(ASN1ObjectIdentifier(OGRNIP_OID)),
        algorithmId = certificate.subjectPublicKeyInfo.algorithm.algorithm.id
    )
}

private fun X509CertificateHolder.getIssuerRdnValue(type: ASN1ObjectIdentifier): String? {
    val rdn = subject.rdNs.find { it.first.type == type }
    return rdn?.first?.value?.toString()
}