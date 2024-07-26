/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ca

import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import ru.rutoken.tech.utils.base64ToPrivateKey
import ru.rutoken.tech.utils.decoded
import java.math.BigInteger
import java.time.Period
import java.time.ZonedDateTime
import java.util.Date
import java.util.Random

/**
 * Expires in 2045.
 */
private const val ROOT_CERTIFICATE_GOST = "MIIDGTCCAsSgAwIBAgIUGg1z23prjHDnKX/jgFTkmEIyGicwDAYIKoUDBwEBAwIF" +
        "ADCB8jEoMCYGA1UEAwwf0KDRg9GC0L7QutC10L0g0KLQtdGB0YIgUm9vdCBDQTEo" +
        "MCYGA1UECgwf0KDRg9GC0L7QutC10L0g0KLQtdGB0YIgUm9vdCBDQTEXMBUGA1UE" +
        "CwwO0KDRg9GC0L7QutC10L0xRTBDBgNVBAkMPNGD0LvQuNGG0LAg0KjQsNGA0LjQ" +
        "utC+0L/QvtC00YjQuNC/0L3QuNC60L7QstGB0LrQsNGPLCDQtC4gNTEVMBMGA1UE" +
        "BwwM0JzQvtGB0LrQstCwMRgwFgYDVQQIDA83NyDQnNC+0YHQutCy0LAxCzAJBgNV" +
        "BAYTAlJVMB4XDTI0MDgwMTE2MTEzNloXDTQ1MDEwMTE2MTEzNlowgfIxKDAmBgNV" +
        "BAMMH9Cg0YPRgtC+0LrQtdC9INCi0LXRgdGCIFJvb3QgQ0ExKDAmBgNVBAoMH9Cg" +
        "0YPRgtC+0LrQtdC9INCi0LXRgdGCIFJvb3QgQ0ExFzAVBgNVBAsMDtCg0YPRgtC+" +
        "0LrQtdC9MUUwQwYDVQQJDDzRg9C70LjRhtCwINCo0LDRgNC40LrQvtC/0L7QtNGI" +
        "0LjQv9C90LjQutC+0LLRgdC60LDRjywg0LQuIDUxFTATBgNVBAcMDNCc0L7RgdC6" +
        "0LLQsDEYMBYGA1UECAwPNzcg0JzQvtGB0LrQstCwMQswCQYDVQQGEwJSVTBeMBcG" +
        "CCqFAwcBAQEBMAsGCSqFAwcBAgEBAgNDAARAbFyV6nIImKslDnOVfu/qrqIozJMM" +
        "gsPuJSOkOl+/NLdtCbdoIH55801UmMHBTKbogikE33bp6ZLO8FAy5s3fIKMvMC0w" +
        "HQYDVR0OBBYEFOQrBlHYuiHdOh6crRB5X9w/vR9DMAwGA1UdEwQFMAMBAf8wDAYI" +
        "KoUDBwEBAwIFAANBAJaKaatBXt/qtLqB/XiEfh5xbtpIdpg98VT6rvVXNkVNuQVA" +
        "5gRwZBpkBd+BiKUJ9BNR/9zgbmAYBBB/Eun/XN8="

/**
 * Expires in 2045.
 */
private const val CA_CERTIFICATE_GOST = "MIIDMDCCAtugAwIBAgIUE5NIRWvjPZkfh8hLJOnvtL4TbIYwDAYIKoUDBwEBAwIF" +
        "ADCB8jEoMCYGA1UEAwwf0KDRg9GC0L7QutC10L0g0KLQtdGB0YIgUm9vdCBDQTEo" +
        "MCYGA1UECgwf0KDRg9GC0L7QutC10L0g0KLQtdGB0YIgUm9vdCBDQTEXMBUGA1UE" +
        "CwwO0KDRg9GC0L7QutC10L0xRTBDBgNVBAkMPNGD0LvQuNGG0LAg0KjQsNGA0LjQ" +
        "utC+0L/QvtC00YjQuNC/0L3QuNC60L7QstGB0LrQsNGPLCDQtC4gNTEVMBMGA1UE" +
        "BwwM0JzQvtGB0LrQstCwMRgwFgYDVQQIDA83NyDQnNC+0YHQutCy0LAxCzAJBgNV" +
        "BAYTAlJVMB4XDTI0MDgwMTE2MTEzNloXDTQ1MDEwMTE2MTEzNlowgegxIzAhBgNV" +
        "BAMMGtCg0YPRgtC+0LrQtdC9INCi0LXRgdGCIENBMSMwIQYDVQQKDBrQoNGD0YLQ" +
        "vtC60LXQvSDQotC10YHRgiBDQTEXMBUGA1UECwwO0KDRg9GC0L7QutC10L0xRTBD" +
        "BgNVBAkMPNGD0LvQuNGG0LAg0KjQsNGA0LjQutC+0L/QvtC00YjQuNC/0L3QuNC6" +
        "0L7QstGB0LrQsNGPLCDQtC4gNTEVMBMGA1UEBwwM0JzQvtGB0LrQstCwMRgwFgYD" +
        "VQQIDA83NyDQnNC+0YHQutCy0LAxCzAJBgNVBAYTAlJVMF4wFwYIKoUDBwEBAQEw" +
        "CwYJKoUDBwECAQECA0MABEDrdHXvrvhdK7HqeAD7NeuW9UV4perGn4//Vs505n3W" +
        "KnjZPS+d/M0h2oYrsYPCMkJG6jbzjAmQwFav4uLC0fmAo1AwTjAdBgNVHQ4EFgQU" +
        "xJPj5SRQsTsCmeUHxbaeP/WL3XEwDAYDVR0TBAUwAwEB/zAfBgNVHSMEGDAWgBTk" +
        "KwZR2Loh3ToenK0QeV/cP70fQzAMBggqhQMHAQEDAgUAA0EAgLkB6hhRYHlMg/qs" +
        "oVI5zxY/BkTQXeL/Ib2w63uSR+GjjSheLg0FZhIKllKw5xMZ+RRqLyM5v2zUNtv4" +
        "lICOwA=="

private const val CA_PRIVATE_KEY_GOST = "MEACAQAwFwYIKoUDBwEBAQEwCwYJKoUDBwECAQECBCIEIC/mU8VxtwxCevWktjiW" +
        "RXzQ1rxygoHwlz9p1UbYplht"

private val CA_CONFIG_GOST = LocalCAConfig(
    caCertificate = CA_CERTIFICATE_GOST,
    caPrivateKey = CA_PRIVATE_KEY_GOST,
    privateKeyAlgorithm = "ECGOST3410-2012",
    signatureAlgorithm = "GOST3411-2012-256WITHECGOST3410-2012-256",
    issuedCertificateValidityPeriod = Period.ofYears(1)
)

object LocalCA {
    val rootCertificate: ByteArray
        get() = ROOT_CERTIFICATE_GOST.decoded

    val caCertificate: ByteArray
        get() = CA_CONFIG_GOST.caCertificate.decoded

    fun issueCertificate(csr: ByteArray): ByteArray {
        return with(CA_CONFIG_GOST) {
            val caCertificateHolder = X509CertificateHolder(this@LocalCA.caCertificate)
            val certificationRequest = PKCS10CertificationRequest(csr)

            val notBefore = ZonedDateTime.now()
            val notAfter = notBefore + issuedCertificateValidityPeriod

            val certificateBuilder = X509v3CertificateBuilder(
                caCertificateHolder.subject,
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

            val caPrivateKey = base64ToPrivateKey(caPrivateKey, privateKeyAlgorithm)
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
