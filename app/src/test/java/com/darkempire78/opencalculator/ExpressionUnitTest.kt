package com.darkempire78.opencalculator

import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import java.text.DecimalFormatSymbols

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExpressionUnitTest {

    private val decimalSeparatorSymbol = DecimalFormatSymbols.getInstance().decimalSeparator.toString()
    private val groupingSeparatorSymbol = DecimalFormatSymbols.getInstance().groupingSeparator.toString()
    @Test
    fun percentage_isCorrect() {
        var result = calculate("100*95%", false).toDouble()
        assertEquals(95.0, result, 0.0)

        result = calculate("(10*10)*95%", false).toDouble()
        assertEquals(95.0, result, 0.0)

        result = calculate("100*100%", false).toDouble()
        assertEquals(100.0, result, 0.0)

        result = calculate("100*100/100*100%", false).toDouble()
        assertEquals(100.0, result, 0.0)

        result = calculate("100%10", false).toDouble()
        assertEquals(10.0, result, 0.0)

        result = calculate("10%10%", false).toDouble()
        assertEquals(0.01, result, 0.0)

        result = calculate("900/10%", false).toDouble()
        assertEquals(9000.0, result, 0.0)
    }

    @Test
    fun addition_isCorrect() {
        var result = calculate("1+1", false).toDouble()
        assertEquals(2.0, result, 0.0)

        result = calculate("(1+1)+1", false).toDouble()
        assertEquals(3.0, result, 0.0)
    }

    @Test
    fun pow_isCorrect() {
        var result = calculate("4^3", false).toDouble()
        assertEquals(64.0, result, 0.0)

        result = calculate("5^-5", false).toDouble()
        assertEquals(0.00032, result, 0.0)

        result = calculate("2^3.5", false).toDouble()
        assertEquals(11.31370849898476, result, 0.0000001)

        result = calculate("2^-3.5", false).toDouble()
        assertEquals(0.08838834764, result, 0.0000001)
    }

    @Test
    fun subtraction_isCorrect() {
        var result = calculate("1-1", false).toDouble()
        assertEquals(0.0, result, 0.0)

        result = calculate("1-1-1", false).toDouble()
        assertEquals(-1.0, result, 0.0)
    }

    @Test
    fun division_isCorrect() {
        var result = calculate("0.5/0.01", false).toDouble()
        assertEquals(50.0, result, 0.0)

        result = calculate("7/2", false).toDouble()
        assertEquals(3.5, result, 0.0)
    }

    @Test
    fun factorial_isCorrect() {
        var result = calculate("0!", false).toDouble()
        assertEquals(1.0, result, 0.0)

        result = calculate("5!", false).toDouble()
        assertEquals(120.0, result, 0.0)

        result = calculate("10!", false).toDouble()
        assertEquals(3628800.0, result, 0.0)

        result = calculate("5!+5!", false).toDouble()
        assertEquals(240.0, result, 0.0)

        result = calculate("(3!)!+(3!)!", false).toDouble()
        assertEquals(1440.0, result, 0.0)

        result = calculate("(3!)!/(3!)!", false).toDouble()
        assertEquals(1.0, result, 0.0)
    }

    @Test
    fun sqrt_isCorrect() {
        var result = calculate("√2^2", false).toDouble()
        assertEquals(2.0, result, 0.0)

        result = calculate("√9", false).toDouble()
        assertEquals(3.0, result, 0.0)
    }

    @Test
    fun trigonometric_functions_isCorrect() {
        // In radians
        var result = calculate("cos(0)", false).toDouble()
        assertEquals(1.0, result, 0.0)

        result = calculate("1*cos(0)", false).toDouble()
        assertEquals(1.0, result, 0.0)

        result = calculate("1+cos(0)", false).toDouble()
        assertEquals(2.0, result, 0.0)

        result = calculate("1-cos(0)", false).toDouble()
        assertEquals(0.0, result, 0.0)

        result = calculate("2^cos(0)", false).toDouble()
        assertEquals(2.0, result, 0.0)

        result = calculate("(cos(0))", false).toDouble()
        assertEquals(1.0, result, 0.0)

        result = calculate("cos(2)", false).toDouble()
        assertEquals(-0.4161468365471424, result, 0.0)

        result = calculate("tan(pi/2)", false).toDouble()
        assertEquals(0.0, result, 0.0) // 0.0 means that it is impossible

        result = calculate("tan(45)", true).toDouble()
        assertEquals(0.9999999999999999, result, 0.0)

        result = calculate("sin(220)", true).toDouble()
        assertEquals(-0.6427876096865393, result, 0.0)

        result = calculate("sin(5!)", true).toDouble()
        assertEquals(0.8660254037844387, result, 0.0)

        result = calculate("sin(1+1)", true).toDouble()
        assertEquals(0.03489949670250097, result, 0.0)
    }

    private fun calculate(input: String, isDegreeModeActivated : Boolean) = calculator.evaluate(expression.getCleanExpression(input, decimalSeparatorSymbol, groupingSeparatorSymbol), isDegreeModeActivated)

    companion object {
        private lateinit var expression: Expression
        private lateinit var calculator: Calculator

        @BeforeClass
        @JvmStatic fun setup() {
            expression = Expression()
            calculator = Calculator(10)
        }
    }
}
