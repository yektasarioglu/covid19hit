package com.yektasarioglu.covid19hit.base

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

import com.yektasarioglu.base.android.BaseActivity
import com.yektasarioglu.base.android.Theme
import com.yektasarioglu.covid19hit.MyApplication
import com.yektasarioglu.covid19hit.R

abstract class CustomBaseActivity : BaseActivity() {

    protected var currentTheme: Theme?
        get() = (application as? MyApplication)?.currentTheme
        set(value) {
            (application as? MyApplication)?.currentTheme = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate(...)")
        activity = this
        handleTheme()
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy(...)")
        activity = null
    }

    private fun handleTheme() {
        currentTheme?.let {
            Log.i("Theme", "Theme is changing")
            activity?.setTheme(it)
        }
    }

    protected fun themeName() : String {
        return if (currentTheme == R.style.Theme_Light) "Light"
        else "Dark"
    }

    companion object {
        var activity: AppCompatActivity? = null
    }

}