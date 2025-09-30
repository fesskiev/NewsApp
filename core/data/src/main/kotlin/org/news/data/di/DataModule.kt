package org.news.data.di

import org.koin.dsl.module
import org.news.data.NewsRepository
import org.news.data.NewsRepositoryImpl

val databModule = module {

    single<NewsRepository> { NewsRepositoryImpl(apiService = get()) }

}