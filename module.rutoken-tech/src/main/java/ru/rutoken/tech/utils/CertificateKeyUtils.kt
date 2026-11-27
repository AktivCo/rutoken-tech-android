/*
 * Copyright (c) 2026, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.utils

import org.bouncycastle.asn1.x9.X9ObjectIdentifiers
import org.bouncycastle.cert.X509CertificateHolder

fun X509CertificateHolder.hasEcPublicKey() = subjectPublicKeyInfo.algorithm.algorithm == X9ObjectIdentifiers.id_ecPublicKey

fun isEcCertificate(certificate: ByteArray) = X509CertificateHolder(certificate).hasEcPublicKey()