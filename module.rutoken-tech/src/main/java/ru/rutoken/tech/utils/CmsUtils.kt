/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.utils

import org.bouncycastle.cert.X509CertificateHolder
import ru.rutoken.tech.ui.bank.payments.Base64String
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

enum class VerifyCmsResult {
    SUCCESS,
    SIGNATURE_INVALID,
    CERTIFICATE_CHAIN_NOT_VERIFIED
}

fun ByteArray.toBase64String(): Base64String = Base64.getEncoder().encodeToString(this)

val Base64String.decoded: ByteArray get() = Base64.getDecoder().decode(this)

fun base64ToPrivateKey(privateKey: Base64String, privateKeyAlgorithm: String): PrivateKey {
    val caPrivateKeySpec = PKCS8EncodedKeySpec(privateKey.decoded)
    val keyFactory = KeyFactory.getInstance(privateKeyAlgorithm)
    return keyFactory.generatePrivate(caPrivateKeySpec)
}

fun base64ToX509CertificateHolder(certificate: Base64String) = X509CertificateHolder(certificate.decoded)