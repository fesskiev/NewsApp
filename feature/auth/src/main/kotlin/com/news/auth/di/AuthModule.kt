package com.news.auth.di

import com.news.auth.LoginViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    viewModel {
        LoginViewModel(
            keyManager = get(),
            biometricManager = get(),
            authRepository = get()
        )
    }
}