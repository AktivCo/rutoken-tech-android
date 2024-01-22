package ru.rutoken.tech.usecasestests.rule

import org.junit.Assume.assumeTrue
import org.junit.rules.ExternalResource
import ru.rutoken.pkcs11wrapper.attribute.Pkcs11Attribute
import ru.rutoken.pkcs11wrapper.datatype.Pkcs11KeyPair
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.pkcs11wrapper.mechanism.Pkcs11Mechanism
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11PrivateKeyObject
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11PublicKeyObject
import ru.rutoken.tech.pkcs11.createobjects.isMechanismSupported

class GenerateKeyPairRule<PublicKey : Pkcs11PublicKeyObject, PrivateKey : Pkcs11PrivateKeyObject>(
    private val publicKeyClass: Class<PublicKey>,
    private val privateKeyClass: Class<PrivateKey>,
    private val session: SessionRule,
    private val mechanism: Pkcs11Mechanism,
    private val publicKeyTemplate: List<Pkcs11Attribute>,
    private val privateKeyTemplate: List<Pkcs11Attribute>,
    private val beforeGenerationCheck: ((Pkcs11Session) -> Boolean)? = null
) : ExternalResource() {
    private lateinit var _value: Pkcs11KeyPair<PublicKey, PrivateKey>
    val value get() = _value

    override fun before() {
        assumeTrue(session.value.token.isMechanismSupported(mechanism.mechanismType))
        beforeGenerationCheck?.let { assumeTrue(it(session.value)) }

        _value = session.value.keyManager.generateKeyPair(
            publicKeyClass, privateKeyClass, mechanism, publicKeyTemplate, privateKeyTemplate
        )
    }

    override fun after() {
        session.value.objectManager.destroyKeyPair(_value)
    }
}
