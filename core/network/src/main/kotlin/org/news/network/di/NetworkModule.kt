package org.news.network.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.news.network.service.NewsApiService
import org.news.network.service.NewsApiServiceImpl
import org.news.network.buildKtorClient
import org.news.network.buildMockKtorEngine
import org.news.network.service.AuthApiService
import org.news.network.service.AuthApiServiceImpl
import org.news.network.token.TokenProvider
import org.news.network.token.TokenProviderImpl

val networkModule = module {

    single<TokenProvider> {
        TokenProviderImpl(
            authApiService = get(),
            globalNavigationEventBus = get()
        )
    }

    single<HttpClientEngine>(named("real")) { Android.create() }
    single<HttpClientEngine>(named("mock")) { buildMockKtorEngine() }


    single<HttpClient>(named("auth")) {
        buildKtorClient(
            engine = get<HttpClientEngine>(named("mock")),
            tokenProvider = lazy { get<TokenProvider>() }
        )
    }

    single<AuthApiService> {
        AuthApiServiceImpl(httpClient = get<HttpClient>(named("auth")))
    }


    single<HttpClient>(named("news")) {
        buildKtorClient(
            engine = get<HttpClientEngine>(named("real")),
            tokenProvider = lazy { get<TokenProvider>() }
        )
    }

    single<NewsApiService> {
        NewsApiServiceImpl(httpClient = get<HttpClient>(named("news")))
    }
}
