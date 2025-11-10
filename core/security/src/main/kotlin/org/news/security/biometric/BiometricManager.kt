package org.news.security.biometric

import android.content.Context

interface BiometricManager {

    fun isBiometricEnable(): Boolean
}

class BiometricManagerImpl(
    private val context: Context
) : BiometricManager {

    override fun isBiometricEnable(): Boolean {
        val biometricManager = androidx.biometric.BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

}