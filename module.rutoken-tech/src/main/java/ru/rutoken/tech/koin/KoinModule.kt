/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.koin

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.rutoken.pkcs11wrapper.main.Pkcs11Module
import ru.rutoken.tech.database.Database
import ru.rutoken.tech.pkcs11.Pkcs11Launcher
import ru.rutoken.tech.pkcs11.RtPkcs11Module
import ru.rutoken.tech.repository.UserRepository
import ru.rutoken.tech.repository.UserRepositoryImpl
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.tokenmanager.TokenManager
import ru.rutoken.tech.ui.bank.choosecertificate.ChooseNewCertificateViewModel
import ru.rutoken.tech.ui.bank.payment.PaymentViewModel
import ru.rutoken.tech.ui.bank.payments.PaymentsViewModel
import ru.rutoken.tech.ui.bank.startscreen.BankStartScreenViewModel
import ru.rutoken.tech.ui.ca.generateobjects.certificate.GenerateCertificateViewModel
import ru.rutoken.tech.ui.ca.generateobjects.keypair.GenerateKeyPairViewModel
import ru.rutoken.tech.ui.ca.tokeninfo.CaTokenInfoViewModel
import ru.rutoken.tech.ui.tokenauth.EnterPinViewModel
import ru.rutoken.tech.ui.tokenauth.LoginViewModel

val koinModule = module {
    single { RtPkcs11Module() } bind Pkcs11Module::class
    single { Pkcs11Launcher(get()) }
    single {
        Room.databaseBuilder(androidContext(), Database::class.java, "rutoken_tech_database").build()
    }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single { TokenManager() }
    single { AppSessionHolder() }

    viewModel { LoginViewModel(androidContext(), get(), get(), get()) }
    viewModel { CaTokenInfoViewModel(androidContext(), get()) }
    viewModel { GenerateKeyPairViewModel(get(), get()) }
    viewModel { EnterPinViewModel(get(), get(), get()) }
    viewModel { GenerateCertificateViewModel(get(), get()) }
    viewModel { BankStartScreenViewModel(get(), get(), get()) }
    viewModel { ChooseNewCertificateViewModel(get(), get(), get()) }
    viewModel { PaymentsViewModel(get()) }
    viewModel { PaymentViewModel(get(), get()) }
}
