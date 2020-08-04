package com.yektasarioglu.network.utils

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log


import com.yektasarioglu.network.INSTITUTION_LIST
import com.yektasarioglu.network.model.Institution

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

import io.paperdb.Paper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

var appContext: Context? = null

val isInstitutionListCached: Boolean
    get() {
        val value = Paper.book().read<ArrayList<Institution>>(INSTITUTION_LIST)
        return value != null
    }

suspend fun getIsConnected() : Boolean {
    return GlobalScope.async(Dispatchers.IO) {
        return@async isConnected(appContext)
    }.await()
}

fun isConnected(context: Context?) : Boolean {
    if (context == null) {
        Log.e("Utility", "Context must not be NULL !!")
        return false
    }

    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = cm.activeNetworkInfo

    return if (activeNetwork != null && activeNetwork.isConnected) {
        try {
            val url = URL("http://www.google.com/")
            val urlc: HttpURLConnection = url.openConnection() as HttpURLConnection
            urlc.setRequestProperty("User-Agent", "test")
            urlc.setRequestProperty("Connection", "close")
            urlc.connectTimeout = 4000 // mTimeout is in seconds
            urlc.connect()
            urlc.responseCode == 200
        } catch (e: IOException) {
            Log.i("WARNING", "Error checking internet connection", e)
            false
        }
    } else false
}