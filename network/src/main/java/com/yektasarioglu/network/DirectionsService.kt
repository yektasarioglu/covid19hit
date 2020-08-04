package com.yektasarioglu.network

import com.yektasarioglu.network.request.RouteDirection
import com.yektasarioglu.network.response.RouteResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface DirectionsService {

    @POST("v1/routeService/bicycling")
    suspend fun getBicyclingRoute(
        @Query("key") key: String,
        @Body routeDirection: RouteDirection
    ) : Response<RouteResponse>

    @POST("v1/routeService/driving")
    suspend fun getDrivingRoute(
        @Query("key") key: String,
        @Body routeDirection: RouteDirection
    ) : Response<RouteResponse>

    @POST("v1/routeService/walking")
    suspend fun getWalkingRoute(
        @Query("key") key: String,
        @Body routeDirection: RouteDirection
    ) : Response<RouteResponse>

}