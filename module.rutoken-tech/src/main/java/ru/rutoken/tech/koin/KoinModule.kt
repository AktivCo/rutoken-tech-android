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
import ru.rutoken.tech.pkcs11.TokenContextStorage
import ru.rutoken.tech.repository.document.DocumentRepository
import ru.rutoken.tech.repository.document.DocumentRepositoryImpl
import ru.rutoken.tech.repository.user.UserRepository
import ru.rutoken.tech.repository.user.UserRepositoryImpl
import ru.rutoken.tech.tokenmanager.TokenManager
import ru.rutoken.tech.ui.ca.generateobjects.keypair.GenerateKeyPairViewModel
import ru.rutoken.tech.ui.tokenauth.EnterPinViewModel

val koinModule = module {
    single { RtPkcs11Module() } bind Pkcs11Module::class
    single { Pkcs11Launcher(get()) }
    single {
        Room.databaseBuilder(androidContext(), Database::class.java, "rutoken_tech_database").build()
    }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<DocumentRepository> { DocumentRepositoryImpl(get()) }
    single { TokenManager() }
    single { TokenContextStorage() }

    viewModel { GenerateKeyPairViewModel(get(), get()) }
    viewModel { EnterPinViewModel() }
}
