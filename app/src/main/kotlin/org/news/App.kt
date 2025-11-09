package org.news

import android.app.Application
import com.news.auth.di.authModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.news.data.di.databModule
import org.news.di.newsFeedModule
import org.news.navigation.di.navigationModule
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
            navigationModule,
            authModule,
            newsFeedModule
        )
}