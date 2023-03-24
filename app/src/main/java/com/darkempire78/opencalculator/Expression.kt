package com.darkempire78.opencalculator

class Expression {

    fun getCleanExpression(calculation: String, decimalSeparatorSymbol: String, groupingSeparatorSymbol: String): String {
        var cleanCalculation = replaceSymbolsFromCalculation(calculation, decimalSeparatorSymbol, groupingSeparatorSymbol)
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

    private fun replaceSymbolsFromCalculation(calculation: String, decimalSeparatorSymbol: String, groupingSeparatorSymbol: String): String {
        var calculation2 = calculation.replace('×', '*')
        calculation2 = calculation2.replace('÷', '/')
        calculation2 = calculation2.replace("log", "logten")
        calculation2 = calculation2.replace("E", "*10^")
        // To avoid that "exp" is interpreted as "e", exp -> xp
        calculation2 = calculation2.replace("exp", "xp")
        // To avoid missmatch with cos, sin, tan -> arcco, arcsi, arcta
        calculation2 = calculation2.replace("cos⁻¹", "arcco")
        calculation2 = calculation2.replace("sin⁻¹", "arcsi")
        calculation2 = calculation2.replace("tan⁻¹", "arcta")
        calculation2 = calculation2.replace(groupingSeparatorSymbol, "")
        calculation2 = calculation2.replace(decimalSeparatorSymbol, ".")
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

        if (calculation[operatorBeforePercentPos] == '*') {
            return calculation
        }
        
        if(calculation[operatorBeforePercentPos] == '/') {
            // insert brackets into percentage. Fixes 900/10% -> 900/(10/100), not 900/10/100 which evals differently.
            // also prevents it from doing the rest of this function, which screws the calculation up
            val stringFirst = calculation.substring(0, operatorBeforePercentPos+1)
            val stringMiddle = calculation.substring(operatorBeforePercentPos+1, percentPos+1)
            val stringLast = calculation.substring(percentPos+1, calculation.length)
            return "$stringFirst($stringMiddle)$stringLast"
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
                if (i != 0 && (cleanCalculation[i-1] in ".e0123456789)")) {
                    cleanCalculation = cleanCalculation.addCharAtIndex('*', i)
                    cleanCalculationLength ++
                }
            } else if (cleanCalculation[i] == ')') {
                if (i+1 < cleanCalculation.length && (cleanCalculation[i+1] in "0123456789(")) {
                    cleanCalculation = cleanCalculation.addCharAtIndex('*', i+1)
                    cleanCalculationLength ++
                }
            } else if (cleanCalculation[i] == '!') {
                if (i+1 < cleanCalculation.length && (cleanCalculation[i+1] in "0123456789π(")) {
                    cleanCalculation = cleanCalculation.addCharAtIndex('*', i+1)
                    cleanCalculationLength ++
                }
            } else if (cleanCalculation[i] == '%') {
                if (i+1 < cleanCalculation.length && (cleanCalculation[i+1] in "0123456789π(")) {
                    cleanCalculation = cleanCalculation.addCharAtIndex('*', i+1)
                    cleanCalculationLength ++
                }
            } else if (i-1 >= 0 && cleanCalculation[i] == '√') {
                if (cleanCalculation[i-1] !in "+-/*(") {
                    cleanCalculation = cleanCalculation.addCharAtIndex('*', i)
                    cleanCalculationLength ++
                }
            } else if (cleanCalculation[i] == 'π') {
                if (i+1 < cleanCalculation.length && (cleanCalculation[i+1] in "0123456789(")) {
                    cleanCalculation = cleanCalculation.addCharAtIndex('*', i+1)
                    cleanCalculationLength ++
                }
                if (i-1 >= 0 && (cleanCalculation[i-1] in ".%πe0123456789)")) {
                    cleanCalculation = cleanCalculation.addCharAtIndex('*', i)
                    cleanCalculationLength ++
                }
            } else if (cleanCalculation[i] == 'e') {
                if (i+1 < cleanCalculation.length && (cleanCalculation[i+1] in "π0123456789(")) {
                    cleanCalculation = cleanCalculation.addCharAtIndex('*', i+1)
                    cleanCalculationLength ++
                }
                if (i-1 >= 0 && (cleanCalculation[i-1] in ".%πe0123456789)")) {
                    cleanCalculation = cleanCalculation.addCharAtIndex('*', i)
                    cleanCalculationLength ++
                }
            } else {
                if (i+1<cleanCalculation.length) {
                    val functionsList = listOf("arcco", "arcsi", "arcta", "cos", "sin", "tan", "ln", "log", "xp")
                    for (function in functionsList) {
                        val text = cleanCalculation.subSequence(0, i+1).toString()
                        if (text.endsWith(function) && text.length != function.length) {
                            if (text[text.length - function.length - 1] !in "+-/*(^") {
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

        val cleanCalculationLength = cleanCalculation.length
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
        var i = calculation.length - 1

        // Return error if the calculation is "!"
        if (i == 0) {
            syntax_error = true
            return ""
        } else {
            var cleanCalculation = calculation

            // Replace 5! by factorial(5)
            while (i > 0) {
                var parenthesisOpened = 0
                // If the current character is "!"
                if (cleanCalculation[i] == '!') {
                    // If the previous character is a parenthesis
                    if (cleanCalculation[i-1] == ')') {
                        // Remove the "!"
                        cleanCalculation = cleanCalculation.substring(0, i) + cleanCalculation.substring(i+1)

                        var j = i
                        while (j > 0) {
                            if (cleanCalculation[j-1] in "*/+^" && parenthesisOpened == 0) {
                                break
                            }
                            // If the previous character isn't a parenthesis
                            if (cleanCalculation[j-1] != ')') {
                                // Count open parentheses
                                if (cleanCalculation[j] == ')') parenthesisOpened +=1
                                else if (cleanCalculation[j-1] == '(') parenthesisOpened -= 1

                                // If there are no open parentheses, add an F in front of the 1st parenthesis
                                if (parenthesisOpened == 0) {
                                    cleanCalculation = cleanCalculation.addCharAtIndex('F', j-1)
                                    break
                                }
                            }

                            // Decrement i on each run
                            j--
                        }
                    } else {
                        // If the previous character is not a parenthesis, add one
                        cleanCalculation = cleanCalculation.substring(0, i) + ')' + cleanCalculation.substring(i + 1)

                        // Store i in a temporary variable
                        var tmp = i

                        // Run until the previous character is a symbol or parenthesis
                        while (i > 0 && cleanCalculation[i-1] !in "()*-/+^") {
                            // Count open parentheses
                            if (cleanCalculation[i] == ')') parenthesisOpened +=1
                            else if (cleanCalculation[i] == '(') parenthesisOpened -= 1

                            while (i > 1 && cleanCalculation[i-1].isDigit() && cleanCalculation[i-2] !in "()*-/+^") i--

                            // If there is only one parenthesis open, close it and add an F in front of it
                            if (parenthesisOpened == 1) {
                                cleanCalculation = cleanCalculation.addCharAtIndex('(', i-1)
                                cleanCalculation = cleanCalculation.addCharAtIndex('F', i-1)
                            }

                            // Decrement i on each run
                            i--
                        }

                        // Restore i from the temporary variable
                        i = tmp
                    }
                }
                // Decrement i on each run
                i--
            }

            // Replace "F" with "factorial"
            cleanCalculation = cleanCalculation.replace("F", "factorial")

            // Return the final result, so it can be calculated
            return cleanCalculation
        }
    }

    private fun String.addCharAtIndex(char: Char, index: Int) =
        StringBuilder(this).apply { insert(index, char) }.toString()

}
