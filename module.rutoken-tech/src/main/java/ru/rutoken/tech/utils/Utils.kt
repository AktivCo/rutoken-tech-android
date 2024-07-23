/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.utils

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.cert.X509CertificateHolder
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

fun Date.toDateString(): String = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(this)

fun LocalDate.toDateString(): String =
    format(DateTimeFormatter.ofPattern("dd MMMM yyyy").withLocale(Locale("ru", "RU")))

fun LocalDateTime.toDateTimeString(): String =
    format(DateTimeFormatter.ofPattern("dd MMMM yyyy г. в HH:mm").withLocale(Locale("ru", "RU")))

fun X509CertificateHolder.checkSubjectRdns() {
    check(subject.rdNs.all { !it.isMultiValued }) { "Multiple RDN values with the same type" }
}

fun X509CertificateHolder.getFullName(): String {
    val cn = getIssuerRdnValue(BCStyle.CN)
    val surname = getIssuerRdnValue(BCStyle.SURNAME)
    val givenName = getIssuerRdnValue(BCStyle.GIVENNAME)

    val hasFullName = surname != null && givenName != null
    check(hasFullName || cn != null) { "Suitable RDNs are not found" }

    return if (hasFullName) "$surname $givenName" else cn!!
}

fun X509CertificateHolder.getIssuerRdnValue(type: ASN1ObjectIdentifier): String? {
    val rdn = subject.rdNs.find { it.first.type == type }
    return rdn?.first?.value?.toString()
}