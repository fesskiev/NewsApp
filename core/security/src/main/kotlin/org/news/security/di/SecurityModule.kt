package org.news.security.di

import org.koin.dsl.module
import org.news.security.keys.BiometricAuthenticationKeyManager
import org.news.security.keys.BiometricAuthenticationKeyManagerImpl
import org.news.security.biometric.BiometricManager
import org.news.security.biometric.BiometricManagerImpl
import org.news.security.keys.TokenEncryptionKeyManager
import org.news.security.keys.TokenEncryptionKeyManagerImpl
import java.security.KeyStore

internal const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"

val securityModule = module {
    single { KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER).apply { load(null) } }
    single<BiometricAuthenticationKeyManager> { BiometricAuthenticationKeyManagerImpl(keyStore = get()) }
    single<TokenEncryptionKeyManager> { TokenEncryptionKeyManagerImpl(keyStore = get()) }
    single<BiometricManager> { BiometricManagerImpl(context = get()) }
}