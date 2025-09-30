package org.news.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.news.ArticleListViewModel

val newsFeed = module {
    viewModel { ArticleListViewModel(repository = get()) }
}