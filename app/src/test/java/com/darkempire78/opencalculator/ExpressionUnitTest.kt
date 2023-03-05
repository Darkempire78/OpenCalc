package com.darkempire78.opencalculator

import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExpressionUnitTest {
    @Test
    fun percentage_isCorrect() {
        var result = calculate("100*95%", false)
        assertEquals(95.0, result, 0.0)

        result = calculate("(10*10)*95%", false)
        assertEquals(95.0, result, 0.0)

        result = calculate("100*100%", false)
        assertEquals(100.0, result, 0.0)

        result = calculate("100*100/100*100%", false)
        assertEquals(100.0, result, 0.0)

        result = calculate("100%10", false)
        assertEquals(10.0, result, 0.0)

        result = calculate("10%10%", false)
        assertEquals(0.01, result, 0.0)
    }

    @Test
    fun addition_isCorrect() {
        var result = calculate("1+1", false)
        assertEquals(2.0, result, 0.0)

        result = calculate("(1+1)+1", false)
        assertEquals(3.0, result, 0.0)
    }

    @Test
    fun subtraction_isCorrect() {
        var result = calculate("1-1", false)
        assertEquals(0.0, result, 0.0)

        result = calculate("1-1-1", false)
        assertEquals(-1.0, result, 0.0)
    }

    @Test
    fun factorial_isCorrect() {
        var result = calculate("0!", false)
        assertEquals(1.0, result, 0.0)

        result = calculate("5!", false)
        assertEquals(120.0, result, 0.0)
    }

    @Test
    fun sqrt_isCorrect() {
        var result = calculate("√2^2", false)
        assertEquals(2.0, result, 0.0)

        result = calculate("√9", false)
        assertEquals(3.0, result, 0.0)
    }

    @Test
    fun trigonometric_functions_isCorrect() {
        // In radians
        var result = calculate("cos(0)", false)
        assertEquals(1.0, result, 0.0)

        result = calculate("1*cos(0)", false)
        assertEquals(1.0, result, 0.0)

        result = calculate("1+cos(0)", false)
        assertEquals(2.0, result, 0.0)

        result = calculate("1-cos(0)", false)
        assertEquals(0.0, result, 0.0)

        result = calculate("2^cos(0)", false)
        assertEquals(2.0, result, 0.0)

        result = calculate("(cos(0))", false)
        assertEquals(1.0, result, 0.0)

        result = calculate("cos(2)", false)
        assertEquals(-0.4161468365471424, result, 0.0)

        result = calculate("tan(pi/2)", false)
        assertEquals(Double.NaN, result, 0.0)

        result = calculate("sin(220)", true)
        assertEquals(-0.6427876096865393, result, 0.0)

        //result = calculate("sin(5!)", true)
        //assertEquals(0.866025403784, result, 0.0)

        result = calculate("sin(1+1)", true)
        assertEquals(0.03489949670250097, result, 0.0)


    }

    private fun calculate(input: String, isDegreeModeActivated : Boolean) = calculator.evaluate(expression.getCleanExpression(input), isDegreeModeActivated)

    companion object {
        private lateinit var expression: Expression
        private lateinit var calculator: Calculator

        @BeforeClass
        @JvmStatic fun setup() {
            expression = Expression()
            calculator = Calculator()
        }
    }
}
