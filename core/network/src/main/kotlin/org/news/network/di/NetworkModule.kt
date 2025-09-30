package org.news.network.di

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.logging.HttpLoggingInterceptor
import org.news.network.ApiKeyInterceptor
import org.news.network.BuildConfig
import org.news.network.NewsApiService

val networkModule = module {

    single {
        buildApiService(
            retrofit = get()
        )
    }

    single {
        buildRetrofit(
            okHttpClient = get()
        )
    }

    single {
        buildOkHttpClient()
    }
}

private fun buildApiService(
    retrofit: Retrofit,
): NewsApiService {
    return retrofit.create(NewsApiService::class.java)
}

private fun buildOkHttpClient(): OkHttpClient {
    val timeOut = 60L
    return OkHttpClient.Builder().apply {
        readTimeout(timeOut, TimeUnit.SECONDS)
        writeTimeout(timeOut, TimeUnit.SECONDS)
        connectTimeout(timeOut, TimeUnit.SECONDS)
        addInterceptor(ApiKeyInterceptor(BuildConfig.APP_KEY))
        if (BuildConfig.DEBUG) {
            val logging =
                HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY)
            addInterceptor(logging)
        }
    }.build()
}

private fun buildRetrofit(
    okHttpClient: OkHttpClient,
): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .client(okHttpClient)
        .build()
}