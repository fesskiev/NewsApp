package org.news.network.model

import org.news.model.Article

fun NewsApiResponse.mapToDomainArticles(): List<Article> {
    return articles.map { apiArticle ->
        Article(
            author = apiArticle.author ?: "Unknown Author",
            title = apiArticle.title ?: "No Title",
            description = apiArticle.description ?: "No Description"
        )
    }
}