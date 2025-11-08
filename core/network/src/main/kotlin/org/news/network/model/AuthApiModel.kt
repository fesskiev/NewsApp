package org.news.network.model

import kotlinx.serialization.Serializable

@Serializable
data class TokenApiResponse (
    val accessToken: String,
    val refreshToken: String
)