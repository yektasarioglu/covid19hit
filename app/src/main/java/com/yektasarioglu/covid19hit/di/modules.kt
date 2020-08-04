package com.yektasarioglu.covid19hit.di

import com.yektasarioglu.covid19hit.BuildConfig
import com.yektasarioglu.network.repository.HealthInstitutionRepository
import com.yektasarioglu.covid19hit.ui.home.viewmodel.HomeViewModel
import com.yektasarioglu.network.DirectionsService
import com.yektasarioglu.network.repository.DirectionsRepository
import com.yektasarioglu.network.repository.datasource.*

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import java.util.concurrent.TimeUnit

val appModule = module {

    // Data Sources
    single { DiskHealthInstitutionDataSource() }
    single { RemoteHealthInstitutionDataSource() }

    // Repositories
    single { HealthInstitutionRepository(get(), get()) }

    viewModel { HomeViewModel(get(), get()) }

}

val networkModule = module {

    // Data Sources
    single { RemoteDirectionDataSource(get()) }

    // Repositories
    single { DirectionsRepository(get()) }

    single {
        OkHttpClient().newBuilder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            })
            .connectTimeout(10000, TimeUnit.MILLISECONDS)
            .readTimeout(10000, TimeUnit.MILLISECONDS)
            .build()
    }

    // Services
    single {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://mapapi.cloud.huawei.com/mapApi/")
            .client(get())
            .build()

        retrofit.create(DirectionsService::class.java)
    }

}