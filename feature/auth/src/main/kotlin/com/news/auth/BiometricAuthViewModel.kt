package com.news.auth

import android.util.Base64
import androidx.lifecycle.viewModelScope
import com.news.auth.AuthEvent.*
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.news.data.AuthRepository
import org.news.common.mvi.MviViewModel
import org.news.security.keys.KeyManager
import org.news.security.biometric.BiometricManager
import org.news.security.biometric.BiometricStatus
import org.news.security.keys.verifySignature
import java.security.Signature

private const val KEY_ALIAS = "BiometricAuth"

internal data class AuthState(
    val isLoading: Boolean = false,
    val biometricState: BiometricState = BiometricState.Disable,
    val email: String = "",
    val password: String = ""
) {
    val isEmailTextFiledEnable: Boolean
        get() = !isLoading

    val isPasswordTextFiledEnable: Boolean
        get() = !isLoading

    val isLoginButtonEnable: Boolean
        get() = !isLoading && email.isNotEmpty() && password.isNotEmpty()
}

internal sealed interface BiometricState {
    data class Enable(val signature: Signature) : BiometricState
    data object Disable : BiometricState
    data object Unavailable : BiometricState
}

internal sealed interface AuthAction {
    data class EmailChange(val email: String) : AuthAction
    data class BiometricAuthenticate(val signature: Signature)  : AuthAction
    data class BiometricAuthenticated(val signature: Signature) : AuthAction
    data object EnableBiometricClick : AuthAction
    data object CheckBiometricEnable : AuthAction
    data class BiometricAuthenticatorError(val error: String) : AuthAction
}

internal sealed interface AuthEvent {
    data object BiometricDisable : AuthEvent
    data class BiometricAuthenticatorError(val error: String) : AuthEvent
}

internal class BiometricAuthViewModel(
    private val keyManager: KeyManager,
    private val biometricManager: BiometricManager,
    private val authRepository: AuthRepository
) : MviViewModel<AuthState, AuthAction, AuthEvent>(
    initialState = AuthState()
) {

    init {
        checkBiometricEnable("userId")
    }

    override fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.EmailChange -> uiState.update { it.copy(email = action.email) }
            is AuthAction.BiometricAuthenticated -> biometricAuthenticated("userId", action.signature)
            is AuthAction.BiometricAuthenticatorError -> emitUiEvent(BiometricAuthenticatorError(action.error))
            AuthAction.CheckBiometricEnable -> checkBiometricEnable("userId")
            else -> {}
        }
    }

    private fun checkBiometricEnable(userId:  String) {
        val alias = KEY_ALIAS + userId
        when(biometricManager.getBiometricStatus()) {
            BiometricStatus.ENABLE -> {
                if (!keyManager.generateKeyPair(alias)) {
                    uiState.update { it.copy(biometricState = BiometricState.Unavailable) }
                    return
                }
                val signature = keyManager.getSignatureForAuthentication(alias)
                if (signature == null) {
                    uiState.update { it.copy(biometricState = BiometricState.Disable) }
                    return
                }
                uiState.update { it.copy(biometricState = BiometricState.Enable(signature)) }
            }
            BiometricStatus.DISABLE -> {
                uiState.update { it.copy(biometricState = BiometricState.Disable) }
                emitUiEvent(BiometricDisable)
            }
            BiometricStatus.UNAVAILABLE -> {
                uiState.update { it.copy(biometricState = BiometricState.Unavailable) }
            }
        }
    }

    private fun biometricAuthenticated(
        userId:  String,
        signature: Signature
    ) {

        /**
         *  1. Registration via login and password
         *  2. ask login via biometric dialog, is yes, after biometric auth success - register biometric (send user id, public key)
         *  3. 2 login options - via login, password or biometric (send data, signature)
         */

        viewModelScope.launch {
            val alias = KEY_ALIAS + userId

            // registration
            val publicKey = keyManager.getPublicKey(alias)
            if (publicKey == null) {
                return@launch
            }
            authRepository.registerBiometric(
                userId = userId,
                publicKey = Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
            )

            // auth
            val dataStr = "$userId:${System.currentTimeMillis()}"
            val data = dataStr.toByteArray(Charsets.UTF_8)

            val signedData = keyManager.signData(signature, data)
            if (signedData == null) {
                return@launch
            }
            authRepository.loginBiometric(
                userId = userId,
                data = dataStr,
                signature = Base64.encodeToString(signedData, Base64.NO_WRAP)
            )

            // test
            val verify = verifySignature(publicKey, data, signedData)
            println("VERIFY: $verify")

        }

    }



}