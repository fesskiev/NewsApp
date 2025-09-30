package org.news.network

import org.news.model.Article
import org.news.network.model.NewsApiResponse

fun NewsApiResponse.mapToDomainArticles(): List<Article> {
    return articles.map { apiArticle ->
        Article(
            author = apiArticle.author ?: "Unknown Author",
            title = apiArticle.title ?: "No Title",
            description = apiArticle.description ?: "No Description"
        )
    }
}