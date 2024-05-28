/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.utils

import android.util.Log
import ru.rutoken.tech.BuildConfig

fun interface LazyString {
    fun get(): String
}

object Logger {
    private const val DEFAULT_TAG = "ru.rutoken.tech"

    @JvmStatic
    fun shallLog(priority: Int) = priority >= BuildConfig.LOG_LEVEL

    @JvmStatic
    fun v(tag: String?, msg: LazyString) = println(Log.VERBOSE, tag, msg)

    @JvmStatic
    fun v(tag: String?, tr: Throwable, msg: LazyString) = println(Log.VERBOSE, tag, tr, msg)

    @JvmStatic
    fun d(tag: String?, msg: LazyString) = println(Log.DEBUG, tag, msg)

    @JvmStatic
    fun d(tag: String?, tr: Throwable, msg: LazyString) = println(Log.DEBUG, tag, tr, msg)

    @JvmStatic
    fun i(tag: String?, msg: LazyString) = println(Log.INFO, tag, msg)

    @JvmStatic
    fun i(tag: String?, tr: Throwable, msg: LazyString) = println(Log.INFO, tag, tr, msg)

    @JvmStatic
    fun w(tag: String?, msg: LazyString) = println(Log.WARN, tag, msg)

    @JvmStatic
    fun w(tag: String?, tr: Throwable, msg: LazyString) = println(Log.WARN, tag, tr, msg)

    @JvmStatic
    fun e(tag: String?, msg: LazyString) = println(Log.ERROR, tag, msg)

    @JvmStatic
    fun e(tag: String?, tr: Throwable, msg: LazyString) = println(Log.ERROR, tag, tr, msg)

    @JvmStatic
    fun wtf(tag: String?, msg: LazyString) = println(Log.ASSERT, tag, msg)

    @JvmStatic
    fun wtf(tag: String?, tr: Throwable, msg: LazyString) = println(Log.ASSERT, tag, tr, msg)

    private fun println(priority: Int, tag: String?, msg: LazyString) {
        if (shallLog(priority))
            Log.println(priority, tag ?: DEFAULT_TAG, msg.get())
    }

    private fun println(priority: Int, tag: String?, tr: Throwable, msg: LazyString) {
        if (shallLog(priority))
            Log.println(priority, tag ?: DEFAULT_TAG, "${msg.get()}\n${Log.getStackTraceString(tr)}")
    }
}

inline fun <reified T> logv(msg: LazyString) = Logger.v(T::class.qualifiedName, msg)
inline fun <reified T> logv(tr: Throwable, msg: LazyString) = Logger.v(T::class.qualifiedName, tr, msg)
inline fun <reified T> T.logv(msg: LazyString) = ru.rutoken.tech.utils.logv<T>(msg)
inline fun <reified T> T.logv(tr: Throwable, msg: LazyString) = ru.rutoken.tech.utils.logv<T>(tr, msg)

inline fun <reified T> logd(msg: LazyString) = Logger.d(T::class.qualifiedName, msg)
inline fun <reified T> logd(tr: Throwable, msg: LazyString) = Logger.d(T::class.qualifiedName, tr, msg)
inline fun <reified T> T.logd(msg: LazyString) = ru.rutoken.tech.utils.logd<T>(msg)
inline fun <reified T> T.logd(tr: Throwable, msg: LazyString) = ru.rutoken.tech.utils.logd<T>(tr, msg)

inline fun <reified T> logi(msg: LazyString) = Logger.i(T::class.qualifiedName, msg)
inline fun <reified T> logi(tr: Throwable, msg: LazyString) = Logger.i(T::class.qualifiedName, tr, msg)
inline fun <reified T> T.logi(msg: LazyString) = ru.rutoken.tech.utils.logi<T>(msg)
inline fun <reified T> T.logi(tr: Throwable, msg: LazyString) = ru.rutoken.tech.utils.logi<T>(tr, msg)

inline fun <reified T> logw(msg: LazyString) = Logger.w(T::class.qualifiedName, msg)
inline fun <reified T> logw(tr: Throwable, msg: LazyString) = Logger.w(T::class.qualifiedName, tr, msg)
inline fun <reified T> T.logw(msg: LazyString) = ru.rutoken.tech.utils.logw<T>(msg)
inline fun <reified T> T.logw(tr: Throwable, msg: LazyString) = ru.rutoken.tech.utils.logw<T>(tr, msg)

inline fun <reified T> loge(msg: LazyString) = Logger.e(T::class.qualifiedName, msg)
inline fun <reified T> loge(tr: Throwable, msg: LazyString) = Logger.e(T::class.qualifiedName, tr, msg)
inline fun <reified T> T.loge(msg: LazyString) = ru.rutoken.tech.utils.loge<T>(msg)
inline fun <reified T> T.loge(tr: Throwable, msg: LazyString) = ru.rutoken.tech.utils.loge<T>(tr, msg)

inline fun <reified T> logwtf(msg: LazyString) = Logger.wtf(T::class.qualifiedName, msg)
inline fun <reified T> logwtf(tr: Throwable, msg: LazyString) = Logger.wtf(T::class.qualifiedName, tr, msg)
inline fun <reified T> T.logwtf(msg: LazyString) = ru.rutoken.tech.utils.logwtf<T>(msg)
inline fun <reified T> T.logwtf(tr: Throwable, msg: LazyString) = ru.rutoken.tech.utils.logwtf<T>(tr, msg)
