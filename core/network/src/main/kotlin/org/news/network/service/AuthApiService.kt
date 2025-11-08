package org.news.network.service

import io.ktor.client.HttpClient
import kotlinx.coroutines.delay
import org.news.network.BuildConfig
import org.news.network.model.TokenApiResponse

interface AuthApiService {

    suspend fun login(username: String, password: String): TokenApiResponse

    suspend fun refreshToken(refreshToken: String) : TokenApiResponse
}

internal class AuthApiServiceImpl(
    private val httpClient: HttpClient
): AuthApiService {

    override suspend fun login(
        username: String,
        password: String
    ): TokenApiResponse {
        delay(500)
        return TokenApiResponse(
            accessToken = BuildConfig.APP_KEY,
            refreshToken = BuildConfig.APP_KEY
        )
    }

    override suspend fun refreshToken(refreshToken: String): TokenApiResponse {
        delay(500)
        return TokenApiResponse(
            accessToken = BuildConfig.APP_KEY,
            refreshToken = BuildConfig.APP_KEY
        )
    }
}