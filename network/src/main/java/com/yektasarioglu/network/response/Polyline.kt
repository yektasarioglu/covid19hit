package com.yektasarioglu.network.response


import com.google.gson.annotations.SerializedName

data class Polyline(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double
)