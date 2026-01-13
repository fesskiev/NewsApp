package org.news.security.biometric

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.Signature
import kotlin.coroutines.resume
import org.news.common.utils.Result
import org.news.model.Error

suspend fun FragmentActivity.launchBiometricAuthenticator(signature: Signature): Result<Signature, Error> =
    suspendCancellableCoroutine { continuation ->
        val biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    result.cryptoObject?.signature?.let {
                        continuation.resume(Result.Success(it))
                    } ?: continuation.resume(
                        Result.Failure(Error("Signature not available"))
                    )
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    continuation.resume(Result.Failure(Error(errString.toString())))
                }

                override fun onAuthenticationFailed() {
                    continuation.resume(Result.Failure(Error("Authentication failed")))
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use password")
            .build()

        val crypto = BiometricPrompt.CryptoObject(signature)
        biometricPrompt.authenticate(promptInfo, crypto)

        continuation.invokeOnCancellation {
            biometricPrompt.cancelAuthentication()
        }
    }

suspend fun FragmentActivity.launchBiometricAuthenticator(): Result<Unit, Error> =
    suspendCancellableCoroutine { continuation ->
        val biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    continuation.resume(Result.Success(Unit))
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    continuation.resume(Result.Failure(Error(errString.toString())))
                }

                override fun onAuthenticationFailed() {
                    continuation.resume(Result.Failure(Error("Authentication failed")))
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use password")
            .build()

        biometricPrompt.authenticate(promptInfo)

        continuation.invokeOnCancellation {
            biometricPrompt.cancelAuthentication()
        }
    }