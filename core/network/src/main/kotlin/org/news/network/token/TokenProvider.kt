package org.news.network.token

import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.news.navigation.GlobalNavigationEventBus
import org.news.navigation.Unauthenticated
import org.news.network.BuildConfig
import org.news.network.service.AuthApiService

internal interface TokenProvider {
    val bearerTokens: BearerTokens?
    suspend fun refreshToken()
}

internal class TokenProviderImpl(
    private val authApiService: AuthApiService,
    private val globalNavigationEventBus: GlobalNavigationEventBus,
    override var bearerTokens: BearerTokens? = BearerTokens(
        accessToken = BuildConfig.APP_KEY,
        refreshToken = null
    )
) : TokenProvider {

    private val mutex = Mutex()

    override suspend fun refreshToken() = mutex.withLock {
        try {
            val refreshToken = bearerTokens?.refreshToken ?: throw IllegalStateException("Refresh must not be null")
            val newTokenResponse = authApiService.refreshToken(refreshToken)
            bearerTokens = BearerTokens(
                accessToken = newTokenResponse.accessToken,
                refreshToken = newTokenResponse.refreshToken
            )
        } catch (e: Exception) {
            bearerTokens = null
            globalNavigationEventBus.navigateTo(Unauthenticated)
        }
    }
}