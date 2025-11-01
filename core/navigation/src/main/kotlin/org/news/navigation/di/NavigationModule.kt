package org.news.navigation.di

import org.koin.dsl.module
import org.news.navigation.GlobalNavigationEventBus
import org.news.navigation.GlobalNavigationEventBusImpl

val navigationModule = module {
    single<GlobalNavigationEventBus> { GlobalNavigationEventBusImpl() }
}