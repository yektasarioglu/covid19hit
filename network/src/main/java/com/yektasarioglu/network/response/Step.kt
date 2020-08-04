package com.yektasarioglu.network.response


import com.google.gson.annotations.SerializedName

data class Step(
    @SerializedName("action")
    val action: String,
    @SerializedName("distance")
    val distance: Double,
    @SerializedName("distanceText")
    val distanceText: String,
    @SerializedName("duration")
    val duration: Double,
    @SerializedName("durationText")
    val durationText: String,
    @SerializedName("endLocation")
    val endLocation: EndLocationX,
    @SerializedName("instruction")
    val instruction: String,
    @SerializedName("orientation")
    val orientation: Int,
    @SerializedName("polyline")
    val polyline: List<Polyline>,
    @SerializedName("roadName")
    val roadName: String,
    @SerializedName("startLocation")
    val startLocation: StartLocationX
) {

    val latitudeRange: LongRange?
        get() = LongRange(
            start = startLocation.lat.toLong(),
            endInclusive = endLocation.lat.toLong()
        )

    val longitudeRange: LongRange?
        get() = LongRange(
            start = startLocation.lng.toLong(),
            endInclusive = endLocation.lng.toLong()
        )

}