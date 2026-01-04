package com.news.auth

import android.util.Base64
import androidx.lifecycle.viewModelScope
import com.news.auth.AuthEvent.*
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.news.data.AuthRepository
import org.news.common.mvi.MviViewModel
import org.news.common.utils.Result
import org.news.security.keys.KeyManager
import org.news.security.biometric.BiometricManager
import org.news.security.biometric.BiometricStatus
import org.news.security.keys.verifySignature
import java.security.Signature

private const val KEY_ALIAS = "BiometricAuth"

internal data class AuthState(
    val isLoading: Boolean = false,
    val biometricState: BiometricState = BiometricState.Disable,
    val email: String = "test@gmail.com",
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
    data class PasswordChange(val password: String) : AuthAction
    data class BiometricAuthenticateClick(val signature: Signature) : AuthAction
    data class BiometricAuthenticated(val signature: Signature) : AuthAction
    data object EnableBiometricClick : AuthAction
    data object CheckBiometricEnable : AuthAction
    data class BiometricAuthenticatorError(val error: String) : AuthAction
}

internal sealed interface AuthEvent {
    sealed interface SnackbarEvent : AuthEvent {
        data object BiometricDisable : SnackbarEvent
        data class BiometricAuthenticatorError(val error: String) : SnackbarEvent
    }

    data class LaunchBiometricAuthenticator(val signature: Signature) : AuthEvent
    data object EnrollBiometric : AuthEvent
}

internal class AuthViewModel(
    private val keyManager: KeyManager,
    private val biometricManager: BiometricManager,
    private val authRepository: AuthRepository
) : MviViewModel<AuthState, AuthAction, AuthEvent>(
    initialState = AuthState()
) {

    init {
        checkBiometricEnable()
    }

    override fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.EmailChange -> uiState.update { it.copy(email = action.email) }
            is AuthAction.PasswordChange -> uiState.update { it.copy(password = action.password) }
            is AuthAction.BiometricAuthenticated -> biometricAuthenticated(action.signature)
            is AuthAction.BiometricAuthenticatorError -> emitUiEvent(SnackbarEvent.BiometricAuthenticatorError(action.error))
            AuthAction.CheckBiometricEnable -> checkBiometricEnable()
            is AuthAction.BiometricAuthenticateClick -> emitUiEvent(LaunchBiometricAuthenticator(action.signature))
            AuthAction.EnableBiometricClick -> emitUiEvent(EnrollBiometric)
        }
    }

    private fun checkBiometricEnable() {
        with(uiState) {
            val email = value.email
            val alias = KEY_ALIAS + email
            when (biometricManager.getBiometricStatus()) {
                BiometricStatus.ENABLE -> {
                    if (!keyManager.generateKeyPair(alias)) {
                        update { it.copy(biometricState = BiometricState.Unavailable) }
                        return
                    }
                    val signature = keyManager.getSignatureForAuthentication(alias)
                    if (signature == null) {
                        update { it.copy(biometricState = BiometricState.Disable) }
                        return
                    }
                    update { it.copy(biometricState = BiometricState.Enable(signature)) }
                }

                BiometricStatus.DISABLE -> {
                    update { it.copy(biometricState = BiometricState.Disable) }
                    emitUiEvent(SnackbarEvent.BiometricDisable)
                }

                BiometricStatus.UNAVAILABLE -> {
                    update { it.copy(biometricState = BiometricState.Unavailable) }
                }
            }
        }
    }

    private fun biometricAuthenticated(signature: Signature) {

        /**
         *  1. Registration via login and password
         *  2. ask login via biometric dialog, is yes, after biometric auth success - register biometric (send user id, public key)
         *  3. 2 login options - via login, password or biometric (send data, signature)
         */

        viewModelScope.launch {
            val email = uiState.value.email
            val alias = KEY_ALIAS + email

            // registration
            val publicKey = keyManager.getPublicKey(alias)
            if (publicKey == null) {
                return@launch
            }

            authRepository.registerBiometric(
                userId = email,
                publicKey = Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
            ).fold(
                onSuccess = { response ->

                },
                onFailure = { error ->

                }
            )


            // auth
            val dataStr = "$email:${System.currentTimeMillis()}"
            val data = dataStr.toByteArray(Charsets.UTF_8)

            val signedData = keyManager.signData(signature, data)
            if (signedData == null) {
                return@launch
            }

            authRepository.loginBiometric(
                userId = email,
                data = dataStr,
                signature = Base64.encodeToString(signedData, Base64.NO_WRAP)
            ).fold(
                onSuccess = { response ->

                },
                onFailure = { error ->

                }
            )


            // test
            val verify = verifySignature(publicKey, data, signedData)
            println("VERIFY: $verify")

        }

    }


}