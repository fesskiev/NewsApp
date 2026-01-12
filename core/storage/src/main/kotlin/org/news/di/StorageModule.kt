package org.news.di

import org.koin.dsl.module
import org.news.storage.AuthDataStorage
import org.news.storage.AuthDataStorageImpl

val storageModule = module {
    single<AuthDataStorage> {
        AuthDataStorageImpl(
            dispatcher = get(),
            context = get()
        )
    }
}