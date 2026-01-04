package org.news.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponse (
    val status: String,
    val code: String,
    val message: String
)