/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.shift.sign

import android.content.Context
import org.bouncycastle.cert.X509CertificateHolder
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPrivateKeyObject
import ru.rutoken.pkcs11wrapper.rutoken.main.RtPkcs11Session
import ru.rutoken.tech.ca.LocalCA
import ru.rutoken.tech.ui.shift.documents.Document
import ru.rutoken.tech.usecase.CmsOperations

suspend fun RtPkcs11Session.signDocuments(
    documents: List<Document>,
    applicationContext: Context,
    certificate: X509CertificateHolder,
    privateKey: Pkcs11GostPrivateKeyObject,
): List<Document> {

    return documents.map { document ->
        document.copy(
            signedCms = CmsOperations.signDetachedGost256Hardware(
                session = this@signDocuments,
                data = document.readFile(applicationContext),
                existingCmsBytes = document.signedCms,
                privateKey = privateKey,
                certificate = certificate,
                additionalCertificates = listOf(X509CertificateHolder(LocalCA.caCertificate))
            )
        )
    }
}