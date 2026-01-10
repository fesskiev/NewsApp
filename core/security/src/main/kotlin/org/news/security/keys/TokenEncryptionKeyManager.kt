package org.news.security.keys

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import org.news.security.di.ANDROID_KEYSTORE_PROVIDER
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

interface TokenEncryptionKeyManager {
    fun generateAesKeyForTokenEncryption(keyAlias: String): Boolean
    fun getAesKeyForTokenEncryption(keyAlias: String): SecretKey?
    fun encryptToken(aesKey: SecretKey, token: String): ByteArray?
    fun decryptToken(aesKey: SecretKey, encryptedToken: ByteArray): String?
}

private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
private const val GCM_IV_LENGTH = 12  // Standard for GCM
private const val GCM_TAG_LENGTH = 128 // Bits
private const val TAG = "TokenEncryptionKeyManager"

class TokenEncryptionKeyManagerImpl(private val keyStore: KeyStore) : TokenEncryptionKeyManager {

    override fun generateAesKeyForTokenEncryption(keyAlias: String): Boolean {
        return try {
            if (keyStore.containsAlias(keyAlias)) {
                Log.w(TAG, "AES key alias '$keyAlias' already exists. Skipping generation.")
                return true
            }
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE_PROVIDER
            )
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationParameters(30, KeyProperties.AUTH_BIOMETRIC_STRONG)
                .build()
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate AES key for alias: $keyAlias", e)
            false
        }
    }

    override fun getAesKeyForTokenEncryption(keyAlias: String): SecretKey? {
        return try {
            keyStore.getKey(keyAlias, null) as? SecretKey
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get AES key for alias: $keyAlias", e)
            null
        }
    }

    override fun encryptToken(aesKey: SecretKey, token: String): ByteArray? {
        return try {
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, aesKey)
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
            iv + encryptedBytes
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encrypt token", e)
            null
        }
    }

    override fun decryptToken(aesKey: SecretKey, encryptedToken: ByteArray): String? {
        return try {
            val iv = encryptedToken.copyOfRange(0, GCM_IV_LENGTH)
            val ciphertext = encryptedToken.copyOfRange(GCM_IV_LENGTH, encryptedToken.size)
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, aesKey, spec)
            val decryptedBytes = cipher.doFinal(ciphertext)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt token", e)
            null
        }
    }
}