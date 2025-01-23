package com.darkempire78.opencalculator.calculator

import com.darkempire78.opencalculator.MyPreferences
import kotlin.math.abs
import kotlin.math.floor

fun decimalToFraction(decimal: Double, precision: Double): String {
    // val precision = 1.0E-4
    var n1 = 1
    var n2 = 0
    var d1 = 0
    var d2 = 1
    var b = decimal
    val base = floor(b).toInt()
    var dec = decimal - base.toDouble()
    val dec2 = dec
    do {
        val a = floor(dec).toInt()
        val numer = n1
        val denom = d1
        n1 = a * n1 + n2
        n2 = numer
        d1 = a * d1 + d2
        d2 = denom
        dec = 1 / (dec - a)
    } while (abs(dec2 - n1.toDouble() / d1) > dec2 * precision)
    if (base != 0) {
        return "$base $n1/$d1"
    }
    if (n1 > d1) {
        val whole = n1 / d1
        n1 -= whole * d1
        if (n1 == 0) {
            return "$whole"
        }
        return "$whole $n1/$d1"
    }
    return "$n1/$d1"
}