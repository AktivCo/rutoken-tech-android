package ru.rutoken.tech.tokenmanager

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.rutoken.pkcs11wrapper.main.Pkcs11Module
import ru.rutoken.pkcs11wrapper.main.Pkcs11Token
import ru.rutoken.tech.pkcs11.Pkcs11Launcher
import ru.rutoken.tech.pkcs11.SerialHexString
import ru.rutoken.tech.pkcs11.getSerialNumber
import ru.rutoken.tech.tokenmanager.slotevent.SlotEvent
import ru.rutoken.tech.tokenmanager.slotevent.SlotEventProvider
import ru.rutoken.tech.utils.loge
import java.util.Collections

class TokenManager : SlotEventProvider.Listener, Pkcs11Launcher.Listener {
    private val tokens = Collections.synchronizedMap<SerialHexString, Pkcs11Token>(mutableMapOf())
    private var waitTokenDeferred: CompletableDeferred<Pkcs11Token>? = null

    private lateinit var eventJob: Job

    override fun onPkcs11Initialized(scope: CoroutineScope, pkcs11Module: Pkcs11Module) {
        eventJob = scope.launch {
            val tokenSlots = withContext(Dispatchers.IO) { pkcs11Module.getSlotList(true) }
            tokenSlots.forEach { addToken(it.token) }

            SlotEventProvider(pkcs11Module).also {
                it.addListener(this@TokenManager)
                it.launchEvents(this)
            }
        }
    }

    override fun beforePkcs11Finalize(scope: CoroutineScope, pkcs11Module: Pkcs11Module) {
        eventJob.cancel()
    }

    override fun onSlotEvent(event: SlotEvent) {
        if (event.slotInfo.isTokenPresent)
            addToken(event.slot.token)
        else
            removeToken(event.slot.token)
    }

    fun getFirstTokenAsync(): Deferred<Pkcs11Token> {
        synchronized(tokens) {
            return when (tokens.size) {
                0 -> {
                    waitTokenDeferred = waitTokenDeferred ?: CompletableDeferred()
                    return waitTokenDeferred!!
                }

                else -> CompletableDeferred(tokens.values.first())
            }
        }
    }

    fun getTokenBySerialNumber(serialNumber: SerialHexString): Pkcs11Token? = tokens[serialNumber]

    private fun addToken(token: Pkcs11Token) {
        try {
            tokens[token.getSerialNumber()] = token
            waitTokenDeferred?.complete(token)
            waitTokenDeferred = null
        } catch (e: Exception) {
            loge(e) { "Adding a token to the token manager failed" }
        }
    }

    private fun removeToken(token: Pkcs11Token) {
        tokens.values.remove(token)
    }
}
