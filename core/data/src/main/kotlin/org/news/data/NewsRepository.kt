package org.news.data

import kotlinx.serialization.json.Json
import org.news.common.utils.Result
import org.news.model.Article
import org.news.model.Error
import org.news.network.NewsApiService
import org.news.network.mapToDomainArticles
import org.news.network.model.ApiError
import retrofit2.Response
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

    private suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>,
    ): Result<T, Error> = try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
                ?: throw IllegalStateException("Empty response body")
            Result.Success(body)
        } else {
            Result.Failure(Error(message = response.getErrorMessage()))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.Failure(Error(e.message ?: "Unknown error"))
    }

    private fun <T> Response<T>.getErrorMessage(): String {
        val errorBody = errorBody()?.string()
        val message: String = if (errorBody != null) {
            try {
                Json.decodeFromString<ApiError>(errorBody).message
            } catch (e: Exception) {
                "Can't decode error body" + e.message
            }
        } else {
            "Error body is empty"
        }
        return message
    }

}