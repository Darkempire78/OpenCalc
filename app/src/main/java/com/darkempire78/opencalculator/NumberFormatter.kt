package com.darkempire78.opencalculator


import android.util.Log
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

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
            val decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator.toString()
            if (it.value.contains(decimalSeparator))
                it.value.toDouble()
            else
                it.value.toLong()
        }.toList()
        return resultsList
    }

    private fun addSeparators(numbersList: List<Number>): List<String> {
        numbersList.map { DecimalFormat().format(it) }
        Log.i(TAG, "SEP COMA : "+ DecimalFormatSymbols.getInstance().decimalSeparator.toString())
        Log.i(TAG, "SEP MILL : "+ DecimalFormatSymbols.getInstance().groupingSeparator.toString())
        return numbersList.map { DecimalFormat().format(it) }
    }

    private fun removeSeparators(text: String): String {
        val groupingSeparator = DecimalFormatSymbols.getInstance().groupingSeparator.toString()
        return if (text.contains(groupingSeparator)) {
            text.replace(groupingSeparator, "")
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
