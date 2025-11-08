package org.news.network.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.news.network.model.NewsApiResponse

interface NewsApiService {

    suspend fun searchNews(
      query: String,
        from: String,
        to: String
    ): NewsApiResponse
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
}