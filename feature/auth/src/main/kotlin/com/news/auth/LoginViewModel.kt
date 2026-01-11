package com.news.auth

import android.util.Base64
import androidx.lifecycle.viewModelScope
import com.news.auth.LoginEvent.*
import com.news.auth.LoginEvent.SnackbarEvent.*
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.news.data.AuthRepository
import org.news.common.mvi.MviViewModel
import org.news.security.keys.BiometricAuthenticationKeyManager
import org.news.security.biometric.BiometricManager
import org.news.security.biometric.BiometricStatus
import org.news.security.keys.verifySignature
import java.security.Signature

private const val KEY_ALIAS = "BiometricAuth"

internal data class LoginState(
    val isLoading: Boolean = false,
    val biometricState: BiometricState = BiometricState.Disable,
    val email: String = "test@gmail.com",
    val password: String = "qwerty"
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

internal sealed interface LoginAction {
    data class EmailChange(val email: String) : LoginAction
    data class PasswordChange(val password: String) : LoginAction
    data class BiometricAuthenticateClick(val signature: Signature) : LoginAction
    data class BiometricAuthenticated(val signature: Signature) : LoginAction
    data object EnableBiometricClick : LoginAction
    data object LoginClick : LoginAction
    data object CheckBiometricEnable : LoginAction
    data class BiometricAuthenticatorError(val error: String) : LoginAction
}

internal sealed interface LoginEvent {
    sealed interface SnackbarEvent : LoginEvent {
        data object BiometricDisable : SnackbarEvent
        data class BiometricAuthenticatorError(val error: String) : SnackbarEvent
        data object GenerateKeyPairError : SnackbarEvent
    }

    data class LaunchBiometricAuthenticator(val signature: Signature) : LoginEvent
    data object EnrollBiometric : LoginEvent
}

internal class LoginViewModel(
    private val keyManager: BiometricAuthenticationKeyManager,
    private val biometricManager: BiometricManager,
    private val authRepository: AuthRepository
) : MviViewModel<LoginState, LoginAction, LoginEvent>(
    initialState = LoginState()
) {

    init {
        checkBiometricEnable()
    }

    override fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.EmailChange -> uiState.update { it.copy(email = action.email) }
            is LoginAction.PasswordChange -> uiState.update { it.copy(password = action.password) }
            is LoginAction.BiometricAuthenticated -> biometricAuthenticated(action.signature)
            is LoginAction.BiometricAuthenticatorError -> emitUiEvent(
                BiometricAuthenticatorError(
                    action.error
                )
            )

            LoginAction.CheckBiometricEnable -> checkBiometricEnable()
            is LoginAction.BiometricAuthenticateClick -> emitUiEvent(
                LaunchBiometricAuthenticator(
                    action.signature
                )
            )

            LoginAction.EnableBiometricClick -> emitUiEvent(EnrollBiometric)
            LoginAction.LoginClick -> TODO()
        }
    }

    private fun checkBiometricEnable() {
        with(uiState) {
            val email = value.email
            val alias = KEY_ALIAS + email
            when (biometricManager.getBiometricStatus()) {
                BiometricStatus.ENABLE -> {
                    if (!keyManager.generateKeyPairIfNeed(alias)) {
                        update { it.copy(biometricState = BiometricState.Unavailable) }
                        emitUiEvent(GenerateKeyPairError)
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
                    emitUiEvent(BiometricDisable)
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


            // login
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