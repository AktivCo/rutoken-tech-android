/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.pkcs11

import android.app.Activity
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.nsk.kstatemachine.Event
import ru.nsk.kstatemachine.StateMachine
import ru.nsk.kstatemachine.createStdLibStateMachine
import ru.nsk.kstatemachine.initialState
import ru.nsk.kstatemachine.invoke
import ru.nsk.kstatemachine.onTriggered
import ru.nsk.kstatemachine.processEventBlocking
import ru.nsk.kstatemachine.state
import ru.nsk.kstatemachine.transition
import ru.rutoken.pkcs11wrapper.datatype.Pkcs11InitializeArgs
import ru.rutoken.pkcs11wrapper.main.Pkcs11Module
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Binds pkcs11 library initialization to application activity lifecycle.
 * We do not want to keep pkcs11 running while activity is not in foreground.
 */
class Pkcs11Launcher(private val pkcs11Module: Pkcs11Module) : DefaultLifecycleObserver {
    private val scope = MainScope()
    private val pkcs11Channel = Channel<Pkcs11Event>(Channel.UNLIMITED)
    private val listeners = CopyOnWriteArraySet<Listener>()

    private val stateMachine = createStdLibStateMachine(this::class.simpleName) {
        logger = StateMachine.Logger { Log.d(this::class.qualifiedName, it()) }

        val finalizedState = initialState("Pkcs11 finalized")
        val initializedState = state("Pkcs11 initialized")

        finalizedState {
            transition<Pkcs11Event.InitializeEvent> {
                targetState = initializedState
                onTriggered { sendPkcs11Event(it.event) }
            }
        }

        initializedState {
            transition<Pkcs11Event.FinalizeEvent> {
                targetState = finalizedState
                onTriggered { sendPkcs11Event(it.event) }
            }
        }
    }

    init {
        scope.launch {
            while (isActive) {
                try {
                    when (pkcs11Channel.receive()) {
                        is Pkcs11Event.InitializeEvent -> initializePkcs11()
                        is Pkcs11Event.FinalizeEvent -> finalizePkcs11()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @MainThread
    fun launchPkcs11() = stateMachine.processEventBlocking(Pkcs11Event.InitializeEvent)

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    @MainThread
    override fun onStart(owner: LifecycleOwner) {
        stateMachine.processEventBlocking(Pkcs11Event.InitializeEvent)
    }

    @MainThread
    override fun onStop(owner: LifecycleOwner) {
        if (!(owner as Activity).isChangingConfigurations) {
            stateMachine.processEventBlocking(Pkcs11Event.FinalizeEvent)
        }
    }

    private fun sendPkcs11Event(event: Pkcs11Event) = scope.launch {
        pkcs11Channel.send(event)
    }

    private suspend fun initializePkcs11() {
        withContext(Dispatchers.IO) {
            pkcs11Module.initializeModule(Pkcs11InitializeArgs.Builder().setOsLockingOk(true).build())
        }
        listeners.forEach { it.onPkcs11Initialized(scope, pkcs11Module) }
    }

    private suspend fun finalizePkcs11() {
        listeners.forEach { it.beforePkcs11Finalize(scope, pkcs11Module) }
        withContext(Dispatchers.IO) {
            pkcs11Module.finalizeModule()
        }
    }

    @MainThread
    interface Listener {
        fun onPkcs11Initialized(scope: CoroutineScope, pkcs11Module: Pkcs11Module)
        fun beforePkcs11Finalize(scope: CoroutineScope, pkcs11Module: Pkcs11Module)
    }

    private sealed class Pkcs11Event {
        object FinalizeEvent : Event, Pkcs11Event()
        object InitializeEvent : Event, Pkcs11Event()
    }
}