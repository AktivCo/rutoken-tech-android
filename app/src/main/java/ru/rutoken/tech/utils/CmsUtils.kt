package ru.rutoken.tech.utils

import org.bouncycastle.asn1.cms.ContentInfo
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import java.io.StringWriter

fun cmsToPem(encodedCms: ByteArray): String {
    val stringWriter = StringWriter()
    JcaPEMWriter(stringWriter).use {
        it.writeObject(ContentInfo.getInstance(encodedCms))
        it.flush()
    }

    return stringWriter.toString()
}
