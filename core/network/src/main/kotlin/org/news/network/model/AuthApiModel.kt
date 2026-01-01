package org.news.network.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginApiRequest(
    val username: String,
    val password: String
)

@Serializable
data class BiometricRegistrationApiRequest(
    val userId: String,
    val publicKey: String
)

@Serializable
data class BiometricLoginApiRequest(
    val userId: String,
    val data: String,
    val signature: String
)

@Serializable
data class AuthApiResponse (
    val userId: String,
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class RefreshTokenApiRequest(
    val refreshToken: String
)