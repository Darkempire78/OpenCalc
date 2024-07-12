package com.darkempire78.opencalculator

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

class DecimalToFraction {
    // Recursive function to return GCD of a and b
    private fun gcd(_a: BigInteger, _b: BigInteger): BigInteger {
        val a = _a.abs()
        val b = _b.abs()
        if (a == BigInteger.ZERO) return b
        else if (b == BigInteger.ZERO) return a
        return if (a < b) gcd(a, b % a)
        else gcd(b, a % b)
    }

    fun approximateRational(value: BigDecimal, tolerance: BigDecimal = BigDecimal("1E-10")): Pair<BigInteger, BigInteger> {
        var r0 = value
        var r1 = BigDecimal.ONE
        var a0 = r0.toBigInteger()
        var a1 = BigInteger.ONE
        var b0 = BigInteger.ONE
        var b1 = BigInteger.ZERO

        var r = r0 - a0.toBigDecimal()
        var n = BigInteger.ONE

        while (r.abs() > tolerance) {
            r = BigDecimal.ONE.divide(r, MathContext.DECIMAL128)
            val ai = r.toBigInteger()

            val newA = ai.multiply(a0).add(a1)
            val newB = ai.multiply(b0).add(b1)

            a1 = a0
            b1 = b0
            a0 = newA
            b0 = newB

            r = r.subtract(ai.toBigDecimal())

            n++
        }

        return Pair(a0, b0)
    }

    fun needsApproximation(value: BigDecimal, significantFigures : Int): Boolean {
        val roundedValue = value.round(MathContext(significantFigures, RoundingMode.HALF_UP))
        val difference = value.subtract(roundedValue).abs()
        val tolerance = BigDecimal("1E-${significantFigures+1}")

        return difference > tolerance
    }

    fun getFraction(number: BigDecimal): String {
        var intVal = number.toBigInteger() // Fetch integral value of the decimal
        val fVal = number - BigDecimal(intVal) // Fetch fractional part of the decimal

        if (fVal.compareTo(BigDecimal.ZERO) == 0) {
            return ""
        }

        // Consider precision value to convert fractional part to integral equivalent
        val scale = number.scale()
        val pVal = BigDecimal.TEN.pow(scale)
        //val pVal = BigDecimal(10000000000000)

        // Check if the number needs approximation to be converted to a fraction
        // Example: 1.33333...3 -> 4/3
        if (needsApproximation(fVal, 10)) {
            val (num, den) = approximateRational(fVal)
            val isNgeative = if (intVal < BigInteger.ZERO) "-" else ""
            intVal = intVal.abs()
            return if (intVal == BigInteger.ZERO) {
                "$isNgeative<small><sup>$num</sup>/<sub>$den</sub></small>"
            } else {
                "$isNgeative$intVal <small><sup>$num</sup>/<sub>$den</sub></small>"
            }
        }

        // Calculate GCD of integral equivalent of fractional part and precision value
        val gcdVal = gcd((fVal * pVal).toBigInteger(), pVal.toBigInteger())

        // Calculate num and deno
        val num = ((fVal * pVal / gcdVal.toBigDecimal()).toBigInteger()).abs()
        val deno = ((pVal / gcdVal.toBigDecimal()).toBigInteger()).abs()

        val isNgeative = if (intVal < BigInteger.ZERO) "-" else ""
        intVal = intVal.abs()

        return if (intVal == BigInteger.ZERO) {
            //"$isNgeative    <span style=\"size:$size+\">$text</span><sup>$num</sup>/<sub>$deno</sub>"
            "$isNgeative<small><sup>$num</sup>/<sub>$deno</sub></small>"
        } else {
            "$isNgeative$intVal <small><sup>$num</sup>/<sub>$deno</sub></small>"
        }
    }

    // Create a function that

}