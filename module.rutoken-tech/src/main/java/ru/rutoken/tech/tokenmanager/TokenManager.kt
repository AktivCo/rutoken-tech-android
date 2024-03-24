package ru.rutoken.tech.tokenmanager

import kotlinx.coroutines.*
import ru.rutoken.pkcs11wrapper.main.Pkcs11Module
import ru.rutoken.pkcs11wrapper.main.Pkcs11Token
import ru.rutoken.tech.pkcs11.Pkcs11Launcher
import ru.rutoken.tech.pkcs11.getSerialNumber
import ru.rutoken.tech.pkcs11.getTokenModel
import ru.rutoken.tech.session.SerialHexString
import ru.rutoken.tech.tokenmanager.slotevent.SlotEvent
import ru.rutoken.tech.tokenmanager.slotevent.SlotEventProvider
import ru.rutoken.tech.ui.ca.tokeninfo.model.TokenModel
import ru.rutoken.tech.ui.ca.tokeninfo.model.isSupported
import ru.rutoken.tech.utils.loge
import java.util.Collections
import java.util.concurrent.atomic.AtomicReference

data class Pkcs11TokenData(val pkcs11Token: Pkcs11Token, val model: TokenModel)

class TokenManager : SlotEventProvider.Listener, Pkcs11Launcher.Listener {
    private val tokens = Collections.synchronizedMap<SerialHexString, Pkcs11TokenData>(mutableMapOf())
    private val waitTokenDeferred: AtomicReference<CompletableDeferred<Pkcs11TokenData>> = AtomicReference()

    private lateinit var eventJob: Job

    override fun onPkcs11Initialized(scope: CoroutineScope, pkcs11Module: Pkcs11Module) {
        eventJob = scope.launch {
            withContext(Dispatchers.IO) {
                pkcs11Module.getSlotList(true).forEach {
                    addTokenIfSupported(it.token)
                }
            }

            SlotEventProvider(pkcs11Module).also {
                it.addListener(this@TokenManager)
                it.launchEvents(this)
            }
        }
    }

    override fun beforePkcs11Finalize(scope: CoroutineScope, pkcs11Module: Pkcs11Module) {
        eventJob.cancel()
    }

    override suspend fun onSlotEvent(event: SlotEvent) {
        if (event.slotInfo.isTokenPresent)
            addTokenIfSupported(event.slot.token)
        else
            removeToken(event.slot.token)
    }

    fun getFirstTokenAsync(): Deferred<Pkcs11TokenData> {
        synchronized(tokens) {
            return when (tokens.size) {
                0 -> {
                    val deferred = waitTokenDeferred.get() ?: CompletableDeferred()
                    waitTokenDeferred.set(deferred)
                    return deferred
                }
                else -> CompletableDeferred(tokens.values.first())
            }
        }
    }

    fun getTokenBySerialNumber(serialNumber: SerialHexString): Pkcs11TokenData? = tokens[serialNumber]

    private suspend fun addTokenIfSupported(token: Pkcs11Token) {
        try {
            val tokenModel = token.getTokenModel()
            if (!tokenModel.isSupported) return
            val tokenData = Pkcs11TokenData(token, tokenModel)
            tokens[token.getSerialNumber()] = tokenData
            waitTokenDeferred.getAndSet(null)?.complete(tokenData)
        } catch (e: Exception) {
            loge(e) { "Adding a token to the token manager failed" }
        }
    }

    private fun removeToken(token: Pkcs11Token) {
        tokens.values.removeIf { it.pkcs11Token == token }
    }
}
