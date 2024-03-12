package ru.rutoken.tech.utils

class BusinessRuleException : Exception {
    val case: BusinessRuleCase

    constructor(case: BusinessRuleCase) : super(case.name) {
        this.case = case
    }

    constructor(case: BusinessRuleCase, e: Throwable) : super(case.name, e) {
        this.case = case
    }
}

enum class BusinessRuleCase {
    WRONG_RUTOKEN,
    TOKEN_REMOVED
}
