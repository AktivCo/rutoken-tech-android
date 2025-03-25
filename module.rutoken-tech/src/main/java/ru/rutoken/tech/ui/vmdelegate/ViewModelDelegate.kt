/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.vmdelegate

import kotlinx.coroutines.CoroutineScope

interface ViewModelDelegate {
    val delegateScope: CoroutineScope
}
