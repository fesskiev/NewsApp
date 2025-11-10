package org.news.security.di

import org.koin.dsl.module
import org.news.security.keys.ANDROID_KEYSTORE_PROVIDER
import org.news.security.keys.KeyManager
import org.news.security.keys.KeyManagerImpl
import org.news.security.biometric.BiometricManager
import org.news.security.biometric.BiometricManagerImpl
import java.security.KeyStore

val securityModule = module {
    single { KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER).apply { load(null) } }
    single<KeyManager> { KeyManagerImpl(keyStore = get()) }
    single<BiometricManager> { BiometricManagerImpl(context = get()) }
}