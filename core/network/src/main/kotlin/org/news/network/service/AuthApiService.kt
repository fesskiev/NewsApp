package org.news.network.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.news.network.model.BiometricRegistrationApiRequest
import org.news.network.model.LoginApiRequest
import org.news.network.model.AuthApiResponse
import org.news.network.model.BiometricLoginApiRequest
import org.news.network.model.RefreshTokenApiRequest

interface AuthApiService {

    suspend fun login(username: String, password: String): AuthApiResponse

    suspend fun registerBiometric(userId: String, publicKey: String): AuthApiResponse

    suspend fun loginBiometric(userId: String, data: String, signature: String): AuthApiResponse

    suspend fun refreshToken(refreshToken: String): AuthApiResponse
}

internal class AuthApiServiceImpl(
    private val httpClient: HttpClient
): AuthApiService {

    override suspend fun login(username: String, password: String): AuthApiResponse {
        return httpClient.post("/auth/login") {
            setBody(LoginApiRequest(username, password))
        }.body()
    }

    override suspend fun registerBiometric(userId: String, publicKey: String): AuthApiResponse {
        return httpClient.post("/auth/biometric/register") {
            setBody(BiometricRegistrationApiRequest(userId, publicKey))
        }.body()
    }

    override suspend fun loginBiometric(userId: String, data: String, signature: String): AuthApiResponse {
        return httpClient.post("/auth/biometric/login") {
            setBody(BiometricLoginApiRequest(userId, data, signature))
        }.body()
    }

    override suspend fun refreshToken(refreshToken: String): AuthApiResponse {
        return httpClient.post("/auth/refresh") {
            setBody(RefreshTokenApiRequest(refreshToken))
        }.body()
    }
}