package org.news.network.model

import kotlinx.serialization.Serializable

@Serializable
data class BiometricRegistrationApiRequest(
    val userId: String,
    val publicKey: String,
    val deviceId: String
)

@Serializable
data class BiometricAuthApiRequest(
    val userId: String,
    val signature: String,
    val deviceId: String,
    val timestamp: Long
)

@Serializable
data class TokenApiResponse (
    val accessToken: String,
    val refreshToken: String
)