package ru.rutoken.tech.pkcs11

import ru.rutoken.pkcs11jna.RtPkcs11Constants.PKCS7_DETACHED_SIGNATURE
import ru.rutoken.pkcs11wrapper.`object`.certificate.Pkcs11CertificateObject
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPrivateKeyObject
import ru.rutoken.pkcs11wrapper.rutoken.main.RtPkcs11Session

fun RtPkcs11Session.signCmsDetachedWithPkcs11Wrapper(
    data: ByteArray,
    privateKey: Pkcs11GostPrivateKeyObject,
    certificate: Pkcs11CertificateObject,
): ByteArray = cmsManager.sign(data, certificate, privateKey, null, PKCS7_DETACHED_SIGNATURE)
