package com.yektasarioglu.network.model

data class UserLocation(
    var city: String? = null,
    var country: String? = null,
    var state: String? = null,
    var coordinate: Pair<Double, Double>? = null
)