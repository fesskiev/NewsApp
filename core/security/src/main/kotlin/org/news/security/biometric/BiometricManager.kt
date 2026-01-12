package org.news.security.biometric

import android.content.Context

enum class BiometricCapability {
    AVAILABLE,           // Hardware + enrolled biometrics
    NO_HARDWARE,         // Device doesn't support biometrics
    NO_ENROLLED,         // Hardware exists but no biometrics enrolled
    TEMPORARILY_UNAVAILABLE  // Locked out, etc.
}

interface BiometricManager {

    fun checkBiometricCapability(): BiometricCapability
}

class BiometricManagerImpl(
    private val context: Context
) : BiometricManager {

    override fun checkBiometricCapability(): BiometricCapability {
        val biometricManager = androidx.biometric.BiometricManager.from(context)
        val result = biometricManager.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)
        return result.toBiometricState()
    }

    private fun Int.toBiometricState(): BiometricCapability {
        return when (this) {
            androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS ->
                BiometricCapability.AVAILABLE

            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                BiometricCapability.NO_HARDWARE

            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                BiometricCapability.NO_ENROLLED

            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED,
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
                BiometricCapability.TEMPORARILY_UNAVAILABLE

            else -> BiometricCapability.TEMPORARILY_UNAVAILABLE
        }
    }

}