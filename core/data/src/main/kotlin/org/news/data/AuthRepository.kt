package org.news.data

import org.news.network.model.AuthApiResponse
import org.news.network.service.AuthApiService

interface AuthRepository {

    suspend fun login(username: String, password: String): AuthApiResponse

    suspend fun registerBiometric(userId: String, publicKey: String): AuthApiResponse

    suspend fun loginBiometric(userId: String, data: String, signature: String): AuthApiResponse
}

internal class AuthRepositoryImpl(
    private val authApiService: AuthApiService
) : AuthRepository {

    override suspend fun login(
        username: String,
        password: String
    ) = authApiService.login(username, password)


    override suspend fun registerBiometric(
        userId: String,
        publicKey: String
    ) = authApiService.registerBiometric(userId, publicKey)

    override suspend fun loginBiometric(
        userId: String,
        data: String,
        signature: String
    ) = authApiService.loginBiometric(userId, data, signature)

}