package ru.rutoken.tech.bouncycastle

import org.bouncycastle.asn1.ASN1Encoding
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cms.CMSProcessableByteArray
import org.bouncycastle.cms.CMSSignedDataGenerator
import org.bouncycastle.cms.SignerInfoGeneratorBuilder
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPrivateKeyObject
import ru.rutoken.tech.bouncycastle.signature.GostContentSigner
import ru.rutoken.tech.bouncycastle.signature.makeSignatureByHashOid

fun signCmsDetachedWithBouncyCastle(
    session: Pkcs11Session,
    data: ByteArray,
    privateKey: Pkcs11GostPrivateKeyObject,
    certificate: X509CertificateHolder
): ByteArray {
    val signature = makeSignatureByHashOid(privateKey.getGostR3411ParamsAttributeValue(session).byteArrayValue, session)
    val signer = GostContentSigner(signature)
    signer.signInit(privateKey)
    val generator = CMSSignedDataGenerator().apply {
        addCertificate(certificate)
        addSignerInfoGenerator(SignerInfoGeneratorBuilder(signer.getDigestProvider()).build(signer, certificate))
    }

    return generator.generate(CMSProcessableByteArray(data)).getEncoded(ASN1Encoding.DER)
}
