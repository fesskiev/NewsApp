package org.news.data

import org.news.common.utils.Result
import org.news.model.Article
import org.news.model.Error
import org.news.network.ApiException
import org.news.network.NewsApiService
import org.news.network.mapToDomainArticles
import kotlin.coroutines.cancellation.CancellationException

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

    suspend fun <T> safeApiCall(block: suspend () -> T): Result<T, Error> {
        return try {
            Result.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: ApiException) {
            Result.Failure(Error(message = e.error.message))
        } catch (e: Exception) {
            Result.Failure(Error(message = e.message ?: "Unknown error"))
        }
    }
}