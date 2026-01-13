package org.news.security.biometric

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.Signature
import kotlin.coroutines.resume
import org.news.common.utils.Result
import org.news.model.Failure

data class PromptConfig(
    val title: String,
    val subtitle: String,
    val negativeButtonText: String? = null
)

suspend fun FragmentActivity.launchBiometricAuthenticator(
    config: PromptConfig,
    signature: Signature
): Result<Signature, Failure> =
    suspendCancellableCoroutine { continuation ->
        val biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    result.cryptoObject?.signature?.let {
                        continuation.resume(Result.Success(it))
                    } ?: continuation.resume(
                        Result.Failure(Failure("Signature not available"))
                    )
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    continuation.resume(Result.Failure(Failure(errString.toString())))
                }

                override fun onAuthenticationFailed() {
                    continuation.resume(Result.Failure(Failure("Authentication failed")))
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(config.title)
            .setSubtitle(config.subtitle)
            .apply { config.negativeButtonText?.let(::setNegativeButtonText) }
            .build()

        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(signature))
        continuation.invokeOnCancellation {
            biometricPrompt.cancelAuthentication()
        }
    }

suspend fun FragmentActivity.launchBiometricAuthenticator(config: PromptConfig): Result<Unit, Failure> =
    suspendCancellableCoroutine { continuation ->
        val biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    continuation.resume(Result.Success(Unit))
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    continuation.resume(Result.Failure(Failure(errString.toString())))
                }

                override fun onAuthenticationFailed() {
                    continuation.resume(Result.Failure(Failure("Authentication failed")))
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(config.title)
            .setSubtitle(config.subtitle)
            .apply { config.negativeButtonText?.let(::setNegativeButtonText) }
            .build()

        biometricPrompt.authenticate(promptInfo)

        continuation.invokeOnCancellation {
            biometricPrompt.cancelAuthentication()
        }
    }