package com.yektasarioglu.covid19hit.utils

import java.text.DecimalFormat

/**
 * This function formats the distance value based on its value such as;
 * 1200 distance value returns 1,2 KM or 150 distance value return 150 M
 */
fun formatDistance(distance: Double) : String {
    val decimalFormatter = DecimalFormat("#.#")
    var formattedDistanceNumber: String = ""

    val unit = if (distance > 1000) {
        val initialTwoDigit = distance.toString().substring(0, 2)
        formattedDistanceNumber = "${initialTwoDigit[0]},${initialTwoDigit[1]}"
        "KM"
    } else {
        formattedDistanceNumber = "${decimalFormatter.format(distance)}"
        "M"
    }

    return "$formattedDistanceNumber $unit"
}
