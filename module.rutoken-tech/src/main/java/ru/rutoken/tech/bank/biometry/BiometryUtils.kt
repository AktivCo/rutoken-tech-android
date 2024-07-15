/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.bank.biometry

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.*
import androidx.annotation.WorkerThread
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.rutoken.tech.R
import ru.rutoken.tech.utils.loge
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

private const val SECRET_KEY_ALIAS = "rutoken tech pin encryption key"

/**
 * Logging purposes
 */
private class BiometryUtils

fun Context.canUseBiometry(): Boolean {
    // We do not allow to use biometry on Android 8.1 and lower - this requires the app to use Theme.appCompat,
    // and we use Material themes
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false

    val biometricManager = BiometricManager.from(this)
    return biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS
}

@WorkerThread
suspend fun encryptWithBiometricPrompt(
    activity: FragmentActivity,
    data: ByteArray,
    onError: (Int?, String?) -> Unit,
    onSuccess: (ByteArray, ByteArray) -> Unit
) = performCipherCallWithBiometricPrompt(activity, Cipher.ENCRYPT_MODE, data, null, onError, onSuccess)

@WorkerThread
suspend fun decryptWithBiometricPrompt(
    activity: FragmentActivity,
    data: ByteArray,
    cipherIv: ByteArray,
    onError: (Int?, String?) -> Unit,
    onSuccess: (ByteArray) -> Unit
) = performCipherCallWithBiometricPrompt(
    activity,
    Cipher.DECRYPT_MODE,
    data,
    cipherIv,
    onError,
) { decryptedData, _ -> onSuccess(decryptedData) }

private suspend fun performCipherCallWithBiometricPrompt(
    activity: FragmentActivity,
    cipherMode: Int,
    data: ByteArray,
    cipherIv: ByteArray?,
    onError: (Int?, String?) -> Unit,
    onSuccess: (ByteArray, ByteArray) -> Unit
) {
    val secretKey = getSecretKey() ?: generateSecretKey()
    val cipher = getCipher().also {
        if (cipherIv != null)
            it.init(cipherMode, secretKey, IvParameterSpec(cipherIv))
        else
            it.init(cipherMode, secretKey)
    }
    val authenticationCallback = makeAuthenticationCallback(data, onError, onSuccess)

    withContext(Dispatchers.Main) {
        BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), authenticationCallback)
            .authenticate(activity.getBiometricPromptInfo(), BiometricPrompt.CryptoObject(cipher))
    }
}

private fun getCipher() = Cipher.getInstance("$KEY_ALGORITHM_AES/$BLOCK_MODE_CBC/$ENCRYPTION_PADDING_PKCS7")

private fun getSecretKey() = with(KeyStore.getInstance("AndroidKeyStore")) {
    load(null)
    getKey(SECRET_KEY_ALIAS, null) as? SecretKey
}

private fun generateSecretKey(): SecretKey {
    val keyGenParameterSpec = KeyGenParameterSpec.Builder(SECRET_KEY_ALIAS, PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
        .setBlockModes(BLOCK_MODE_CBC)
        .setEncryptionPaddings(ENCRYPTION_PADDING_PKCS7)
        .setUserAuthenticationRequired(true)
        .setInvalidatedByBiometricEnrollment(false)
        .build()

    return with(KeyGenerator.getInstance(KEY_ALGORITHM_AES, "AndroidKeyStore")) {
        init(keyGenParameterSpec)
        generateKey()
    }
}

private fun Context.getBiometricPromptInfo() = BiometricPrompt.PromptInfo.Builder()
    .setTitle(getString(R.string.use_biometry_title))
    .setSubtitle(getString(R.string.use_biometry_subtitle))
    .setNegativeButtonText(getString(R.string.cancel))
    .build()

private fun makeAuthenticationCallback(
    dataToCrypt: ByteArray,
    onError: (Int?, String?) -> Unit,
    onSuccess: (ByteArray, ByteArray) -> Unit
) = object : BiometricPrompt.AuthenticationCallback() {
    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        try {
            val cipher = result.cryptoObject!!.cipher!!
            onSuccess(cipher.doFinal(dataToCrypt), cipher.iv)
        } catch (exception: Exception) {
            loge<BiometryUtils>(exception) { "Biometric authentication failed" }
            onError(null, exception.toString())
        }
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        loge<BiometryUtils> { "Biometric authentication failed with code $errorCode: $errString" }
        onError(errorCode, errString.toString())
    }
}