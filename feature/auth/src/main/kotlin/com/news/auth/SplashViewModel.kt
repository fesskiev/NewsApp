package com.news.auth

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.news.common.mvi.MviViewModel
import org.news.data.AuthRepository
import org.news.network.token.TokenProvider
import org.news.security.biometric.BiometricManager
import org.news.security.keys.GCM_IV_LENGTH
import org.news.security.keys.TokenEncryptionKeyManager

internal data class SplashState(
    val isLoading: Boolean = false
)

internal sealed interface SplashAction {

}

internal sealed interface SplashEvent {

}

internal class SplashViewModel(
    private val keyManager: TokenEncryptionKeyManager,
    private val biometricManager: BiometricManager,
    private val authRepository: AuthRepository,
    private val tokenProvider: TokenProvider
) : MviViewModel<SplashState, SplashAction, SplashEvent>(
    initialState = SplashState()
) {

    init {
        checkLocalAuthState()
    }

    override fun onAction(action: SplashAction) {
        when (action) {
            else -> Unit
        }
    }

    private fun checkLocalAuthState() {
        viewModelScope.launch {
            try {
                val alias = getAlias()
                if (!keyManager.generateAESKeyIfNeed(alias)) {
                    navigateToLogin()
                    return@launch
                }

                val encryptedAccessToken = getEncryptedAccessTokenFromStorage()
                val encryptedRefreshToken = getEncryptedRefreshTokenFromStorage()

                val iv = encryptedAccessToken.copyOfRange(0, GCM_IV_LENGTH)
                val cipher = keyManager.getCipherForDecryption(alias, iv)
                if (cipher == null) {
                    navigateToLogin()
                    return@launch
                } else {
                    // show biometric prompt
                }

                val accessToken = keyManager.decryptToken(cipher, encryptedAccessToken)
                val refreshToken = keyManager.decryptToken(cipher, encryptedRefreshToken)

                if (accessToken == null || refreshToken == null) {
                    navigateToLogin()
                    return@launch
                }

                tokenProvider.setTokens(accessToken, refreshToken)

                if (!tokenProvider.isAccessTokenExpired()) {
                    navigateToHome()
                    return@launch
                }

                if (tokenProvider.isRefreshTokenExpired()) {
                    navigateToLogin()
                    return@launch
                }

                val isRefreshSuccess = tokenProvider.refreshToken()
                if (isRefreshSuccess) {
                    navigateToHome()
                }
            } catch (e: Exception) {
                navigateToLogin()
            }
        }
    }

    private fun getEncryptedRefreshTokenFromStorage(): ByteArray {
        TODO("Provide the return value")
    }

    private fun getEncryptedAccessTokenFromStorage(): ByteArray {
        TODO("Provide the return value")
    }

    private fun getAlias(): String {
        TODO("Provide the return value")
    }

    private fun navigateToHome() {

    }

    private fun navigateToLogin() {

    }
}
