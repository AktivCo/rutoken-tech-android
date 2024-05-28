/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.utils

class BusinessRuleException : Exception {
    val case: BusinessRuleCase

    constructor(case: BusinessRuleCase) : super() {
        this.case = case
    }

    constructor(case: BusinessRuleCase, e: Throwable) : super(e) {
        this.case = case
    }
}

sealed class BusinessRuleCase {
    data object WrongRutoken : BusinessRuleCase()
    data object TokenRemoved : BusinessRuleCase()
    data object PinLocked : BusinessRuleCase()
    data object NoSuchKeyPair : BusinessRuleCase()
    class IncorrectPin(val retryLeft: Long) : BusinessRuleCase()
}
