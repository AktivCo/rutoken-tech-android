package ru.rutoken.tech.pkcs11

import java.util.LinkedList
import java.util.concurrent.atomic.AtomicReference

typealias SerialHexString = String
typealias CkaIdString = String

data class TokenContext(
    val tokenUserPin: String,
    val tokenSerial: SerialHexString,
    val keyPairs: LinkedList<CkaIdString>
)

class TokenContextStorage {
    private val context: AtomicReference<TokenContext> = AtomicReference()

    val currentContext: TokenContext?
        get() = context.get()

    fun requireCurrentContext() = requireNotNull(currentContext)

    fun updateContext(tokenContext: TokenContext) {
        context.set(tokenContext)
    }

    fun resetContext() {
        context.set(null)
    }
}
