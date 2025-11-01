package org.news.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.news.network.model.NewsApiResponse
import org.news.network.model.TokenApiResponse

interface NewsApiService {

    suspend fun searchNews(
      query: String,
        from: String,
        to: String
    ): NewsApiResponse

    suspend fun refreshToken(refreshToken: String) : TokenApiResponse
}

internal class NewsApiServiceImpl(
    private val httpClient: HttpClient
) : NewsApiService {

    override suspend fun searchNews(
        query: String,
        from: String,
        to: String
    ): NewsApiResponse {
        return httpClient.get("/v2/everything") {
            url {
                parameters.append("q", query)
                parameters.append("from", from)
                parameters.append("to", to)
            }
        }.body()
    }

    override suspend fun refreshToken(refreshToken: String): TokenApiResponse {
        return TokenApiResponse(
            accessToken = BuildConfig.APP_KEY,
            refreshToken = BuildConfig.APP_KEY
        )
    }
}