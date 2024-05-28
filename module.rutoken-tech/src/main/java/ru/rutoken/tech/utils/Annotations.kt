/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.utils

/**
 * Annotates methods that contain any kind of workaround for unstable Android/Compose/etc. API. Such methods are
 * intended to be removed ASAP when the corresponding API becomes stable or fully functional.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "Self-written workaround for unstable Android/Compose/etc. API"
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class Workaround
