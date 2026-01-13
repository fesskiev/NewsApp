package org.news.data

import org.news.network.ApiException
import kotlin.coroutines.cancellation.CancellationException
import org.news.common.utils.Result
import org.news.model.Failure

suspend fun <T> safeApiCall(block: suspend () -> T): Result<T, Failure> {
    return try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: ApiException) {
        Result.Failure(Failure(message = e.error.message))
    } catch (e: Exception) {
        Result.Failure(Failure(message = e.message ?: "Unknown error"))
    }
}