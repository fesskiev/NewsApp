package org.news.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.URLProtocol
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.news.network.model.ApiError
import org.news.network.token.TokenProvider

internal fun buildKtorClient(
    tokenProvider: Lazy<TokenProvider>
) = HttpClient(Android) {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            }
        )
    }
    install(Logging) {
        logger = Logger.ANDROID
        level = LogLevel.ALL
    }
    install(DefaultRequest) {
        url {
            protocol = URLProtocol.HTTPS
            host = BuildConfig.BASE_URL
        }
    }

    install(Auth) {
        bearer {
            loadTokens {
                val provider = tokenProvider.value
                BearerTokens(
                    accessToken = provider.accessToken,
                    refreshToken = provider.refreshToken
                )
            }

            refreshTokens {
                val provider = tokenProvider.value
                val newToken = provider.refreshToken()
                if (newToken != null) {
                    BearerTokens(
                        accessToken = provider.accessToken,
                        refreshToken = provider.refreshToken
                    )
                } else {
                    null
                }
            }
        }
    }

    HttpResponseValidator {
        validateResponse { response ->
            if (!response.status.isSuccess()) {
                val apiError: ApiError = try {
                    response.body()
                } catch (e: Exception) {
                    ApiError(
                        status = response.status.description,
                        code = response.status.value.toString(),
                        message = e.message ?: "Unknown error"
                    )
                }
                throw ApiException(apiError)
            }
        }
    }

}