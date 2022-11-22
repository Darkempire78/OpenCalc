package com.darkempire78.opencalculator

class Expression {

    fun getCleanExpression(calculation: String): String {
        var cleanCalculation = replaceSymbolsFromCalculation(calculation)
        cleanCalculation = addMultiply(cleanCalculation)
        if (cleanCalculation.contains('√')) {
            cleanCalculation = formatSquare(cleanCalculation)
        }
        if (cleanCalculation.contains('!')) {
            cleanCalculation = formatFactorial(cleanCalculation)
        }
        if (cleanCalculation.contains('%')) {
            cleanCalculation = getPercentString(cleanCalculation)
            cleanCalculation = cleanCalculation.replace("%", "/100")
        }
        cleanCalculation = addParenthesis(cleanCalculation)

        return cleanCalculation
    }

    private fun replaceSymbolsFromCalculation(calculation: String): String {
        var calculation2 = calculation.replace('×', '*')
        calculation2 = calculation2.replace('÷', '/')
        calculation2 = calculation2.replace("log", "logten")
        calculation2 = calculation2.replace("E", "*10^")
        calculation2 = calculation2.replace(NumberFormatter.groupingSeparatorSymbol, "")
        calculation2 = calculation2.replace(NumberFormatter.decimalSeparatorSymbol, ".")
        return calculation2
    }

    /* Transform any calculation string containing %
     * to respect the user friend (non-mathematical) way (see issue #35)
     */
    private fun getPercentString(calculation: String): String {
        val percentPos = calculation.lastIndexOf('%')
        if (percentPos < 1) {
            return calculation
        }
        // find the last operator before the last %
        val operatorBeforePercentPos = calculation.subSequence(0, percentPos - 1).lastIndexOfAny(charArrayOf('-', '+', '*', '/', '('))
        if (operatorBeforePercentPos < 1) {
            return calculation
        }
        // extract the first part of the calculation
        var calculationStringFirst = calculation.subSequence(0, operatorBeforePercentPos).toString()

        // recursively parse it
        if (calculationStringFirst.contains('%')) {
            calculationStringFirst = getPercentString(calculationStringFirst)
        }
        // check if there are already some parenthesis, so we skip formatting
        if (calculation[operatorBeforePercentPos] == '(') {
            return calculationStringFirst + calculation.subSequence(operatorBeforePercentPos, calculation.length)
        }
        calculationStringFirst = "($calculationStringFirst)"

        // modify the calculation to have something like (X)+(Y%*X)
        return calculationStringFirst + calculation[operatorBeforePercentPos] + calculationStringFirst + "*(" + calculation.subSequence(operatorBeforePercentPos + 1, percentPos) + ")" + calculation.subSequence(percentPos, calculation.length)

    }

    private fun addParenthesis(calculation: String): String {
        // Add ")" which lack
        var cleanCalculation = calculation
        var openParentheses = 0
        var closeParentheses = 0

        for (i in calculation.indices) {
            if (calculation[i] == '(') {
                openParentheses += 1
            }
            if (calculation[i] == ')') {
                closeParentheses += 1
            }
        }
        if (closeParentheses < openParentheses) {
            for (i in 0 until openParentheses - closeParentheses) {
                cleanCalculation += ')'
            }
        }

        return cleanCalculation
    }

    private fun addMultiply(calculation: String): String {
        // Add "*" which lack
        var cleanCalculation = calculation
        var cleanCalculationLength = cleanCalculation.length
        var i = 0
        while (i < cleanCalculationLength) {

            if (cleanCalculation[i] == '(') {
                if (i != 0 && (cleanCalculation[i-1] in "123456789)")) {
                    cleanCalculation = cleanCalculation.addCharAtIndex('*', i)
                    cleanCalculationLength ++
                }
            } else if (cleanCalculation[i] == ')') {
                if (i+1 < cleanCalculation.length && (cleanCalculation[i+1] in "123456789(")) {
                    cleanCalculation = cleanCalculation.addCharAtIndex('*', i+1)
                    cleanCalculationLength ++
                }
            } else if (cleanCalculation[i] == '!') {
                if (i+1 < cleanCalculation.length && (cleanCalculation[i+1] in "123456789π(")) {
                    cleanCalculation = cleanCalculation.addCharAtIndex('*', i+1)
                    cleanCalculationLength ++
                }
            } else if (i-1 >= 0 && cleanCalculation[i] == '√') {
                if (cleanCalculation[i-1] !in "+-/*") {
                    cleanCalculation = cleanCalculation.addCharAtIndex('*', i)
                    cleanCalculationLength ++
                }
            } else if (cleanCalculation[i] == 'π') {
                if (i+1 < cleanCalculation.length && (cleanCalculation[i+1] in "123456789(")) {
                    cleanCalculation = cleanCalculation.addCharAtIndex('*', i+1)
                    cleanCalculationLength ++
                }
                if (i-1 >= 0 && (cleanCalculation[i-1] in "%πe123456789)")) {
                    cleanCalculation = cleanCalculation.addCharAtIndex('*', i)
                    cleanCalculationLength ++
                }
            } else if (cleanCalculation[i] == 'e') {
                if (i+1 < cleanCalculation.length && (cleanCalculation[i+1] in "π123456789(")) {
                    cleanCalculation = cleanCalculation.addCharAtIndex('*', i+1)
                    cleanCalculationLength ++
                }
                if (i-1 >= 0 && (cleanCalculation[i-1] in "%πe123456789)")) {
                    cleanCalculation = cleanCalculation.addCharAtIndex('*', i)
                    cleanCalculationLength ++
                }
            } else {
                if (i+1<cleanCalculation.length) {
                    val functionsList = listOf("arcos", "arcsin", "arctan", "cos", "sin", "tan", "ln", "log", "exp")
                    for (function in functionsList) {
                        val text = cleanCalculation.subSequence(0, i+1).toString()
                        if (text.endsWith(function) && text.length != function.length) {
                            if (text[text.length - function.length - 1] != '*') {
                                cleanCalculation = cleanCalculation.subSequence(0, i - function.length + 1).toString() +
                                        "*" + function + cleanCalculation.subSequence(i+1, cleanCalculation.length).toString()
                                cleanCalculationLength ++
                                break
                            }
                        }
                    }
                }
            }
            i ++
        }
        return cleanCalculation
    }

    private fun formatSquare(calculation: String): String {
        // Replace √5 by sqrt(5)
        var cleanCalculation = calculation
        var parenthesisOpened = 0

        var cleanCalculationLength = cleanCalculation.length
        var i = 0
        while (i < cleanCalculationLength) {
            if (i < cleanCalculation.length - 1) {
                if (parenthesisOpened > 0) {
                    if (cleanCalculation[i+1] in "(*-/+^") {
                        cleanCalculation = cleanCalculation.addCharAtIndex(')', i+1)
                        parenthesisOpened -= 1
                    }
                }
                if (cleanCalculation[i] == '√' && cleanCalculation[i+1] != '(') {
                    cleanCalculation = cleanCalculation.addCharAtIndex('(', i+1)
                    parenthesisOpened += 1
                }
            }
            i++
        }
        cleanCalculation = cleanCalculation.replace("√", "sqrt")
        return cleanCalculation
    }

    private fun formatFactorial(calculation: String): String {
        // Replace 5! by factorial(5)
        var cleanCalculation = calculation
        var parenthesisOpened = 0

        var cleanCalculationLength = cleanCalculation.length
        var i = cleanCalculationLength - 1
        while (i > 0) {
            if (parenthesisOpened > 0) {
                if (cleanCalculation[i-1] in ")*-/+^") {
                    cleanCalculation = cleanCalculation.addCharAtIndex('(', i)
                    cleanCalculation = cleanCalculation.addCharAtIndex('F', i)
                    parenthesisOpened -= 1
                    i += 2
                }
            }
            if (cleanCalculation[i] == '!' && cleanCalculation[i-1] != ')') {
                cleanCalculation = cleanCalculation.addCharAtIndex(')', i)
                parenthesisOpened += 1
                i += 1
            }
            i --
        }

        while (parenthesisOpened > 0) {
            cleanCalculation = cleanCalculation.addCharAtIndex('(', 0)
            cleanCalculation = cleanCalculation.addCharAtIndex('F', 0)
            parenthesisOpened -= 1
        }

        cleanCalculation = cleanCalculation.replace("!", "")
        cleanCalculation = cleanCalculation.replace("F", "factorial")
        return cleanCalculation
    }

    private fun String.addCharAtIndex(char: Char, index: Int) =
        StringBuilder(this).apply { insert(index, char) }.toString()

}