package com.darkempire78.opencalculator


import java.text.DecimalFormat

object NumberFormatter {
    val TAG = "Mah "

    val numberRegex = Regex("([0-9]+\\.[0-9]+)|([0-9]+)")

    fun format(text: String): String {
        val textNoSeparator = removeSeparators(text)
        val numbersList = extractNumbers(textNoSeparator)

        val numbersWithSeparators = addSeparators(numbersList)

        var textWithSeparators = textNoSeparator
        numbersList.forEachIndexed { index, number ->
            textWithSeparators = textWithSeparators.replace("$number", numbersWithSeparators[index])
        }

        return textWithSeparators
    }

    fun extractNumbers(text: String): List<Number> {
        val results = numberRegex.findAll(text)
        val resultsList: List<Number> = results.map {
            if (it.value.contains("."))
                it.value.toDouble()
            else
                it.value.toLong()
        }.toList()
        return resultsList
    }

    private fun addSeparators(numbersList: List<Number>): List<String> {
        numbersList.map { DecimalFormat().format(it) }
        return numbersList.map { DecimalFormat().format(it) }
    }

    private fun removeSeparators(text: String): String {
        return if (text.contains(",")) {
            text.replace(",", "")
        } else {
            text
        }
    }
}


/**
double Number Regex = [0-9]+\\.[0-9]+

long Number Regex  = ([0-9]+)

 */
/*    fun extractNumberss(text: String) :List<String> {
    val textNoSeparator = removeSeparators(text)
    val results = numberRegex.findAll(textNoSeparator)

        val resultsList:List<String> = results.map {
            if (it.value.contains("."))
                addSeparators(it.value.toDouble())
            else
                addSeparators(it.value.toLong())
        }.toList()
        return resultsList
    }*/
