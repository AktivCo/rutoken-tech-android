/*
 * Copyright (c) 2026, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.pkcs11.findobjects

import ru.rutoken.pkcs11wrapper.main.Pkcs11Session

suspend fun Pkcs11Session.findCertificateAndKeyContainers(): List<CertificateAndKeyContainer> =
    findGost256CertificateAndKeyContainers() + findEcCertificateAndKeyContainers()

suspend fun Pkcs11Session.findCertificateAndKeyContainer(
    ckaId: ByteArray,
    certificateBytes: ByteArray,
): CertificateAndKeyContainer =
    findCertificateAndKeyContainers().filter { container ->
        container.ckaId.contentEquals(ckaId) &&
                container.certificate.encoded.contentEquals(certificateBytes)
    }.singleOrThrow()