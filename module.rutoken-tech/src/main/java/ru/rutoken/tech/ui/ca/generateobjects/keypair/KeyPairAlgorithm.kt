/*
 * Copyright (c) 2026, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.ca.generateobjects.keypair

import androidx.annotation.StringRes
import ru.rutoken.tech.R

enum class KeyPairAlgorithm(@StringRes val title: Int) {
    GOST_2012_256(R.string.gost256_algorithm),
    ECDSA_SECP256R1(R.string.ecdsa_secp256r1_algorithm),
}
