package com.yektasarioglu.network.response

import com.google.gson.annotations.SerializedName

data class RouteResponse(
    @SerializedName("returnCode")
    val returnCode: String,
    @SerializedName("returnDesc")
    val returnDesc: String,
    @SerializedName("routes")
    val routes: List<Route>
)