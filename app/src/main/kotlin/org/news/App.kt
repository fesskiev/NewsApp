package org.news

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.news.data.di.databModule
import org.news.di.newsFeed
import org.news.network.di.networkModule

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@App)
            koinModules()
        }
    }

    private fun KoinApplication.koinModules() =
        modules(
            networkModule,
            databModule,
            newsFeed
        )
}