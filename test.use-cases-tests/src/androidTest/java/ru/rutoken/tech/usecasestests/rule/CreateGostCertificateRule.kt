/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.usecasestests.rule

import org.junit.rules.ExternalResource
import ru.rutoken.pkcs11wrapper.`object`.certificate.Pkcs11CertificateObject
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPrivateKeyObject
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPublicKeyObject
import ru.rutoken.tech.ca.LocalCA
import ru.rutoken.tech.pkcs11.createobjects.makeCertificateTemplate
import ru.rutoken.tech.ui.ca.generateobjects.certificate.EXTENSIONS
import ru.rutoken.tech.ui.ca.generateobjects.certificate.createDN
import ru.rutoken.tech.usecasestests.ID

class CreateGostCertificateRule(
    private val session: RtSessionRule,
    private val keyPair: GenerateKeyPairRule<Pkcs11GostPublicKeyObject, Pkcs11GostPrivateKeyObject>
) : ExternalResource() {
    private lateinit var _encoded: ByteArray
    private lateinit var _value: Pkcs11CertificateObject
    val encoded get() = _encoded
    val value get() = _value

    override fun before() {
        with(session.value) {
            val csr = createCsr(keyPair.value.publicKey, createDN("Ivanov"), keyPair.value.privateKey, null, EXTENSIONS)
            _encoded = LocalCA.issueCertificate(csr)
            _value = objectManager.createObject(
                Pkcs11CertificateObject::class.java,
                attributeFactory.makeCertificateTemplate(ID, _encoded)
            )
        }
    }

    override fun after() {
        session.value.objectManager.destroyObject(_value)
    }
}
