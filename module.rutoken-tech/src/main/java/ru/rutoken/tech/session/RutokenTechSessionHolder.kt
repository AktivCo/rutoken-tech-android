/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.session

import ru.rutoken.tech.utils.logv
import java.util.concurrent.atomic.AtomicReference

class RutokenTechSessionHolder {
    private val _session: AtomicReference<RutokenTechSession> = AtomicReference()

    val session: RutokenTechSession?
        get() = _session.get()

    fun setSession(newSession: RutokenTechSession) {
        _session.set(newSession)
    }

    fun resetSession() {
        logv<RutokenTechSession> { "Clean up rutoken tech session" }
        _session.set(null)
    }
}

val RutokenTechSessionHolder.caSession: CaRutokenTechSession?
    get() = session as? CaRutokenTechSession

fun RutokenTechSessionHolder.requireCaSession(): CaRutokenTechSession = requireNotNull(caSession)