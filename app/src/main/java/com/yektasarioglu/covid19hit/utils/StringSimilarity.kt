package com.yektasarioglu.covid19hit.utils

import android.util.Log

/**
 * Hallelujah to the guy who posted below :)
 * https://stackoverflow.com/a/16018452/8832537
 */

object StringSimilarity {

    /**
     * Calculates the similarity (a number within 0 and 1) between two strings.
     */
    fun similarity(s1: String, s2: String): Double {
        var longer = s1
        var shorter = s2
        if (s1.length < s2.length) { // longer should always have greater length
            longer = s2
            shorter = s1
        }
        val longerLength = longer.length
        return if (longerLength == 0) {
            1.0 /* both strings are zero length */
        } else (longerLength - editDistance(longer, shorter)) / longerLength.toDouble()
        /* // If you have Apache Commons Text, you can use it to calculate the edit distance:
    LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
    return (longerLength - levenshteinDistance.apply(longer, shorter)) / (double) longerLength; */
    }

    // Example implementation of the Levenshtein Edit Distance
    // See http://rosettacode.org/wiki/Levenshtein_distance#Java
    fun editDistance(s1: String, s2: String): Int {
        var s1 = s1
        var s2 = s2
        s1 = s1.toLowerCase()
        s2 = s2.toLowerCase()
        val costs = IntArray(s2.length + 1)
        for (i in 0..s1.length) {
            var lastValue = i
            for (j in 0..s2.length) {
                if (i == 0) costs[j] = j else {
                    if (j > 0) {
                        var newValue = costs[j - 1]
                        if (s1[i - 1] != s2[j - 1]) newValue = Math.min(
                            Math.min(newValue, lastValue),
                            costs[j]
                        ) + 1
                        costs[j - 1] = lastValue
                        lastValue = newValue
                    }
                }
            }
            if (i > 0) costs[s2.length] = lastValue
        }
        return costs[s2.length]
    }

    fun printSimilarity(s: String, t: String) {
        //println(String.format("%.3f is the similarity between \"%s\" and \"%s\"", similarity(s, t), s, t))

        Log.i("StringSimilarity", "%${similarity(s, t)} is the similarity between $s and $t")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        printSimilarity("", "")
        printSimilarity("1234567890", "1")
        printSimilarity("1234567890", "123")
        printSimilarity("1234567890", "1234567")
        printSimilarity("1234567890", "1234567890")
        printSimilarity("1234567890", "1234567980")
        printSimilarity("47/2010", "472010")
        printSimilarity("47/2010", "472011")
        printSimilarity("47/2010", "AB.CDEF")
        printSimilarity("47/2010", "4B.CDEFG")
        printSimilarity("47/2010", "AB.CDEFG")
        printSimilarity("The quick fox jumped", "The fox jumped")
        printSimilarity("The quick fox jumped", "The fox")
        printSimilarity("kitten", "sitting")
    }
}