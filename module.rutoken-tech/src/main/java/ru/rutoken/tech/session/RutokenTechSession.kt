package ru.rutoken.tech.session

import ru.rutoken.tech.ui.ca.tokeninfo.model.TokenModel
import java.util.LinkedList

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