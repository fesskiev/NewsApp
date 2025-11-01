package org.news.network.di

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.URLProtocol
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.news.network.ApiException
import org.news.network.BuildConfig
import org.news.network.NewsApiService
import org.news.network.NewsApiServiceImpl
import org.news.network.model.ApiError

val networkModule = module {

    single {
        buildKtorClient()
    }

    single<NewsApiService> {
        NewsApiServiceImpl(httpClient = get())
    }
}

private fun buildKtorClient() = HttpClient(Android) {
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
        level = LogLevel.INFO
    }
    install(DefaultRequest) {
        url {
            protocol = URLProtocol.HTTPS
            host = BuildConfig.BASE_URL
        }
    }
    install(
        createClientPlugin("ApiKeyPlugin") {
            onRequest { request, _ ->
                request.url.parameters.append("apiKey", BuildConfig.APP_KEY)
            }
        }
    )
    HttpResponseValidator {
        validateResponse { response ->
            if (!response.status.isSuccess()) {
                val errorBody: ApiError = try {
                    response.body()
                } catch (e: Exception) {
                    ApiError(
                        status = response.status.description,
                        code = response.status.value.toString(),
                        message = e.message ?: "Unknown error"
                    )
                }
                throw ApiException(errorBody)
            }
        }
    }

}
