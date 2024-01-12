package ru.rutoken.tech.koin

import org.koin.dsl.bind
import org.koin.dsl.module
import ru.rutoken.pkcs11wrapper.main.Pkcs11Module
import ru.rutoken.tech.pkcs11.RtPkcs11Module

val koinModule = module {
    single { RtPkcs11Module() } bind Pkcs11Module::class
}
