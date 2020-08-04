package com.yektasarioglu.network.response


import com.google.gson.annotations.SerializedName

data class Path(
    @SerializedName("distance")
    val distance: Double,
    @SerializedName("distanceText")
    val distanceText: String,
    @SerializedName("duration")
    val duration: Double,
    @SerializedName("durationInTraffic")
    val durationInTraffic: Double,
    @SerializedName("durationInTrafficText")
    val durationInTrafficText: String,
    @SerializedName("durationText")
    val durationText: String,
    @SerializedName("endAddress")
    val endAddress: String,
    @SerializedName("endLocation")
    val endLocation: EndLocation,
    @SerializedName("startAddress")
    val startAddress: String,
    @SerializedName("startLocation")
    val startLocation: StartLocation,
    @SerializedName("steps")
    val steps: List<Step>
)