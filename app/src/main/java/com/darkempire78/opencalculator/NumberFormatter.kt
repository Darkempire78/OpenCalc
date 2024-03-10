package com.darkempire78.opencalculator

object NumberFormatter {
    fun format(
        text: String,
        decimalSeparatorSymbol: String,
        groupingSeparatorSymbol: String
    ): String {
        val textNoSeparator = removeSeparators(text, groupingSeparatorSymbol)
        val numbersList = extractNumbers(textNoSeparator, decimalSeparatorSymbol)
        val numbersWithSeparators =
            addSeparators(numbersList, decimalSeparatorSymbol, groupingSeparatorSymbol)
        var textWithSeparators = textNoSeparator
        numbersList.forEachIndexed { index, number ->
            textWithSeparators = textWithSeparators.replace(number, numbersWithSeparators[index])
        }
        return textWithSeparators
    }

    fun extractNumbers(text: String, decimalSeparatorSymbol: String): List<String> {
        val numberRegex =
            "(\\d+\\$decimalSeparatorSymbol\\d+)|(\\d+\\$decimalSeparatorSymbol)|(\\$decimalSeparatorSymbol\\d+)|(\\$decimalSeparatorSymbol)|(\\d+)".toRegex()

        val results = numberRegex.findAll(text)
        return results.map { it.value }.toList()
    }

    private fun addSeparators(
        numbersList: List<String>,
        decimalSeparatorSymbol: String,
        groupingSeparatorSymbol: String
    ): List<String> {
        return numbersList.map {
            if (it.contains(decimalSeparatorSymbol)) {
                if (it.first() == decimalSeparatorSymbol[0]) {
                    //this means the floating point number doesn't have integers
                    it
                } else {
                    val integersPart = it.substring(0, it.indexOf(decimalSeparatorSymbol))
                    val fractions = it.substring(it.indexOf(decimalSeparatorSymbol) + 1)
                    formatIntegers(integersPart, groupingSeparatorSymbol) + decimalSeparatorSymbol + fractions
                }
            } else {
                formatIntegers(it, groupingSeparatorSymbol)
            }
        }
    }

    private fun formatIntegers(integers: String, groupingSeparatorSymbol: String): String {
                                                    // sample input  : 00110
        return integers.reversed()                  // reversed      : 01100
            .chunked(3)                        // chunked       : [011, 00]
            .joinToString(groupingSeparatorSymbol)  // joinedToString: 011,00
            .reversed()                             // reversed      : 00,110
    }

    private fun removeSeparators(text: String, groupingSeparatorSymbol: String): String {
        return text.replace(groupingSeparatorSymbol, "")
    }
}
