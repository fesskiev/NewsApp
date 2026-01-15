package org.news.splash.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.news.splash.SplashViewModel

val splashModule = module {
    viewModel {
        SplashViewModel(
            keyManager = get(),
            biometricManager = get(),
            authDataStorage = get(),
            tokenProvider = get(),
            globalNavigationEventBus = get()
        )
    }
}