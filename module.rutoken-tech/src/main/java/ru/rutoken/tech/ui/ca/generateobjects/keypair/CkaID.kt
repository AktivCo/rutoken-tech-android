/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.ca.generateobjects.keypair

import kotlin.random.Random

private const val CKA_ID_SIZE = 5
private const val CKA_ID_SEPARATOR = ":"
private const val CKA_ID_DISPLAY_BYTE_LIMIT = 7

@JvmInline
value class CkaID(val rawValue: ByteArray = Random.nextBytes(CKA_ID_SIZE))

@OptIn(ExperimentalStdlibApi::class)
fun convertToDefaultString(ckaId: CkaID): String {
    return buildString {
        ckaId.rawValue.take(CKA_ID_DISPLAY_BYTE_LIMIT).forEachIndexed { index, byte ->
            append(byte.toHexString())
            if (index < ckaId.rawValue.lastIndex) append(CKA_ID_SEPARATOR)
        }
        if (ckaId.rawValue.size > CKA_ID_DISPLAY_BYTE_LIMIT) append("...")
    }
}
