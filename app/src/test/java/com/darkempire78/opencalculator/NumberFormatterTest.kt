package com.darkempire78.opencalculator

import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test

class NumberFormatterTest {

    @Test
    fun `given a decimal number when formatting then format with grouping separator`() {
        // Given
        val number = "12345"

        // When
        val result = formatter.format(number, decimalSeparator, groupingSeparator)

        // Then
        val expected = "12,345"
        assertEquals(expected, result)
    }

    @Test
    fun `given a floating-point number when formatting then format with grouping separator and decimal separator`() {
        // Given
        val number = "1234.567"

        // When
        val result = formatter.format(number, decimalSeparator, groupingSeparator)

        // Then
        val expected = "1,234.567"
        assertEquals(expected, result)
    }

    @Test
    fun `given a large number when formatting then format with grouping separator`() {
        // Given
        val number = Long.MAX_VALUE.toString() // 9223372036854775807

        // When
        val result = formatter.format(number, decimalSeparator, groupingSeparator)

        // Then
        val expected = "9,223,372,036,854,775,807"
        assertEquals(expected, result)
    }

    @Test
    fun `given an already formatted number when formatting then keep the same format`() {
        // Given
        val number = "123,456"

        // When
        val result = formatter.format(number, decimalSeparator, groupingSeparator)

        // Then
        val expected = "123,456"
        assertEquals(expected, result)
    }

    @Test
    fun `given a number with leading zeros when formatting then keep the leading zeros for decimal`() {
        // Given
        val number = "00100"

        // When
        val result = formatter.format(number, decimalSeparator, groupingSeparator)

        // Then
        val expected = "00,100"
        assertEquals(expected, result)
    }

    @Test
    fun `given a number with leading zeros when formatting then keep the leading zeros for floating-point`() {
        // Given
        val number = "0000.0100"

        // When
        val result = formatter.format(number, decimalSeparator, groupingSeparator)

        // Then
        val expected = "0,000.0100"
        assertEquals(expected, result)
    }

    @Test
    fun `given a negative integer number when formatting then format with grouping separator and negative sign`() {
        // Given
        val number = "-12345"

        // When
        val result = formatter.format(number, decimalSeparator, groupingSeparator)

        // Then
        val expected = "-12,345"
        assertEquals(expected, result)
    }

    @Test
    fun `given a negative floating-point number when formatting then format with grouping separator, decimal separator, and negative sign`() {
        // Given
        val number = "-1234.567"

        // When
        val result = formatter.format(number, decimalSeparator, groupingSeparator)

        // Then
        val expected = "-1,234.567"
        assertEquals(expected, result)
    }

    @Test
    fun `given an empty input when formatting then return empty string`() {
        // Given
        val number = ""

        // When
        val result = formatter.format(number, decimalSeparator, groupingSeparator)

        // Then
        val expected = ""
        assertEquals(expected, result)
    }

    @Test
    fun `given a zero input when formatting then return zero`() {
        // Given
        val number = "0"

        // When
        val result = formatter.format(number, decimalSeparator, groupingSeparator)

        // Then
        val expected = "0"
        assertEquals(expected, result)
    }

    @Test
    fun `given a zero input with decimal separator when formatting then return zero with decimal separator`() {
        // Given
        val number = "0.0"

        // When
        val result = formatter.format(number, decimalSeparator, groupingSeparator)

        // Then
        val expected = "0.0"
        assertEquals(expected, result)
    }

    companion object {
        lateinit var decimalSeparator: String
        lateinit var groupingSeparator: String
        lateinit var formatter: NumberFormatter

        @JvmStatic
        @BeforeClass
        fun setup(): Unit {
            decimalSeparator = "."
            groupingSeparator = ","
            formatter = NumberFormatter
        }
    }
}