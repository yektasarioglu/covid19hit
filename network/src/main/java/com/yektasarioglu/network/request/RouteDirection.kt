package com.yektasarioglu.network.request

import com.google.gson.annotations.SerializedName

data class RouteDirection(
    @SerializedName("destination")
    val destination: Coordinates,
    @SerializedName("origin")
    val origin: Coordinates
)