package com.darkempire78.opencalculator

import java.math.BigInteger
import kotlin.math.*

class Calculator {

    fun factorial(number: Double): Double {
        val decimalPartOfNumber = number - number.toInt()
        if (decimalPartOfNumber == 0.0) {
            var factorial = BigInteger("1")
            for (i in 1..number.toInt()) {
                factorial *= i.toBigInteger()
            }
            return factorial.toDouble()
        } else {
            return gammaLanczos(number+1)
        }
    }

    private fun gammaLanczos(x: Double): Double {
        // https://rosettacode.org/wiki/Gamma_function
        var xx = x
        val p = doubleArrayOf(
            0.99999999999980993,
            676.5203681218851,
            -1259.1392167224028,
            771.32342877765313,
            -176.61502916214059,
            12.507343278686905,
            -0.13857109526572012,
            9.9843695780195716e-6,
            1.5056327351493116e-7
        )
        val g = 7
        if (xx < 0.5) return Math.PI / (sin(Math.PI * xx) * gammaLanczos(1.0 - xx))
        xx--
        var a = p[0]
        val t = xx + g + 0.5
        for (i in 1 until p.size) a += p[i] / (xx + i)
        return sqrt(2.0 * Math.PI) * t.pow(xx + 0.5) * exp(-t) * a
    }

    fun evaluate(equation: String, isDegreeModeActivated: Boolean): Double {
        println("\n\n$equation")
        // https://stackoverflow.com/questions/3422673/how-to-evaluate-a-math-expression-given-in-string-form
        return object : Any() {
            var pos = -1
            var ch = 0
            fun nextChar() {
                ch = if (++pos < equation.length) equation[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < equation.length) println("Unexpected: " + ch.toChar() + "Expressoion: " + equation)
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm() // addition
                    else if (eat('-'.code)) x -= parseTerm() // subtraction
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor() // multiplication
                    else if (eat('/'.code)) x /= parseFactor() // division
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return +parseFactor() // unary plus
                if (eat('-'.code)) return -parseFactor() // unary minus
                var x: Double
                val startPos = pos
                if (eat('('.code)) { // parentheses
                    x = parseExpression()
                    if (!eat(')'.code)) throw RuntimeException("Missing ')'")
                } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { // numbers
                    while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                    val string = equation.substring(startPos, pos)
                    x = if (string.count { it == '.' } > 1) {
                        Double.NaN
                    } else {
                        string.toDouble()
                    }
                } else if (eat('e'.code)) {
                    x = exp(1.0)
                } else if (eat('π'.code)) {
                        x = Math.PI
                } else if (ch >= 'a'.code && ch <= 'z'.code) { // functions
                    while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                    val func: String = equation.substring(startPos, pos)
                    if (eat('('.code)) {
                        x = parseExpression()
                        if (!eat(')'.code)) x = parseFactor()
                    } else {
                        x = parseFactor()
                    }
                    when (func) {
                        "sqrt" -> x = sqrt(x)
                        "ln" -> x = ln(x)
                        "logten" -> x = log10(x)
                        "exp" -> x = exp(x)
                        "factorial" -> x = factorial(x)
                        "sin" -> if (isDegreeModeActivated) {
                            x = sin(Math.toRadians(x))
                            // https://stackoverflow.com/questions/29516222/how-to-get-exact-value-of-trigonometric-functions-in-java
                            if (x < 1.0E-14) {
                                x = round(x)
                            }
                        } else {
                            x = sin(x)
                            if (x < 1.0E-14) {
                                x = round(x)
                            }
                        }
                        "cos" -> if (isDegreeModeActivated) {
                            x = cos(Math.toRadians(x))
                            if (x < 1.0E-14) {
                                x = round(x)
                            }
                        } else {
                            x = cos(x)
                            if (x < 1.0E-14) {
                                x = round(x)
                            }
                        }
                        "tan" -> if (isDegreeModeActivated) {
                            x = tan(Math.toRadians(x))
                            if (x < 1.0E-14) {
                                x = round(x)
                            }
                        } else {
                            x = tan(x)
                            if (x < 1.0E-14) {
                                x = round(x)
                            }
                        }
                        "arcsin" -> if (isDegreeModeActivated) {
                            x = asin(Math.toRadians(x))
                            if (x < 1.0E-14) {
                                x = round(x)
                            }
                        } else {
                            x = asin(x)
                            if (x < 1.0E-14) {
                                x = round(x)
                            }
                        }
                        "arccos" -> if (isDegreeModeActivated) {
                            x = acos(Math.toRadians(x))
                            if (x < 1.0E-14) {
                                x = round(x)
                            }
                        } else {
                            x = acos(x)
                            if (x < 1.0E-14) {
                                x = round(x)
                            }
                        }
                        "arctan" -> if (isDegreeModeActivated) {
                            x = atan(Math.toRadians(x))
                            if (x < 1.0E-14) {
                                x = round(x)
                            }
                        } else {
                            x = atan(x)
                            if (x < 1.0E-14) {
                                x = round(x)
                            }
                        }
                        else -> x = Double.NaN
                    }
                } else {
                    x = Double.NaN
                }
                if (eat('^'.code)) x = x.pow(parseFactor()) // exponentiation
                return x
            }
        }.parse()
    }
}