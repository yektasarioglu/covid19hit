package com.yektasarioglu.covid19hit

import android.app.Application

import com.yektasarioglu.base.android.Theme
import com.yektasarioglu.covid19hit.di.appModule
import com.yektasarioglu.covid19hit.di.networkModule
import com.yektasarioglu.network.utils.appContext

import io.paperdb.Paper

import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MyApplication : Application() {

    var currentTheme: Theme? = null
        get() = Paper.book().read(THEME_KEY)
        set(value) {
            field = value ?: 0
            Paper.book().write(THEME_KEY, value ?: 0)
        }

    override fun onCreate() {
        super.onCreate()

        appContext = applicationContext

        Paper.init(applicationContext)

        if (currentTheme == null)
            currentTheme = R.style.Theme_Light

        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(appModule + networkModule)
        }
    }

}