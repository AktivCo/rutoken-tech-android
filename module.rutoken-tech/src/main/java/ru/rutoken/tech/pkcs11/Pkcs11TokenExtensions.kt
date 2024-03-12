package ru.rutoken.tech.pkcs11

import androidx.annotation.WorkerThread
import ru.rutoken.pkcs11wrapper.main.Pkcs11Token

@WorkerThread
fun Pkcs11Token.getSerialNumber(): SerialHexString = tokenInfo.serialNumber.trimEnd()
