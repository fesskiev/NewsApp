package org.news.data

import org.news.common.utils.Result
import org.news.network.model.AuthApiResponse
import org.news.network.service.AuthApiService
import org.news.model.Failure

interface AuthRepository {

    suspend fun login(username: String, password: String): Result<AuthApiResponse, Failure>

    suspend fun registerBiometric(userId: String, publicKey: String): Result<AuthApiResponse, Failure>

    suspend fun loginBiometric(userId: String, data: String, signature: String): Result<AuthApiResponse, Failure>
}

internal class AuthRepositoryImpl(
    private val authApiService: AuthApiService
) : AuthRepository {

    override suspend fun login(
        username: String,
        password: String
    ) = safeApiCall {authApiService.login(username, password) }


    override suspend fun registerBiometric(
        userId: String,
        publicKey: String
    ) = safeApiCall {  authApiService.registerBiometric(userId, publicKey) }

    override suspend fun loginBiometric(
        userId: String,
        data: String,
        signature: String
    ) = safeApiCall { authApiService.loginBiometric(userId, data, signature) }

}