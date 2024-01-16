package ru.rutoken.tech.tokenmanager.slotevent

import androidx.annotation.AnyThread
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11Flag.CKF_TOKEN_PRESENT
import ru.rutoken.pkcs11wrapper.datatype.Pkcs11SlotInfo
import ru.rutoken.pkcs11wrapper.lowlevel.datatype.CkSlotInfo
import ru.rutoken.pkcs11wrapper.lowlevel.datatype.CkVersion
import ru.rutoken.pkcs11wrapper.main.Pkcs11Module
import java.util.concurrent.CopyOnWriteArraySet

class SlotEventProvider(private val pkcs11Module: Pkcs11Module) {
    private val listeners = CopyOnWriteArraySet<Listener>()
    private val channel = Channel<SlotEvent>(Channel.UNLIMITED)
    private val previousSlotEvent = mutableMapOf<Long, SlotEvent>()

    fun launchEvents(scope: CoroutineScope) {
        scope.launch {
            SlotEventGenerator(channel, pkcs11Module).launchGeneration(this)

            while (isActive) {
                handleSlotEvent(channel.receive())
            }
        }
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    private suspend fun handleSlotEvent(event: SlotEvent) {
        val slotId = event.slot.id
        previousSlotEvent[slotId]?.let {
            if (it.slotInfo.isTokenPresent == event.slotInfo.isTokenPresent)
                handleSlotEvent(makeFakeSlotEvent(it, event))
        }

        previousSlotEvent[slotId] = event
        withContext(Dispatchers.Default) {
            listeners.forEach { it.onSlotEvent(event) }
        }
    }

    private companion object {
        fun makeFakeSlotEvent(previousEvent: SlotEvent, newEvent: SlotEvent) =
            if (previousEvent.slotInfo.isTokenPresent)
                previousEvent.copyWithFlags(previousEvent.slotInfo.flags and CKF_TOKEN_PRESENT.asLong.inv())
            else
                newEvent.copyWithFlags(newEvent.slotInfo.flags or CKF_TOKEN_PRESENT.asLong)

        private fun SlotEvent.copyWithFlags(flags: Long): SlotEvent {
            val ckSlotInfo = object : CkSlotInfo {
                override fun getSlotDescription() = slotInfo.slotDescription.toByteArray()

                override fun getManufacturerID() = slotInfo.manufacturerId.toByteArray()

                override fun getFlags() = flags

                override fun getHardwareVersion() = object : CkVersion {
                    override fun getMajor() = slotInfo.hardwareVersion.major

                    override fun getMinor() = slotInfo.hardwareVersion.minor
                }

                override fun getFirmwareVersion() = object : CkVersion {
                    override fun getMajor() = slotInfo.firmwareVersion.major

                    override fun getMinor() = slotInfo.firmwareVersion.minor
                }
            }

            return SlotEvent(slot, Pkcs11SlotInfo(ckSlotInfo), isFake = true)
        }
    }

    @AnyThread
    interface Listener {
        fun onSlotEvent(event: SlotEvent)
    }
}
