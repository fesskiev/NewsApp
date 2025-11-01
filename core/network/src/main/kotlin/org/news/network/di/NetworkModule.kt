package org.news.network.di

import org.koin.dsl.module
import org.news.network.NewsApiService
import org.news.network.NewsApiServiceImpl
import org.news.network.buildKtorClient
import org.news.network.token.TokenProvider
import org.news.network.token.TokenProviderImpl

val networkModule = module {
    single {
        buildKtorClient(tokenProvider = lazy { get<TokenProvider>() })
    }

    single<NewsApiService> {
        NewsApiServiceImpl(httpClient = get())
    }

    single<TokenProvider> {
        TokenProviderImpl(apiService = get())
    }
}
