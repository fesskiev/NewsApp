package com.news.auth

import kotlinx.coroutines.flow.update
import org.news.common.mvi.MviViewModel
import org.news.security.keys.KeyManager
import org.news.security.biometric.BiometricManager
import org.news.security.keys.verifySignature
import java.security.Signature

private const val KEY_ALIAS = "BiometricAuth"

internal data class AuthState(
    val isLoading: Boolean = false,
    val biometricState: BiometricState = BiometricState.Disable,
    val email: String = "",
)

internal sealed interface BiometricState {
    data class Enable(val signature: Signature) : BiometricState
    data object Disable : BiometricState
}

internal sealed interface AuthAction {
    data class EmailChange(val email: String) : AuthAction
    data class BiometricAuthenticated(val signature: Signature) : AuthAction
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
) : MviViewModel<AuthState, AuthAction, AuthEvent>(
    initialState = AuthState()
) {

    init {
        checkBiometricEnable()
    }

    override fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.EmailChange -> uiState.update { it.copy(email = action.email) }
            is AuthAction.BiometricAuthenticated -> biometricAuthenticated(action.signature)
            is AuthAction.BiometricAuthenticatorError -> emitUiEvent(AuthEvent.BiometricAuthenticatorError(action.error))
            AuthAction.CheckBiometricEnable -> checkBiometricEnable()
        }
    }

    private fun checkBiometricEnable() {
        if (biometricManager.isBiometricEnable()) {
            if (!keyManager.generateKeyPair(KEY_ALIAS)) {
                return
            }
            val signature = keyManager.getSignatureForAuthentication(KEY_ALIAS)
            if (signature == null) {
                return
            }
            uiState.update { it.copy(biometricState = BiometricState.Enable(signature)) }
        } else {
            uiState.update { it.copy(biometricState = BiometricState.Disable) }
            emitUiEvent(AuthEvent.BiometricDisable)
        }
    }

    private fun biometricAuthenticated(signature: Signature) {
        val publicKey = keyManager.getPublicKey(KEY_ALIAS)
        if (publicKey == null) {
            return
        }

        val dataToSign = "user123:${System.currentTimeMillis()}".toByteArray()

        val signedData = keyManager.signData(signature, dataToSign)
        if (signedData == null) {
            return
        }

        val verify = verifySignature(publicKey, dataToSign, signedData)
        println("VERIFY: $verify")
    }

}