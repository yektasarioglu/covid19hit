package com.yektasarioglu.network.repository.datasource

import android.util.Log

import com.yektasarioglu.network.BuildConfig
import com.yektasarioglu.network.DirectionsService
import com.yektasarioglu.network.request.RouteDirection
import com.yektasarioglu.network.response.RouteResponse

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import retrofit2.HttpException

import java.net.URLEncoder

const val KEY = BuildConfig.HMS_API_KEY

class RemoteDirectionDataSource(
    private val directionsService: DirectionsService
) : DirectionOperations {

    private val encodedKey: String
        get() = URLEncoder.encode(KEY, "UTF-8").replace("\\+", "%20")

    override suspend fun getBicyclingRoute(routeDirection: RouteDirection, onError: ((String) -> Unit)?): RouteResponse? = withContext(Dispatchers.IO) {
        val routeResponse = directionsService.getBicyclingRoute(key = KEY, routeDirection = routeDirection)
        //Log.i("RDDS", "response is $walkingRoute")

        try {
            if (routeResponse.isSuccessful)
                Log.i("RDDS", "response is ${routeResponse.body()}")
            else {
                launch (Dispatchers.Main) {
                    onError?.invoke("${routeResponse.body()?.returnCode} - ${routeResponse.body()?.returnDesc}")
                }
            }
        } catch (e: HttpException) {
            Log.e("RDDS", "Exception e is ${e.message()}")
        }

        routeResponse.body()
    }

    override suspend fun getDrivingRoute(routeDirection: RouteDirection, onError: ((String) -> Unit)?): RouteResponse? = withContext(Dispatchers.IO) {
        val routeResponse = directionsService.getDrivingRoute(key = KEY, routeDirection = routeDirection)
        //Log.i("RDDS", "response is $walkingRoute")

        try {
            if (routeResponse.isSuccessful)
                Log.i("RDDS", "response is ${routeResponse.body()}")
            else {
                launch (Dispatchers.Main) {
                    onError?.invoke("${routeResponse.body()?.returnCode} - ${routeResponse.body()?.returnDesc}")
                }
            }
        } catch (e: HttpException) {
            Log.e("RDDS", "Exception e is ${e.message()}")
        }

        routeResponse.body()
    }

    override suspend fun getWalkingRoute(routeDirection: RouteDirection, onError: ((String) -> Unit)?): RouteResponse? = withContext(Dispatchers.IO) {
        val routeResponse = directionsService.getWalkingRoute(key = KEY, routeDirection = routeDirection)
        //Log.i("RDDS", "response is $walkingRoute")

        try {
            if (routeResponse.isSuccessful)
                Log.i("RDDS", "response is ${routeResponse.body()}")
            else {
                launch (Dispatchers.Main) {
                    onError?.invoke("${routeResponse.body()?.returnCode} - ${routeResponse.body()?.returnDesc}")
                }
            }
        } catch (e: HttpException) {
            Log.e("RDDS", "Exception e is ${e.message()}")
        }

        routeResponse.body()
    }
}