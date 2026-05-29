/*
 * Copyright (c) 2026, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.rtpki

import androidx.annotation.IntDef
import ru.rutoken.rtpki.RtPkiCmsCipherAlgorithm.RT_PKI_CMS_CIPHER_AES_128_CBC
import ru.rutoken.rtpki.RtPkiCmsCipherAlgorithm.RT_PKI_CMS_CIPHER_AES_192_CBC
import ru.rutoken.rtpki.RtPkiCmsCipherAlgorithm.RT_PKI_CMS_CIPHER_AES_256_CBC
import ru.rutoken.rtpki.RtPkiCmsCipherAlgorithm.RT_PKI_CMS_CIPHER_GOST28147
import ru.rutoken.rtpki.RtPkiCmsCipherAlgorithm.RT_PKI_CMS_CIPHER_KUZNECHIK_CTR_ACPKM
import ru.rutoken.rtpki.RtPkiCmsCipherAlgorithm.RT_PKI_CMS_CIPHER_KUZNECHIK_CTR_ACPKM_OMAC
import ru.rutoken.rtpki.RtPkiCmsCipherAlgorithm.RT_PKI_CMS_CIPHER_MAGMA_CTR_ACPKM
import ru.rutoken.rtpki.RtPkiCmsCipherAlgorithm.RT_PKI_CMS_CIPHER_MAGMA_CTR_ACPKM_OMAC

@IntDef(
    value = [
        RT_PKI_CMS_CIPHER_KUZNECHIK_CTR_ACPKM,
        RT_PKI_CMS_CIPHER_KUZNECHIK_CTR_ACPKM_OMAC,
        RT_PKI_CMS_CIPHER_MAGMA_CTR_ACPKM,
        RT_PKI_CMS_CIPHER_MAGMA_CTR_ACPKM_OMAC,
        RT_PKI_CMS_CIPHER_GOST28147,
        RT_PKI_CMS_CIPHER_AES_128_CBC,
        RT_PKI_CMS_CIPHER_AES_192_CBC,
        RT_PKI_CMS_CIPHER_AES_256_CBC,
    ]
)
annotation class RtPkiEncryptionAlgorithm
