package org.news.network.di

import io.ktor.client.engine.android.Android
import org.koin.dsl.module
import org.news.network.BuildConfig
import org.news.network.service.NewsApiService
import org.news.network.service.NewsApiServiceImpl
import org.news.network.buildKtorClient
import org.news.network.buildMockKtorEngine
import org.news.network.service.AuthApiService
import org.news.network.service.AuthApiServiceImpl
import org.news.network.token.TokenProvider
import org.news.network.token.TokenProviderImpl

val networkModule = module {
    single {
        buildKtorClient(
            engine = if (BuildConfig.USE_MOCKS) {
                buildMockKtorEngine()
            } else {
                Android.create()
            },
            tokenProvider = lazy { get<TokenProvider>() }
        )
    }

    single<NewsApiService> {
        NewsApiServiceImpl(httpClient = get())
    }

    single<AuthApiService> {
        AuthApiServiceImpl(httpClient = get())
    }

    single<TokenProvider> {
        TokenProviderImpl(
            authApiService = get(),
            globalNavigationEventBus = get()
        )
    }
}
