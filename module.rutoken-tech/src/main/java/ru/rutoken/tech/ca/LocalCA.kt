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
private const val CA_CERTIFICATE_GOST = "MIIDDzCCArqgAwIBAgIUJxS8qHxUeUlscrFsnAWghwqH4RwwDAYIKoUDBwEBAwIF" +
        "ADCB6DEjMCEGA1UEAwwa0KDRg9GC0L7QutC10L0g0KLQtdGB0YIgQ0ExIzAhBgNV" +
        "BAoMGtCg0YPRgtC+0LrQtdC9INCi0LXRgdGCIENBMRcwFQYDVQQLDA7QoNGD0YLQ" +
        "vtC60LXQvTFFMEMGA1UECQw80YPQu9C40YbQsCDQqNCw0YDQuNC60L7Qv9C+0LTR" +
        "iNC40L/QvdC40LrQvtCy0YHQutCw0Y8sINC0LiA1MRUwEwYDVQQHDAzQnNC+0YHQ" +
        "utCy0LAxGDAWBgNVBAgMDzc3INCc0L7RgdC60LLQsDELMAkGA1UEBhMCUlUwHhcN" +
        "MjQwNTIyMTY0NzUxWhcNNDUwMTA5MTY0NzUxWjCB6DEjMCEGA1UEAwwa0KDRg9GC" +
        "0L7QutC10L0g0KLQtdGB0YIgQ0ExIzAhBgNVBAoMGtCg0YPRgtC+0LrQtdC9INCi" +
        "0LXRgdGCIENBMRcwFQYDVQQLDA7QoNGD0YLQvtC60LXQvTFFMEMGA1UECQw80YPQ" +
        "u9C40YbQsCDQqNCw0YDQuNC60L7Qv9C+0LTRiNC40L/QvdC40LrQvtCy0YHQutCw" +
        "0Y8sINC0LiA1MRUwEwYDVQQHDAzQnNC+0YHQutCy0LAxGDAWBgNVBAgMDzc3INCc" +
        "0L7RgdC60LLQsDELMAkGA1UEBhMCUlUwaDAhBggqhQMHAQEBATAVBgkqhQMHAQIB" +
        "AQEGCCqFAwcBAQICA0MABEB0UsRqP/NIohrEQDExXW37SnbZZrCIZ1pTd0/xArcF" +
        "qc51238/UFgJQesoOlpl8iND+wdNFRJZ/ORgxLoiSRBXoy8wLTAdBgNVHQ4EFgQU" +
        "DMaJy55gyK+zsMtqhANQG2ljoQwwDAYDVR0TBAUwAwEB/zAMBggqhQMHAQEDAgUA" +
        "A0EADnlQoAheYVww+lB0o88DAvOrXEg7XoBI/F2OhH4OPNEtXHCzDDvBD6UrpKzu" +
        "L3fOQhNu8LtLkWRkfc+19AiIhA=="

private const val CA_PRIVATE_KEY_GOST = "MEoCAQAwIQYIKoUDBwEBAQEwFQYJKoUDBwECAQEBBggqhQMHAQECAgQiBCCkUvCd" +
        "FzC2FFrvtAzLUlSBlW3myKWAiXhAgwu7rqd8HA=="

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

            val certificateExtensions = certificationRequest.requestedExtensions
            certificateExtensions.extensionOIDs.forEach { oid ->
                val extension = certificateExtensions.getExtension(oid)
                certificateBuilder.addExtension(oid, extension.isCritical, extension.parsedValue)
            }

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
