package com.darkempire78.opencalculator

import java.math.BigDecimal
import java.math.BigInteger

class DecimalToFraction {
    // Recursive function to return GCD of a and b
    private fun gcd(a: BigInteger, b: BigInteger): BigInteger {
        if (a == BigInteger.ZERO) return b
        else if (b == BigInteger.ZERO) return a
        if (a < b) return gcd(a, b % a)
        else return gcd(b, a % b)
    }

    // Function to convert decimal to fraction
    fun getFraction(number: BigDecimal): String {
        // Fetch integral value of the decimal
        val intVal = number.toBigInteger()

        // Fetch fractional part of the decimal
        val fVal = number - BigDecimal(intVal)

        // Consider precision value to convert fractional part to integral equivalent
        val pVal = BigDecimal(1000000000)

        // Calculate GCD of integral equivalent of fractional part and precision value
        val gcdVal = gcd((fVal * pVal).toBigInteger(), pVal.toBigInteger())

        // Calculate num and deno
        val num = (fVal * pVal / gcdVal.toBigDecimal()).toBigInteger()
        val deno = (pVal / gcdVal.toBigDecimal()).toBigInteger()

        // return the fraction
        //println("${intVal * deno + num}/$deno")
        return "${intVal * deno + num}/$deno"
    }
}