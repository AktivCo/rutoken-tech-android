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
import ru.rutoken.tech.tokenmanager.slotevent.SlotEvent
import ru.rutoken.tech.tokenmanager.slotevent.SlotEventProvider
import java.util.Collections

class TokenManager : SlotEventProvider.Listener, Pkcs11Launcher.Listener {
    private val tokens = Collections.synchronizedSet<Pkcs11Token>(mutableSetOf())
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

                else -> CompletableDeferred(tokens.first())
            }
        }
    }

    private fun addToken(token: Pkcs11Token) {
        tokens.add(token)
        waitTokenDeferred?.complete(token)
        waitTokenDeferred = null
    }

    private fun removeToken(token: Pkcs11Token) {
        tokens.remove(token)
    }
}
