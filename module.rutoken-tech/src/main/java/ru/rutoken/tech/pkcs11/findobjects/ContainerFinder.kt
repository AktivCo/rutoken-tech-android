package ru.rutoken.tech.pkcs11.findobjects

import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers.id_tc26_gost_3410_12_256
import org.bouncycastle.cert.X509CertificateHolder
import ru.rutoken.pkcs11wrapper.attribute.Pkcs11ByteArrayAttribute
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType
import ru.rutoken.pkcs11wrapper.datatype.Pkcs11KeyPair
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.pkcs11wrapper.`object`.certificate.Pkcs11X509PublicKeyCertificateObject
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11Gost256PrivateKeyObject
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11Gost256PublicKeyObject

/**
 * It is supposed that key pairs and certificates are linked by CKA_ID.
 */
fun Pkcs11Session.findGost256CertificateAndKeyContainers() =
    findGost256Containers().filterIsInstance<Gost256CertificateAndKeyContainer>()

fun Pkcs11Session.findGost256KeyContainers() = findGost256Containers().filterIsInstance<Gost256KeyContainer>()

fun Pkcs11Session.findGost256KeyPairByCkaId(ckaId: ByteArray)
        : Pkcs11KeyPair<Pkcs11Gost256PublicKeyObject, Pkcs11Gost256PrivateKeyObject> {
    val template = listOf(Pkcs11ByteArrayAttribute(Pkcs11AttributeType.CKA_ID, ckaId))
    val publicKey = objectManager.findObjectsAtOnce(Pkcs11Gost256PublicKeyObject::class.java, template).singleOrThrow()
    val privateKey =
        objectManager.findObjectsAtOnce(Pkcs11Gost256PrivateKeyObject::class.java, template).singleOrThrow()

    return Pkcs11KeyPair(publicKey, privateKey)
}

fun Pkcs11Session.findGost256CertificateByCkaId(ckaId: ByteArray): X509CertificateHolder {
    val template = listOf(Pkcs11ByteArrayAttribute(Pkcs11AttributeType.CKA_ID, ckaId))
    val certificate =
        objectManager.findObjectsAtOnce(Pkcs11X509PublicKeyCertificateObject::class.java, template).singleOrThrow()
    val x509CertificateHolder = X509CertificateHolder(certificate.getValueAttributeValue(this).byteArrayValue)

    if (x509CertificateHolder.subjectPublicKeyInfo.algorithm.algorithm != id_tc26_gost_3410_12_256)
        throw IllegalStateException("Found certificate's subjectPublicKeyInfo algorithm is not supported")

    return x509CertificateHolder
}

/**
 * It is supposed that key pairs and certificates are linked by CKA_ID.
 */
private fun Pkcs11Session.findGost256Containers(): List<Container> {
    val result = mutableListOf<Container>()

    val gost256KeyContainers = findGost256KeyPairs()
    val certificates = objectManager.findObjectsAtOnce(Pkcs11X509PublicKeyCertificateObject::class.java)

    for (certificate in certificates) {
        val x509CertificateHolder = X509CertificateHolder(certificate.getValueAttributeValue(this).byteArrayValue)

        if (x509CertificateHolder.subjectPublicKeyInfo.algorithm.algorithm != id_tc26_gost_3410_12_256)
            continue

        val ckaId = certificate.getIdAttributeValue(this).byteArrayValue
        val gost256KeyContainer = gost256KeyContainers.find { it.ckaId.contentEquals(ckaId) }

        if (gost256KeyContainer != null) {
            result.add(Gost256CertificateAndKeyContainer(ckaId, x509CertificateHolder, gost256KeyContainer.keyPair))
            gost256KeyContainers.remove(gost256KeyContainer)
        }
    }

    result.addAll(gost256KeyContainers)
    return result
}

/**
 * This method searches for all GOST 2012 256 key pairs and creates Gost256KeyContainers from them.
 */
private fun Pkcs11Session.findGost256KeyPairs(): MutableList<Gost256KeyContainer> {
    val result = mutableListOf<Gost256KeyContainer>()
    val publicKeys = objectManager.findObjectsAtOnce(Pkcs11Gost256PublicKeyObject::class.java)

    for (publicKey in publicKeys) {
        val ckaId = publicKey.getIdAttributeValue(this).byteArrayValue
        val template = listOf(Pkcs11ByteArrayAttribute(Pkcs11AttributeType.CKA_ID, ckaId))

        val privateKey = try {
            objectManager.findObjectsAtOnce(Pkcs11Gost256PrivateKeyObject::class.java, template).singleOrThrow()
        } catch (ignore: IllegalStateException) {
            continue
        }

        result.add(Gost256KeyContainer(ckaId, Pkcs11KeyPair(publicKey, privateKey)))
    }

    return result
}

private fun <T> Collection<T>.singleOrThrow() =
    singleOrNull() ?: throw IllegalStateException("One object required, but $size found")