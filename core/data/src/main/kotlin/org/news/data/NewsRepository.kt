package org.news.data

import org.news.model.Article
import org.news.network.NewsApiService
import org.news.network.mapToDomainArticles

interface NewsRepository {

    suspend fun searchNews(
        query: String,
        from: String,
        to: String
    ): List<Article>

    fun clearQueryCache(
        query: String,
        from: String,
        to: String
    ): Boolean
}

internal class NewsRepositoryImpl(
    private val apiService: NewsApiService
) : NewsRepository {

    // In-memory cache using a HashMap with a composite key
    private val cache = HashMap<String, List<Article>>()

    override suspend fun searchNews(
        query: String,
        from: String,
        to: String
    ): List<Article> {
        // Create a unique cache key based on all parameters
        val cacheKey = "$query|$from|$to"

        // Check if result is in cache
        cache[cacheKey]?.let { cachedArticles ->
            return cachedArticles
        }

        // Fetch from API if not in cache
        val remoteNews = apiService.searchNews(query, from, to).mapToDomainArticles()

        // Store in cache
        cache[cacheKey] = remoteNews

        return remoteNews
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