package com.yektasarioglu.network.response


import com.google.gson.annotations.SerializedName

data class StartLocation(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double
)