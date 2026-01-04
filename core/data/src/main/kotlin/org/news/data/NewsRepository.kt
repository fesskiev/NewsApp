package org.news.data

import org.news.common.utils.Result
import org.news.model.Article
import org.news.model.Error
import org.news.network.service.NewsApiService
import org.news.network.model.mapToDomainArticles

interface NewsRepository {

    suspend fun searchNews(
        query: String,
        from: String,
        to: String
    ): Result<List<Article>, Error>

    fun clearQueryCache(
        query: String,
        from: String,
        to: String
    ): Boolean
}

internal class NewsRepositoryImpl(
    private val apiService: NewsApiService
) : NewsRepository {

    private val cache = HashMap<String, List<Article>>()

    override suspend fun searchNews(
        query: String,
        from: String,
        to: String
    ): Result<List<Article>, Error> {
        val cacheKey = "$query|$from|$to"

        cache[cacheKey]?.let { cachedArticles ->
            return Result.Success(cachedArticles)
        }

        val result = safeApiCall { apiService.searchNews(query, from, to) }
        return when (result) {
            is Result.Success -> {
                val articles = result.data.mapToDomainArticles()
                cache[cacheKey] = articles
                Result.Success(articles)
            }

            is Result.Failure -> result
        }
    }

    override fun clearQueryCache(
        query: String,
        from: String,
        to: String
    ): Boolean {
        val cacheKey = "$query|$from|$to"
        return cache.remove(cacheKey) != null
    }
}