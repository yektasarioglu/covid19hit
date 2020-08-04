package com.yektasarioglu.network.repository.datasource

import com.yektasarioglu.network.request.RouteDirection
import com.yektasarioglu.network.response.RouteResponse

interface DirectionOperations {
    suspend fun getBicyclingRoute(routeDirection: RouteDirection, onError: ((String) -> Unit)?): RouteResponse?
    suspend fun getDrivingRoute(routeDirection: RouteDirection, onError: ((String) -> Unit)?): RouteResponse?
    suspend fun getWalkingRoute(routeDirection: RouteDirection, onError: ((String) -> Unit)?): RouteResponse?
}