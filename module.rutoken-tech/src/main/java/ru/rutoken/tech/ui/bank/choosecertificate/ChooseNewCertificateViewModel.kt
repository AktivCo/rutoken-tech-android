/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank.choosecertificate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.rutoken.tech.database.user.UserEntity
import ru.rutoken.tech.repository.user.UserRepository
import ru.rutoken.tech.repository.user.makeUser
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.requireBankUserAddingSession

class ChooseNewCertificateViewModel(
    private val sessionHolder: AppSessionHolder,
    private val repository: UserRepository
) : ViewModel() {
    fun addUser(certificateIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val bankUserAddingSession = sessionHolder.requireBankUserAddingSession()
            val selectedCertificate = bankUserAddingSession.certificates[certificateIndex]
            val user = makeUser(
                UserEntity(
                    certificateDerValue = selectedCertificate.bytes,
                    ckaId = selectedCertificate.ckaId,
                    tokenSerialNumber = bankUserAddingSession.tokenSerial
                )
            )
            repository.addUser(user)
        }
    }
}