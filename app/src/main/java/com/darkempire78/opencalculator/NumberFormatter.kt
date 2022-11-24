package com.darkempire78.opencalculator

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

object NumberFormatter {
    val decimalSeparatorSymbol = DecimalFormatSymbols.getInstance().decimalSeparator.toString()
    val groupingSeparatorSymbol = DecimalFormatSymbols.getInstance().groupingSeparator.toString()
    private val numberRegex = "([0-9]+\\$decimalSeparatorSymbol[0-9]+)|([0-9]\\$decimalSeparatorSymbol)|(\\$decimalSeparatorSymbol[0-9]+)|(\\$decimalSeparatorSymbol)|([0-9]+)".toRegex()

    fun format(text: String): String {
        val textNoSeparator = removeSeparators(text)
        val numbersList = extractNumbers(textNoSeparator)
        val numbersWithSeparators = addSeparators(numbersList)
        var textWithSeparators = textNoSeparator
        numbersList.forEachIndexed { index, number ->
            textWithSeparators = textWithSeparators.replace(number, numbersWithSeparators[index])
        }
        return textWithSeparators
    }

    fun extractNumbers(text: String): List<String> {
        val results = numberRegex.findAll(text)
        return results.map { it.value }.toList()
    }

    private fun addSeparators(numbersList: List<String>): List<String> {
        return numbersList.map {
            if (it.contains(decimalSeparatorSymbol)) {
                var tmp = it
                if (it[0].toString() == decimalSeparatorSymbol) {
                    tmp = "0" + tmp
                }
                val integersPart = tmp.substring(0, tmp.indexOf(decimalSeparatorSymbol))
                val fractions = tmp.substring(tmp.indexOf(decimalSeparatorSymbol) + 1)
                DecimalFormat().format(integersPart.toBigDecimal()) + decimalSeparatorSymbol + fractions
            } else {
                DecimalFormat().format(it.toBigDecimal())
            }
        }
    }

    private fun removeSeparators(text: String): String {
        return text.replace(groupingSeparatorSymbol, "")
    }
}
