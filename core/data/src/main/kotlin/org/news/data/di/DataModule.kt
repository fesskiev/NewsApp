package org.news.data.di

import org.koin.dsl.module
import org.news.data.AuthRepository
import org.news.data.AuthRepositoryImpl
import org.news.data.NewsRepository
import org.news.data.NewsRepositoryImpl

val databModule = module {

    single<AuthRepository> { AuthRepositoryImpl(authApiService = get()) }
    single<NewsRepository> { NewsRepositoryImpl(apiService = get()) }

}