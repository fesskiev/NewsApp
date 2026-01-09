package org.news.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.news.network.model.ApiErrorResponse
import org.news.network.model.AuthApiResponse

internal fun buildMockKtorEngine(): MockEngine {
    return MockEngine { request ->
        when (request.method) {
            HttpMethod.Post if request.url.rawSegments.last() == "login" -> {
                val requestBody = request.body.toByteArray().decodeToString()
                val jsonElement = Json.parseToJsonElement(requestBody)
                val username = jsonElement.jsonObject["user name"]?.jsonPrimitive?.content
                val password = jsonElement.jsonObject["password"]?.jsonPrimitive?.content
                if (username == "test@gmail.com" && password == "qwerty") {
                    val responseJson = Json.encodeToString(AuthApiResponse.serializer(), successAuthResponse)
                    respond(
                        content = ByteReadChannel(responseJson),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                } else {
                    val errorJson = Json.encodeToString(ApiErrorResponse.serializer(), unauthorizedError)
                    respond(
                        content = ByteReadChannel(errorJson),
                        status = HttpStatusCode.Unauthorized,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
            }
            HttpMethod.Post if request.url.rawSegments.last() == "register" -> {
                val responseJson = Json.encodeToString(AuthApiResponse.serializer(), successAuthResponse)
                respond(
                    content = ByteReadChannel(responseJson),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }
            else -> error("Unhandled request: ${request.method} ${request.url}")
        }
    }
}

private val successAuthResponse = AuthApiResponse(
    userId = "12345-abcde-67890",
    accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
    refreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5d"
)

private val unauthorizedError = ApiErrorResponse(
    status = "401",
    code = "AUTH_INVALID_CREDENTIALS",
    message = "The provided username or password is incorrect."
)

private val badRequestError = ApiErrorResponse(
    status = "400",
    code = "VALIDATION_MISSING_FIELD",
    message = "Email address is required."
)

private val forbiddenError = ApiErrorResponse(
    status = "403",
    code = "PERMISSION_DENIED",
    message = "You do not have access to this resource."
)

private val notFoundError = ApiErrorResponse(
    status = "404",
    code = "RESOURCE_NOT_FOUND",
    message = "User with ID '12345' does not exist."
)

private val serverError = ApiErrorResponse(
    status = "500",
    code = "INTERNAL_SERVER_ERROR",
    message = "An unexpected error occurred. Please try again later."
)

private val rateLimitError = ApiErrorResponse(
    status = "429",
    code = "RATE_LIMIT_EXCEEDED",
    message = "Too many requests. Please wait before trying again."
)
