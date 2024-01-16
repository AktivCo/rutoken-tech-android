package ru.rutoken.tech.pkcs11.createobjects

import ru.rutoken.pkcs11wrapper.constant.IPkcs11KeyType
import ru.rutoken.pkcs11wrapper.constant.IPkcs11MechanismType
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11KeyType.CKK_GOSTR3410
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11MechanismType.CKM_GOSTR3410_KEY_PAIR_GEN

private val TC26_GOST_3410_12_256_B_OID =
    byteArrayOf(0x06, 0x09, 0x2A, 0x85.toByte(), 0x03, 0x07, 0x01, 0x02, 0x01, 0x01, 0x02)
private val GOSTR3411_2012_256_OID = byteArrayOf(0x06, 0x08, 0x2a, 0x85.toByte(), 0x03, 0x07, 0x01, 0x01, 0x02, 0x02)

enum class GostKeyPairParams(
    val mechanismType: IPkcs11MechanismType,
    val keyType: IPkcs11KeyType,
    val paramset3410: ByteArray,
    val paramset3411: ByteArray
) {
    GOST_2012_256(
        CKM_GOSTR3410_KEY_PAIR_GEN,
        CKK_GOSTR3410,
        TC26_GOST_3410_12_256_B_OID,
        GOSTR3411_2012_256_OID
    )
}
