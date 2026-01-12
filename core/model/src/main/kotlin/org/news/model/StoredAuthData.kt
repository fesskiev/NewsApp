package org.news.model

import kotlin.time.Clock

data class StoredAuthData(
    val encryptedAccessToken: ByteArray,
    val encryptedRefreshToken: ByteArray,
    val accessTokenExpiresAt: Long,
    val refreshTokenExpiresAt: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StoredAuthData

        if (accessTokenExpiresAt != other.accessTokenExpiresAt) return false
        if (refreshTokenExpiresAt != other.refreshTokenExpiresAt) return false
        if (!encryptedAccessToken.contentEquals(other.encryptedAccessToken)) return false
        if (!encryptedRefreshToken.contentEquals(other.encryptedRefreshToken)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = accessTokenExpiresAt.hashCode()
        result = 31 * result + refreshTokenExpiresAt.hashCode()
        result = 31 * result + encryptedAccessToken.contentHashCode()
        result = 31 * result + encryptedRefreshToken.contentHashCode()
        return result
    }
}

enum class TokenExpiryStatus {
    ACCESS_VALID,
    REFRESH_VALID,
    BOTH_EXPIRED
}

fun StoredAuthData.getTokenExpiryStatus(): TokenExpiryStatus {
    val now = Clock.System.now().epochSeconds
    return when {
        now < accessTokenExpiresAt -> TokenExpiryStatus.ACCESS_VALID
        now in accessTokenExpiresAt..<refreshTokenExpiresAt -> TokenExpiryStatus.REFRESH_VALID
        else -> TokenExpiryStatus.BOTH_EXPIRED
    }
}