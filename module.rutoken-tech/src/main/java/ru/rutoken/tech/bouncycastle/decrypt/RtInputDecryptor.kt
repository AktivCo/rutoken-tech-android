/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.bouncycastle.decrypt

import org.bouncycastle.asn1.cryptopro.GOST28147Parameters
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.operator.InputDecryptor
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11MechanismType.CKM_GOST28147
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.pkcs11wrapper.mechanism.Pkcs11Mechanism
import ru.rutoken.pkcs11wrapper.mechanism.parameter.Pkcs11ByteArrayMechanismParams
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11KeyObject
import java.io.ByteArrayInputStream
import java.io.InputStream

class RtInputDecryptor(
    private val session: Pkcs11Session,
    private val key: Pkcs11KeyObject,
    private val algorithmIdentifier: AlgorithmIdentifier
) : InputDecryptor {
    override fun getAlgorithmIdentifier() = algorithmIdentifier

    override fun getInputStream(encryptedInput: InputStream): InputStream {
        val data = ByteArray(encryptedInput.available())
        encryptedInput.use { it.read(data) }

        val gost28147Parameters = GOST28147Parameters.getInstance(algorithmIdentifier.parameters)
        val mechanism = Pkcs11Mechanism.make(CKM_GOST28147, Pkcs11ByteArrayMechanismParams(gost28147Parameters.iv))
        val decrypted = session.decryptionManager.decryptAtOnce(data, mechanism, key)

        return ByteArrayInputStream(decrypted)
    }
}
