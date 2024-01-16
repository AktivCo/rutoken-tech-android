package ru.rutoken.tech.tokenmanager.slotevent

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import ru.rutoken.pkcs11wrapper.main.IPkcs11Module
import ru.rutoken.pkcs11wrapper.main.Pkcs11Exception

/**
 * Fills a [Channel] with slot events from a [IPkcs11Module.waitForSlotEvent] function.
 */
class SlotEventGenerator(
    private val slotEventChannel: SendChannel<SlotEvent>,
    private val pkcs11Module: IPkcs11Module
) {
    fun launchGeneration(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    pkcs11Module.waitForSlotEvent(false)?.let {
                        slotEventChannel.send(SlotEvent(it, it.slotInfo))
                    }
                } catch (e: Pkcs11Exception) {
                    Log.e(SlotEventGenerator::class.qualifiedName, "Error while waiting for slot event", e)
                }
            }
        }
    }
}
