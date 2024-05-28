/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.bouncycastle.digest

import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.operator.DigestCalculator
import org.bouncycastle.operator.DigestCalculatorProvider

class DigestProvider(val digestCalculator: DigestCalculator) : DigestCalculatorProvider {
    override fun get(algorithmIdentifier: AlgorithmIdentifier) = digestCalculator
}
