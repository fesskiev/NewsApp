package org.news.security.keys

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import org.news.security.di.ANDROID_KEYSTORE_PROVIDER
import java.security.*

private const val SIGNATURE_ALGORITHM = "SHA256withRSA"
private const val TAG = "BiometricAuthenticationKeyManager"

interface BiometricAuthenticationKeyManager {
    fun generateKeyPairIfNeed(keyAlias: String): Boolean
    fun getPublicKey(keyAlias: String): PublicKey?
    fun getSignatureForAuthentication(keyAlias: String): Signature?
    fun signData(signature: Signature, data: ByteArray): ByteArray?
}

class BiometricAuthenticationKeyManagerImpl(
    private val keyStore: KeyStore
) : BiometricAuthenticationKeyManager {

    override fun generateKeyPairIfNeed(keyAlias: String): Boolean {
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
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationParameters(30, KeyProperties.AUTH_BIOMETRIC_STRONG)
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

fun verifySignature(publicKey: PublicKey, data: ByteArray, signature: ByteArray): Boolean {
    return try {
        val verifier = Signature.getInstance(SIGNATURE_ALGORITHM).apply {
            initVerify(publicKey)
            update(data)
        }
        verifier.verify(signature)
    } catch (e: Exception) {
        Log.e(TAG, "Verification error", e)
        false
    }
}