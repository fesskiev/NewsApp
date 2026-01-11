package org.news.security.keys

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import org.news.security.di.ANDROID_KEYSTORE_PROVIDER
import java.security.KeyStore
import java.security.KeyStoreException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
const val GCM_IV_LENGTH = 12
private const val GCM_TAG_LENGTH = 128
private const val TAG = "TokenEncryptionKeyManager"

interface TokenEncryptionKeyManager {
    fun generateAESKeyIfNeed(keyAlias: String): Boolean
    fun getCipherForEncryption(keyAlias: String): Cipher?
    fun getCipherForDecryption(keyAlias: String, iv: ByteArray): Cipher?
    fun encryptToken(cipher: Cipher, token: String): ByteArray?
    fun decryptToken(cipher: Cipher, encryptedToken: ByteArray): String?
}

class TokenEncryptionKeyManagerImpl(private val keyStore: KeyStore) : TokenEncryptionKeyManager {

    override fun generateAESKeyIfNeed(keyAlias: String): Boolean {
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

    override fun getCipherForEncryption(keyAlias: String): Cipher? {
        return try {
            val aesKey = keyStore.getKey(keyAlias, null) as? SecretKey
                ?: throw KeyStoreException("AES key not found for alias: $keyAlias")

            Cipher.getInstance(AES_TRANSFORMATION).apply {
                init(Cipher.ENCRYPT_MODE, aesKey)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Cipher for encryption: $keyAlias", e)
            null
        }
    }

    override fun getCipherForDecryption(keyAlias: String, iv: ByteArray): Cipher? {
        return try {
            val aesKey = keyStore.getKey(keyAlias, null) as? SecretKey
                ?: throw KeyStoreException("AES key not found for alias: $keyAlias")

            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            Cipher.getInstance(AES_TRANSFORMATION).apply {
                init(Cipher.DECRYPT_MODE, aesKey, spec)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Cipher for decryption: $keyAlias", e)
            null
        }
    }

    override fun encryptToken(cipher: Cipher, token: String): ByteArray? {
        return try {
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
            iv + encryptedBytes
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encrypt token", e)
            null
        }
    }

    override fun decryptToken(cipher: Cipher, encryptedToken: ByteArray): String? {
        return try {
            val ciphertext = encryptedToken.copyOfRange(GCM_IV_LENGTH, encryptedToken.size)
            val decryptedBytes = cipher.doFinal(ciphertext)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt token", e)
            null
        }
    }
}