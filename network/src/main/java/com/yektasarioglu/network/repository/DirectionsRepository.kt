package com.yektasarioglu.network.repository

import com.yektasarioglu.network.repository.datasource.DirectionOperations
import com.yektasarioglu.network.repository.datasource.RemoteDirectionDataSource
import com.yektasarioglu.network.request.RouteDirection
import com.yektasarioglu.network.response.RouteResponse

class DirectionsRepository(
    private val remoteDirectionsDataSource: RemoteDirectionDataSource
) : DirectionOperations
{
    override suspend fun getBicyclingRoute(routeDirection: RouteDirection,  onError: ((String) -> Unit)?) : RouteResponse? =
        remoteDirectionsDataSource.getBicyclingRoute(routeDirection, onError)

    override suspend fun getDrivingRoute(routeDirection: RouteDirection, onError: ((String) -> Unit)?): RouteResponse? =
        remoteDirectionsDataSource.getDrivingRoute(routeDirection, onError)

    override suspend fun getWalkingRoute(routeDirection: RouteDirection, onError: ((String) -> Unit)?): RouteResponse? =
        remoteDirectionsDataSource.getWalkingRoute(routeDirection, onError)
}