package org.news.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.*

interface KeyManager {
    fun generateKeyPair(keyAlias: String, isUserAuthenticationRequired: Boolean = true): Boolean
    fun getPublicKey(keyAlias: String): PublicKey?
    fun getSignatureForAuthentication(keyAlias: String): Signature?
    fun signData(signature: Signature, data: ByteArray): ByteArray?
}

class KeyManagerImpl(
    private val keyStore: KeyStore
) : KeyManager {

    private companion object {
        const val TAG = "KeyManagerImpl"
        const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val SIGNATURE_ALGORITHM = "SHA256withRSA"
    }

    override fun generateKeyPair(keyAlias: String, isUserAuthenticationRequired: Boolean): Boolean {
        return try {
            if (keyStore.containsAlias(keyAlias)) {
                Log.w(TAG, "Key alias '$keyAlias' already exists. Skipping generation.")
                return true
            }

            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                ANDROID_KEYSTORE_PROVIDER
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .setUserAuthenticationRequired(isUserAuthenticationRequired)
                .build()

            keyPairGenerator.initialize(keyGenParameterSpec)
            keyPairGenerator.generateKeyPair()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate key pair for alias: $keyAlias", e)
            false
        }
    }

    override fun getPublicKey(keyAlias: String): PublicKey? {
        return try {
            keyStore.getCertificate(keyAlias)?.publicKey
        } catch (e: KeyStoreException) {
            Log.e(TAG, "Failed to get public key for alias: $keyAlias", e)
            null
        }
    }

    override fun getSignatureForAuthentication(keyAlias: String): Signature? {
        try {
            val privateKey = keyStore.getKey(keyAlias, null) as? PrivateKey
                ?: throw KeyStoreException("Private key not found for alias: $keyAlias")

            return Signature.getInstance(SIGNATURE_ALGORITHM).apply {
                initSign(privateKey)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Signature for alias: $keyAlias", e)
            return null
        }
    }

    override fun signData(signature: Signature, data: ByteArray): ByteArray? {
        return try {
            signature.update(data)
            signature.sign()
        } catch (e: SignatureException) {
            Log.e(TAG, "Failed to sign data", e)
            null
        }
    }
}