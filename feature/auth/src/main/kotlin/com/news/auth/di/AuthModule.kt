package com.news.auth.di

import com.news.auth.SplashViewModel
import com.news.auth.LoginViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    viewModel {
        SplashViewModel(
            keyManager = get(),
            biometricManager = get(),
            authDataStorage = get(),
            tokenProvider = get(),
            globalNavigationEventBus = get()
        )
    }
    viewModel {
        LoginViewModel(
            keyManager = get(),
            biometricManager = get(),
            authRepository = get()
        )
    }
}