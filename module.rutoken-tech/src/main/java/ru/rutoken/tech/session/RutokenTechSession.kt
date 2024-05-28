/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.session

import ru.rutoken.tech.ui.ca.tokeninfo.model.TokenModel

typealias SerialHexString = String
typealias CkaIdString = String

abstract class RutokenTechSession

data class CaRutokenTechSession(
    val tokenUserPin: String,
    val tokenSerial: SerialHexString,
    val tokenModel: TokenModel,
    val tokenLabel: String,
    val keyPairs: MutableList<CkaIdString>
) : RutokenTechSession()