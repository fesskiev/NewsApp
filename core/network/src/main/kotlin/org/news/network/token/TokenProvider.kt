package org.news.network.token

import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.news.navigation.GlobalNavigationEventBus
import org.news.navigation.Auth
import org.news.network.model.JwtPayload
import org.news.network.service.AuthApiService
import kotlin.time.Instant
import kotlin.time.Clock
import kotlin.io.encoding.Base64

interface TokenProvider {
    val bearerTokens: BearerTokens?
    suspend fun refreshToken() : Boolean
    fun setTokens(accessToken: String, refreshToken: String)
    fun isAccessTokenExpired(): Boolean
    fun isRefreshTokenExpired(): Boolean
}

internal class TokenProviderImpl(
    private val authApiService: AuthApiService,
    private val globalNavigationEventBus: GlobalNavigationEventBus,
    override var bearerTokens: BearerTokens? = null
) : TokenProvider {

    private val mutex = Mutex()
    private val maxRetries = 3

    override suspend fun refreshToken(): Boolean = mutex.withLock {
        try {
            val refreshToken = bearerTokens?.refreshToken ?: throw IllegalStateException("Refresh must not be null")
            var lastException: Exception? = null
            for (attempt in 1..maxRetries) {
                try {
                    val newTokenResponse = authApiService.refreshToken(refreshToken)
                    setTokens(
                        accessToken = newTokenResponse.accessToken,
                        refreshToken = newTokenResponse.refreshToken
                    )
                    return true
                } catch (e: Exception) {
                    lastException = e
                    if (attempt < maxRetries) {
                        delay(1000L * attempt)
                    }
                }
            }
            lastException?.let { throw it }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            bearerTokens = null
            globalNavigationEventBus.navigateTo(Auth)
            false
        }
    }

    override fun isAccessTokenExpired(): Boolean {
        val token = bearerTokens?.accessToken ?: return true
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return true

            val jwtPayload = getJWTPayload(parts)
            val exp = jwtPayload.exp ?: return true
            val expiryInstant = Instant.fromEpochSeconds(exp, 0)
            Clock.System.now() > expiryInstant
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }
    }

    override fun isRefreshTokenExpired(): Boolean {
        val token = bearerTokens?.refreshToken ?: return true
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return true

            val jwtPayload = getJWTPayload(parts)
            val exp = jwtPayload.exp ?: return true
            val expiryInstant = Instant.fromEpochSeconds(exp, 0)
            Clock.System.now() > expiryInstant
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }
    }

    override fun setTokens(accessToken: String, refreshToken: String) {
        bearerTokens = BearerTokens(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    private fun getJWTPayload(parts: List<String>): JwtPayload {
        val payload = parts[1]
        val normalizedPayload = payload.padEnd((payload.length + 3) and -4, '=')
        val decodedBytes = Base64.decode(normalizedPayload)
        val json = String(decodedBytes)
        val jwtPayload = Json.decodeFromString<JwtPayload>(json)
        return jwtPayload
    }
}