package com.darkempire78.opencalculator

import java.math.BigDecimal
import java.math.BigInteger

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

        // Calculate GCD of integral equivalent of fractional part and precision value
        val gcdVal = gcd((fVal * pVal).toBigInteger(), pVal.toBigInteger())

        // Calculate num and deno
        val num = ((fVal * pVal / gcdVal.toBigDecimal()).toBigInteger()).abs()
        val deno = ((pVal / gcdVal.toBigDecimal()).toBigInteger()).abs()

        val isNgeative = if (intVal < BigInteger.ZERO) "-" else ""
        intVal = intVal.abs()

        println("Equation Fraction format : $intVal * $num/$deno")

        return if (intVal == BigInteger.ZERO) {
            //"$isNgeative    <span style=\"size:$size+\">$text</span><sup>$num</sup>/<sub>$deno</sub>"
            "$isNgeative<small><sup>$num</sup>/<sub>$deno</sub></small>"
        } else {
            "$isNgeative$intVal <small><sup>$num</sup>/<sub>$deno</sub></small>"
        }
    }

}