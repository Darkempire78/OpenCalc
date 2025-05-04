package com.darkempire78.opencalculator.calculator.parser

object NumberFormatter {
    fun format(
        text: String,
        decimalSeparatorSymbol: String,
        groupingSeparatorSymbol: String,
        numberingSystem: NumberingSystem = NumberingSystem.INTERNATIONAL
    ): String {
        val textNoSeparator = removeSeparators(text, groupingSeparatorSymbol)
        val numbersList = extractString(textNoSeparator, decimalSeparatorSymbol)
        val numbersWithSeparators =
            addSeparators(numbersList, decimalSeparatorSymbol, groupingSeparatorSymbol, numberingSystem)

        val newString = StringBuilder()

        for (item in numbersWithSeparators) {
            newString.append(item)
        }

        return newString.toString()
    }

    // This function was changed to extract all elements from the input string, not just numbers.
    // This returns a list of all elements. Once the numbers have had separators added, a new string
    // can be constructed from the list.
    private fun extractString(text: String, decimalSeparatorSymbol: String): List<String> {
        val result = mutableListOf<String>()
        var currentNumber = StringBuilder()

        for (char in text) {
            when {
                char.isDigit() || char == decimalSeparatorSymbol.single() -> {
                    currentNumber.append(char)
                }
                else -> {
                    if (currentNumber.isNotEmpty()) {
                        result.add(currentNumber.toString())
                        currentNumber = StringBuilder()
                    }
                    result.add(char.toString())
                }
            }
        }

        if (currentNumber.isNotEmpty()) {
            result.add(currentNumber.toString())
        }

        return result
    }

    private fun addSeparators(
        numbersList: List<String>,
        decimalSeparatorSymbol: String,
        groupingSeparatorSymbol: String,
        numberingSystem: NumberingSystem
    ): List<String> {
        return numbersList.map {
            if (it.contains(decimalSeparatorSymbol)) {
                if (it.first() == decimalSeparatorSymbol[0]) {
                    //this means the floating point number doesn't have integers
                    it
                } else {
                    val integersPart = it.substring(0, it.indexOf(decimalSeparatorSymbol))
                    val fractions = it.substring(it.indexOf(decimalSeparatorSymbol) + 1)
                    formatIntegers(
                        integersPart,
                        groupingSeparatorSymbol,
                        numberingSystem == NumberingSystem.INTERNATIONAL
                    ) + decimalSeparatorSymbol + fractions
                }
            } else {
                formatIntegers(it, groupingSeparatorSymbol, numberingSystem == NumberingSystem.INTERNATIONAL)
            }
        }
    }

    private fun formatIntegers(
        integers: String,
        groupingSeparatorSymbol: String,
        isInternational: Boolean
    ): String {
        // sample input  : 00110
        return if (isInternational) {
            integers.reversed()                         // reversed      : 01100
                .chunked(3)                             // chunked       : [011, 00]
                .joinToString(groupingSeparatorSymbol)  // joinedToString: 011,00
                .reversed()                             // reversed      : 00,110
        } else {
            return formatIndianNumberingSystem(integers)
        }
    }

    private fun removeSeparators(text: String, groupingSeparatorSymbol: String): String {
        return text.replace(groupingSeparatorSymbol, "")
    }

    private fun formatIndianNumberingSystem(numberStr: String): String {
        val isNegative = numberStr.startsWith("-")
        val numberWithoutSign = if (isNegative) numberStr.substring(1) else numberStr

        val numberParts = numberWithoutSign.split(".")
        val integerPart = numberParts[0]
        val decimalPart = if (numberParts.size > 1) numberParts[1] else ""

        val length = integerPart.length
        val result = StringBuilder()
        var count = 0

        for (i in length - 1 downTo 0) {
            result.append(integerPart[i])
            count++

            when {
                /** First comma comes after 3 digits **/
                count == 3 && i != 0 -> {
                    result.append(',')
                    count = 0
                }
                /** Subsequent commas every 2 digits **/
                count == 2 && i != 0 && length - i > 3 -> {
                    result.append(',')
                    count = 0
                }
            }
        }

        val formattedIntegerPart = result.reverse().toString()
        val formattedNumber = if (decimalPart.isNotEmpty()) "$formattedIntegerPart.$decimalPart" else formattedIntegerPart
        return if (isNegative) "-$formattedNumber" else formattedNumber
    }
}

enum class NumberingSystem(val value: Int, val description: String) {
    INTERNATIONAL(0, "International Numbering System"),
    INDIAN(1, "Indian Numbering System");

    companion object {
        fun getDescription(value: Int): String {
            return when (value) {
                0 -> INTERNATIONAL.description
                1 -> INDIAN.description
                else -> INTERNATIONAL.description
            }
        }

        fun Int.toNumberingSystem() : NumberingSystem {
            return when (this) {
                0 -> INTERNATIONAL
                1 -> INDIAN
                else -> INTERNATIONAL
            }
        }
    }
}
