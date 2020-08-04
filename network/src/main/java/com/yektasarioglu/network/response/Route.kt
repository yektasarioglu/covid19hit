package com.yektasarioglu.network.response


import com.google.gson.annotations.SerializedName

data class Route(
    @SerializedName("bounds")
    val bounds: Bounds,
    @SerializedName("paths")
    val paths: List<Path>
)