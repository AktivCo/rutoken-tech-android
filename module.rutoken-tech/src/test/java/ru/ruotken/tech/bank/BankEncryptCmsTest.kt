package ru.ruotken.tech.bank

import io.kotest.assertions.throwables.shouldNotThrowAny
import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cms.CMSAlgorithm.GOST28147_GCFB
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.rutoken.tech.bank.BANK_CERTIFICATE_GOST
import ru.rutoken.tech.bouncycastle.BouncyCastleCmsOperations
import ru.rutoken.tech.bouncycastle.Gost2012KeyFactorySpi
import java.security.Security
import java.util.Base64

class BankEncryptCmsTest {
    private val bcProvider = BouncyCastleProvider()

    @Before
    fun setUpSecurityProvider() {
        bcProvider.addKeyInfoConverter(RosstandartObjectIdentifiers.id_tc26_gost_3410_12_256, Gost2012KeyFactorySpi())
        // Remove system Bouncy Castle provider
        Security.removeProvider(bcProvider.name)
        Security.insertProviderAt(bcProvider, 1)
    }

    @After
    fun removeSecurityProvider() {
        Security.removeProvider(bcProvider.name)
    }

    @Test
    fun encrypt() {
        shouldNotThrowAny {
            val bankCertificate = Base64.getDecoder().decode(BANK_CERTIFICATE_GOST)
            val bankCertificateHolder = X509CertificateHolder(bankCertificate)

            BouncyCastleCmsOperations.encrypt(DATA, bankCertificateHolder, GOST28147_GCFB)
        }
    }

    companion object {
        private val DATA = byteArrayOf(0x01, 0x02, 0x03)
    }
}
