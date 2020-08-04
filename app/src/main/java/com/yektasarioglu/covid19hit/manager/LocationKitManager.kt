package com.yektasarioglu.covid19hit.manager

import android.app.Activity
import android.content.IntentSender
import android.os.Looper
import android.util.Log

import com.huawei.hms.common.ApiException
import com.huawei.hms.common.ResolvableApiException
import com.huawei.hms.location.*

import com.yektasarioglu.base.extension.TAG

class LocationKitManager(private var activity: Activity?) : IManager {

    private val MTAG = TAG

    // LocationKit Variables
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    //private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null

    init {
        Log.i(TAG, "initializeLocationService(...)")

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
    }

    override fun onDestroy() {
        removeLocationUpdates()
        activity = null
    }

    fun requestLocationUpdatesWithCallback(
        locationRequest: LocationRequest,
        locationCallback: LocationCallback
    ) {
        try {
            this.locationCallback = locationCallback

            val builder = LocationSettingsRequest.Builder().apply {
                addLocationRequest(locationRequest)
            }

            val locationSettingsRequest = builder.build()

            checkLocationRequirements(
                locationSettingsRequest = locationSettingsRequest,
                onSuccess = {
                    Log.i(MTAG, "Have permission, check location settings success")

                    // request location updates
                    fusedLocationProviderClient!!.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    ).addOnSuccessListener {
                        Log.i(MTAG, "YES")
                    }.addOnFailureListener {
                        Log.e(MTAG, "NO")
                    }
                },
                onFailed = {
                    Log.e(MTAG, "checkLocationSetting onFailure:" + it.message)

                    val statusCode = (it as ApiException).statusCode
                    when (statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                            val rae = it as ResolvableApiException
                            rae.startResolutionForResult(activity, 0)
                        } catch (sie: IntentSender.SendIntentException) {
                            Log.e(MTAG, "PendingIntent unable to execute request.")
                        }
                    }
                })

        } catch (e: Exception) {
            Log.e(MTAG, "requestLocationUpdatesWithCallback exception:" + e.message)
        }
    }

    // check devices settings before request location updates.
    private fun checkLocationRequirements(locationSettingsRequest: LocationSettingsRequest, onSuccess: (locationSettingsResponse: LocationSettingsResponse) -> Unit, onFailed: (e: Exception) -> Unit) {
        val settingsClient: SettingsClient? = LocationServices.getSettingsClient(activity)
        settingsClient?.checkLocationSettings(locationSettingsRequest)
            ?.addOnSuccessListener(onSuccess)
            ?.addOnFailureListener(onFailed)
    }

    private fun removeLocationUpdates() {
        Log.i(TAG, "removeLocationUpdates()")

        try {
            fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "removeLocationUpdates() - Exception is $e")
        }
    }

}