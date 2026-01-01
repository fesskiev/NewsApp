package org.news.security.biometric

import android.content.Context

enum class BiometricStatus {
    ENABLE, DISABLE, UNAVAILABLE
}

interface BiometricManager {

    fun getBiometricStatus(): BiometricStatus
}

class BiometricManagerImpl(
    private val context: Context
) : BiometricManager {

    override fun getBiometricStatus(): BiometricStatus {
        val biometricManager = androidx.biometric.BiometricManager.from(context)
        val result = biometricManager.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)
        return result.toBiometricState()
    }

    private fun Int.toBiometricState(): BiometricStatus {
        return when (this) {
            androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.ENABLE
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.DISABLE
            androidx.biometric.BiometricManager.BIOMETRIC_STATUS_UNKNOWN,
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED,
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricStatus.UNAVAILABLE
            else -> BiometricStatus.UNAVAILABLE
        }
    }

}