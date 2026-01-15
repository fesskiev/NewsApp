package com.news.auth

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import com.news.auth.SplashEvent.*
import org.news.common.mvi.MviViewModel
import org.news.model.StoredAuthData
import org.news.model.TokenExpiryStatus
import org.news.model.getTokenExpiryStatus
import org.news.navigation.Auth
import org.news.navigation.GlobalNavigationEventBus
import org.news.navigation.Home
import org.news.network.token.TokenProvider
import org.news.security.biometric.BiometricCapability
import org.news.security.biometric.BiometricManager
import org.news.security.keys.GCM_IV_LENGTH
import org.news.security.keys.TokenEncryptionKeyManager
import org.news.storage.AuthDataStorage

private const val KEY_ALIAS = "TokenKey"

internal data class SplashState(
    val isLoading: Boolean = false
)

internal sealed interface SplashAction {
    data class BiometricAuthenticated(val authData: StoredAuthData) : SplashAction
    data class BiometricAuthenticatorError(val error: String) : SplashAction
}

internal sealed interface SplashEvent {
    data class LaunchBiometricAuthenticator(val authData: StoredAuthData) : SplashEvent
}

internal class SplashViewModel(
    private val keyManager: TokenEncryptionKeyManager,
    private val biometricManager: BiometricManager,
    private val authDataStorage: AuthDataStorage,
    private val tokenProvider: TokenProvider,
    private val globalNavigationEventBus: GlobalNavigationEventBus
) : MviViewModel<SplashState, SplashAction, SplashEvent>(
    initialState = SplashState()
) {

    init {
        checkLocalAuthState()
    }

    override fun onAction(action: SplashAction) {
        when (action) {
            is SplashAction.BiometricAuthenticated -> decryptTokens(action.authData)
            is SplashAction.BiometricAuthenticatorError -> TODO()
        }
    }

    private fun checkLocalAuthState() {
        viewModelScope.launch {
            if (biometricManager.checkBiometricCapability() != BiometricCapability.AVAILABLE) {
                navigateToAuth()
                return@launch
            }
            val alias = getAlias()
            if (!keyManager.generateAESKeyIfNeed(alias)) {
                navigateToAuth()
                return@launch
            }

            val authData = authDataStorage.get()
                .firstOrNull()
                ?: run {
                    navigateToAuth()
                    return@launch
                }

            if (authData.getTokenExpiryStatus() == TokenExpiryStatus.BOTH_EXPIRED) {
                navigateToAuth()
                return@launch
            }

            emitUiEvent(LaunchBiometricAuthenticator(authData))
        }
    }

    private fun decryptTokens(authData: StoredAuthData) {
        viewModelScope.launch {
            val alias = getAlias()
            val encryptedAccessToken = authData.encryptedAccessToken
            val iv = encryptedAccessToken.copyOfRange(0, GCM_IV_LENGTH)
            val cipher = keyManager.getCipherForDecryption(alias, iv)
            if (cipher == null) {
                navigateToAuth()
                return@launch
            }
            val encryptedRefreshToken = authData.encryptedRefreshToken
            val accessToken =
                keyManager.decryptToken(cipher, encryptedAccessToken)
            val refreshToken =
                keyManager.decryptToken(cipher, encryptedRefreshToken)
            if (accessToken == null || refreshToken == null) {
                navigateToAuth()
                return@launch
            }
            tokenProvider.setTokens(accessToken, refreshToken)
            when (authData.getTokenExpiryStatus()) {
                TokenExpiryStatus.ACCESS_VALID -> navigateToHome()
                TokenExpiryStatus.REFRESH_VALID -> {
                    val isRefreshSuccess = tokenProvider.refreshToken()
                    if (isRefreshSuccess) {
                        navigateToHome()
                    }
                }

                TokenExpiryStatus.BOTH_EXPIRED -> navigateToAuth()
            }
        }
    }

    private fun getAlias(): String {
        return KEY_ALIAS + "test@gmail.com"
    }

    private suspend fun navigateToHome() {
        globalNavigationEventBus.navigateTo(Home)
    }

    private suspend fun navigateToAuth() {
        globalNavigationEventBus.navigateTo(Auth)
    }
}
