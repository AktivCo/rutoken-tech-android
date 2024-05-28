/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.tokenmanager.slotevent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.rutoken.pkcs11wrapper.main.IPkcs11Module
import ru.rutoken.pkcs11wrapper.main.Pkcs11Exception
import ru.rutoken.tech.utils.loge

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
                    loge<SlotEventGenerator>(e) { "Error while waiting for slot event" }
                }
            }
        }
    }
}
