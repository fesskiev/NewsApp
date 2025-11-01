package org.news.network.token

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.news.navigation.GlobalNavigationEventBus
import org.news.navigation.Unauthenticated
import org.news.network.BuildConfig
import org.news.network.NewsApiService

internal interface TokenProvider {
    val accessToken: String
    val refreshToken: String

    suspend fun refreshToken(): String?
}

internal class TokenProviderImpl(
    private val newsApiService: NewsApiService,
    private val globalNavigationEventBus: GlobalNavigationEventBus,
    override var accessToken: String = BuildConfig.APP_KEY,
    override val refreshToken: String = BuildConfig.APP_KEY
) : TokenProvider {

    private val mutex = Mutex()

    override suspend fun refreshToken(): String? = mutex.withLock {
        try {
            val newTokenResponse = newsApiService.refreshToken(refreshToken)
            accessToken = newTokenResponse.accessToken
            accessToken
        } catch (e: Exception) {
            globalNavigationEventBus.navigateTo(Unauthenticated)
            null
        }
    }
}