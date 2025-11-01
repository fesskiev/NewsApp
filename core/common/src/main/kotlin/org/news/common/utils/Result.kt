package org.news.common.utils

sealed class Result<out S, out E> {
    data class Success<S>(val data: S) : Result<S, Nothing>()
    data class Failure<E>(val error: E) : Result<Nothing, E>()

    fun isSuccess(): Boolean = this is Success
    fun isFailure(): Boolean = this is Failure
    fun getOrNull(): S? = (this as? Success)?.data
    fun exceptionOrNull(): E? = (this as? Failure)?.error
}