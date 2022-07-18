package com.darkempire78.opencalculator

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.mariuszgromada.math.mxparser.*


class MainActivity : AppCompatActivity() {

    // https://stackoverflow.com/questions/34197026/android-content-pm-applicationinfo-android-content-context-getapplicationinfo
    private val display: EditText
    get() = findViewById(R.id.input)

    private val resultDisplay: TextView
    get() = findViewById(R.id.resultDisplay)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        display.showSoftInputOnFocus = false
    }

    fun updateDisplay(value: String) {
        var formerValue = display.text.toString()
        var cursorPosition = display.selectionStart.toInt()
        var leftValue = formerValue.subSequence(0, cursorPosition).toString()
        var rightValue = formerValue.subSequence(cursorPosition, formerValue.length).toString()

        var newValue = leftValue + value + rightValue

        // Update Display
        display.setText(newValue)

        // Increase cursor position
        display.setSelection(cursorPosition + 1)

        // Update resultDisplay
        updateResultDisplay()
    }

    fun updateResultDisplay() {
        var calculation = display.text.toString()
        calculation = calculation.replace('×', '*')
        calculation = calculation.replace('÷', '/')

        val exp = Expression(calculation)
        val result = exp.calculate().toString()
        resultDisplay.setText(result)
    }

    fun zeroButton(view: View) {
        updateDisplay("0")
    }

    fun oneButton(view: View) {
        updateDisplay("1")
    }

    fun twoButton(view: View) {
        updateDisplay("2")
    }

    fun threeButton(view: View) {
        updateDisplay("3")
    }

    fun fourButton(view: View) {
        updateDisplay("4")
    }

    fun fiveButton(view: View) {
        updateDisplay("5")
    }

    fun sixButton(view: View) {
        updateDisplay("6")
    }

    fun sevenButton(view: View) {
        updateDisplay("7")
    }

    fun eightButton(view: View) {
        updateDisplay("8")
    }

    fun nineButton(view: View) {
        updateDisplay("9")
    }

    fun addButton(view: View) {
        updateDisplay("+")
    }

    fun substractButton(view: View) {
        updateDisplay("-")
    }

    fun pointButton(view: View) {
        updateDisplay(".")
    }

    fun devideButton(view: View) {
        updateDisplay("÷")
    }

    fun multiplyButton(view: View) {
        updateDisplay("×")
    }

    fun exponentButton(view: View) {
        updateDisplay("^")
    }

    fun clearButton(view: View) {
        display.setText("")

        // Clear resultDisplay
        resultDisplay.setText("")
    }

    fun equalsButton(view: View) {
        var calculation = display.text.toString()
        calculation = calculation.replace('×', '*')
        calculation = calculation.replace('÷', '/')

        val exp = Expression(calculation)
        val result = exp.calculate().toString()

        mXparser.consolePrintln("Res: " + exp.expressionString.toString() + " = " + exp.calculate())

        if (result != "NaN" && result != "Infinity") {
            display.setText(result)
            // Set cursor
            display.setSelection(display.text.length)

            // Clear resultDisplay
            resultDisplay.setText("")
        }
    }

    fun parenthesesButton(view: View) {
        var cursorPosition = display.selectionStart
        var textLength = display.text.length

        var openParentheses = 0
        var closeParentheses = 0

        var text = display.text.toString()

        // https://kotlinlang.org/docs/ranges.html
        // https://www.reddit.com/r/Kotlin/comments/couh07/getting_error_operator_cannot_be_applied_to_char/
        for (i in 0..cursorPosition-1) {
            if (text[i] == '(') {
                openParentheses += 1
            }
            if (text[i] == ')') {
                closeParentheses += 1
            }
        }

        if (openParentheses == closeParentheses || display.text.toString().subSequence(
                textLength - 1,
                textLength
            ) == "(") {
            updateDisplay("(")
        } else if (closeParentheses < openParentheses && display.text.toString().subSequence(
                textLength - 1,
                textLength
            ) != "(") {
            updateDisplay(")")
        }

        updateResultDisplay()
    }

    fun backspaceButton(view: View) {
        var cursorPosition = display.selectionStart.toInt()
        var textLength = display.text.length

        if (cursorPosition != 0 && textLength != 0) {
            var newValue = display.text.subSequence(0, textLength - 1).toString()
            display.setText(newValue)

            display.setSelection(cursorPosition - 1)
        }

        updateResultDisplay()
    }

}