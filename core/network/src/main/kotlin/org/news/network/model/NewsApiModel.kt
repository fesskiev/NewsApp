package org.news.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NewsApiResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<ArticleApi>
)

@Serializable
data class ArticleApi(
    val source: SourceApi?,
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?
)

@Serializable
data class SourceApi(
    val id: String?,
    val name: String?
)

@Serializable
data class ApiError (
    val status: String,
    val code: String,
    val message: String
)

@Serializable
data class TokenApiResponse (
    val accessToken: String,
    val refreshToken: String
)