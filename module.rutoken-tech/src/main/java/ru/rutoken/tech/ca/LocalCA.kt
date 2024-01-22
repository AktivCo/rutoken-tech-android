package ru.rutoken.tech.ca

import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import java.math.BigInteger
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Period
import java.time.ZonedDateTime
import java.util.Base64
import java.util.Date
import java.util.Random

/**
 * Expires in 2045.
 */
private const val CA_CERTIFICATE_GOST = "MIIBTzCB+wIJAMGuFHcbok4sMAwGCCqFAwcBAQMCBQAwKzELMAkGA1UEBhMCUlUx" +
        "CzAJBgNVBAMMAkNBMQ8wDQYDVQQIDAZNb3Njb3cwHhcNMTgwNjA2MTAyNzA4WhcN" +
        "NDUxMDIyMTAyNzA4WjArMQswCQYDVQQGEwJSVTELMAkGA1UEAwwCQ0ExDzANBgNV" +
        "BAgMBk1vc2NvdzBmMB8GCCqFAwcBAQEBMBMGByqFAwICIwEGCCqFAwcBAQICA0MA" +
        "BECM6iQnPgDs6K2jmUVLHf4V63xwO2j4vO2X2kNQVELu2bROK+wBaNWkTX5TW+IO" +
        "9gLZFioYMSEK2LxsIO3Zf+JeMAwGCCqFAwcBAQMCBQADQQATx6Ksy1KUuvfa2q8X" +
        "kfo3pDN1x1aGo4AmQolzEpbXvzbyMy3vk+VOqegdd8KP4E3x43zaTmHmnu/G1v20" +
        "VzwO"

private const val CA_PRIVATE_KEY_GOST = "MEgCAQAwHwYIKoUDBwEBAQEwEwYHKoUDAgIjAQYIKoUDBwEBAgIEIgQgHZQED2H6" +
        "/QSaiwT1uLTmx9S6dK48xAnrT5/xc/R0+P0="

private val CA_CONFIG_GOST = LocalCAConfig(
    caCertificate = CA_CERTIFICATE_GOST,
    caPrivateKey = CA_PRIVATE_KEY_GOST,
    privateKeyAlgorithm = "ECGOST3410-2012",
    signatureAlgorithm = "GOST3411-2012-256WITHECGOST3410-2012-256",
    issuedCertificateValidityPeriod = Period.ofYears(1)
)

object LocalCA {
    val rootCertificate: ByteArray
        get() = Base64.getDecoder().decode(CA_CONFIG_GOST.caCertificate)

    fun issueCertificate(csr: ByteArray): ByteArray {
        return with(CA_CONFIG_GOST) {
            val caCertificateHolder = X509CertificateHolder(rootCertificate)
            val certificationRequest = PKCS10CertificationRequest(csr)

            val notBefore = ZonedDateTime.now()
            val notAfter = notBefore + issuedCertificateValidityPeriod

            val certificateBuilder = X509v3CertificateBuilder(
                caCertificateHolder.issuer,
                BigInteger(160, Random()).also { it.setBit(0) },
                Date.from(notBefore.toInstant()),
                Date.from(notAfter.toInstant()),
                certificationRequest.subject,
                certificationRequest.subjectPublicKeyInfo
            )

            val caPrivateKeySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(caPrivateKey))
            val keyFactory = KeyFactory.getInstance(privateKeyAlgorithm)
            val caPrivateKey = keyFactory.generatePrivate(caPrivateKeySpec)

            val signer = JcaContentSignerBuilder(signatureAlgorithm).build(caPrivateKey)

            certificateBuilder.build(signer).encoded
        }
    }
}

private data class LocalCAConfig(
    val caCertificate: String,
    val caPrivateKey: String,
    val privateKeyAlgorithm: String,
    val signatureAlgorithm: String,
    val issuedCertificateValidityPeriod: Period
)