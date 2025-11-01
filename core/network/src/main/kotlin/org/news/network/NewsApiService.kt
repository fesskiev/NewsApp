package org.news.network

import org.news.network.model.NewsApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    @GET("v2/everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): Response<NewsApiResponse>
}