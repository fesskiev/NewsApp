package com.news.auth.di

import com.news.auth.BiometricAuthViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    viewModel { BiometricAuthViewModel(keyManager = get(), biometricManager = get()) }
}