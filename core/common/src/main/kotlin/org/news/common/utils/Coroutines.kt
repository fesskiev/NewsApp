package org.news.common.utils

import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> executeWithTryCatch(
    operation: suspend () -> T
): Result<T> = try {
    Result.success(operation())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
