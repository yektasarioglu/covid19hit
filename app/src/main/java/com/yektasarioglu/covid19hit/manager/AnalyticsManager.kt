package com.yektasarioglu.covid19hit.manager

import android.content.Context
import android.os.Bundle

import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.analytics.HiAnalyticsInstance
import com.huawei.hms.analytics.HiAnalyticsTools

class AnalyticsManager(context: Context) : IManager {

    private var instance: HiAnalyticsInstance? = null

    init {
        HiAnalyticsTools.enableLog()

        instance = HiAnalytics.getInstance(context)
    }

    override fun onDestroy() {
        instance = null
    }

    fun sendEvent(key: String, bundle: Bundle) = instance?.onEvent(key, bundle)

}