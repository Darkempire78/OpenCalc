package com.darkempire78.opencalculator

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

object NumberFormatter {
    fun format(text: String, decimalSeparatorSymbol : String, groupingSeparatorSymbol : String): String {
        val textNoSeparator = removeSeparators(text, groupingSeparatorSymbol)
        val numbersList = extractNumbers(textNoSeparator, decimalSeparatorSymbol)
        val numbersWithSeparators = addSeparators(numbersList, decimalSeparatorSymbol)
        var textWithSeparators = textNoSeparator
        numbersList.forEachIndexed { index, number ->
            textWithSeparators = textWithSeparators.replace(number, numbersWithSeparators[index])
        }
        return textWithSeparators
    }

    fun extractNumbers(text: String, decimalSeparatorSymbol : String): List<String> {
        val numberRegex = "(\\d+\\$decimalSeparatorSymbol\\d+)|(\\d+\\$decimalSeparatorSymbol)|(\\$decimalSeparatorSymbol\\d+)|(\\$decimalSeparatorSymbol)|(\\d+)".toRegex()

        val results = numberRegex.findAll(text)
        return results.map { it.value }.toList()
    }

    private fun addSeparators(numbersList: List<String>, decimalSeparatorSymbol : String): List<String> {
        return numbersList.map {
            if (it.contains(decimalSeparatorSymbol)) {
                if (it.first() == decimalSeparatorSymbol[0]) {
                    //this means the floating point number doesn't have integers
                    it
                } else {
                    val integersPart = it.substring(0, it.indexOf(decimalSeparatorSymbol))
                    val fractions = it.substring(it.indexOf(decimalSeparatorSymbol) + 1)
                    DecimalFormat().format(integersPart.toBigDecimal()) + decimalSeparatorSymbol + fractions
                }
            } else {
                DecimalFormat().format(it.toBigDecimal())
            }
        }
    }

    private fun removeSeparators(text: String, groupingSeparatorSymbol: String): String {
        return text.replace(groupingSeparatorSymbol, "")
    }
}
