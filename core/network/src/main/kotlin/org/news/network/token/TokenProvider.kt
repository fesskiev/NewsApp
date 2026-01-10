package org.news.network.token

import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.news.navigation.GlobalNavigationEventBus
import org.news.navigation.Unauthenticated
import org.news.network.BuildConfig
import org.news.network.model.JwtPayload
import org.news.network.service.AuthApiService
import kotlin.time.Instant
import kotlin.time.Clock
import kotlin.io.encoding.Base64

internal interface TokenProvider {
    val bearerTokens: BearerTokens?
    suspend fun refreshToken()
    fun isAccessTokenExpired(): Boolean
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
            val refreshToken = bearerTokens?.refreshToken
                ?: throw IllegalStateException("Refresh must not be null")
            val newTokenResponse = authApiService.refreshToken(refreshToken)
            bearerTokens = BearerTokens(
                accessToken = newTokenResponse.accessToken,
                refreshToken = newTokenResponse.refreshToken
            )
        } catch (e: Exception) {
            e.printStackTrace()
            bearerTokens = null
            globalNavigationEventBus.navigateTo(Unauthenticated)
        }
    }

    override fun isAccessTokenExpired(): Boolean {
        val token = bearerTokens?.accessToken ?: return true
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return true

            val payload = parts[1]
            val normalizedPayload = payload.padEnd((payload.length + 3) and -4, '=')
            val decodedBytes = Base64.decode(normalizedPayload)
            val json = String(decodedBytes)

            val jwtPayload = Json.decodeFromString<JwtPayload>(json)
            val exp = jwtPayload.exp ?: return true
            val expiryInstant = Instant.fromEpochSeconds(exp, 0)
            val now = Clock.System.now()
            now > expiryInstant
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }
    }
}