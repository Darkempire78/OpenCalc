package com.darkempire78.opencalculator

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import org.mariuszgromada.math.mxparser.*


class MainActivity : AppCompatActivity() {

    // https://stackoverflow.com/questions/34197026/android-content-pm-applicationinfo-android-content-context-getapplicationinfo
    private val display: EditText
    get() = findViewById(R.id.input)

    private val resultDisplay: TextView
    get() = findViewById(R.id.resultDisplay)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Themes
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES || AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) {
            setTheme(R.style.darkTheme) //when dark mode is enabled, we use the dark theme
        } else {
            setTheme(R.style.AppTheme)  //default app theme
        }

        setContentView(R.layout.activity_main)

        // check the current selected theme
        checkTheme()

        // Disable the keyboard on display EditText
        display.showSoftInputOnFocus = false

        // Long press backspace button
        val backspaceButton = findViewById<ImageButton>(R.id.backspaceButton)

        // https://www.geeksforgeeks.org/how-to-detect-long-press-in-android/
        backspaceButton.setOnLongClickListener {
            display.setText("")

            // Clear resultDisplay
            resultDisplay.setText("")
            true
        }
    }

    fun selectThemeDialog(view: View) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.select_theme_title))
        val styles = arrayOf("Light", "Dark")
        val checkedItem = MyPreferences(this).darkMode

        builder.setSingleChoiceItems(styles, checkedItem) { dialog, which ->
            when (which) {
                0 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    MyPreferences(this).darkMode = 0
                    delegate.applyDayNight()
                    dialog.dismiss()
                }
                1 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    MyPreferences(this).darkMode = 1
                    delegate.applyDayNight()
                    dialog.dismiss()
                }
            }

        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun checkTheme() {
        when (MyPreferences(this).darkMode) {
            0 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                delegate.applyDayNight()
            }
            1 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                delegate.applyDayNight()
            }
        }
    }

    fun updateDisplay(value: String) {
        var formerValue = display.text.toString()
        var cursorPosition = display.selectionStart
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

        if (calculation != "") {
            calculation = calculation.replace('×', '*')
            calculation = calculation.replace('÷', '/')

            val exp = Expression(calculation)
            var result = exp.calculate().toString()

            if (result != "NaN" && result != "Infinity") {
                // If the double ends with .0 we remove the .0
                if ((exp.calculate() * 10) % 10 == 0.0) {
                    result = String.format("%.0f", exp.calculate())
                    resultDisplay.setText(result)
                } else {
                    resultDisplay.setText(result)
                }
                resultDisplay.setText(result)
            } else if (result == "Infinity") {
                resultDisplay.setText("Infinity")
            } else {
                resultDisplay.setText("")
            }
        }
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
        var result = exp.calculate().toString()

        mXparser.consolePrintln("Res: " + exp.expressionString.toString() + " = " + exp.calculate())

        if (result != "NaN" && result != "Infinity") {
            if ((exp.calculate() * 10) % 10 == 0.0) {
                result = String.format("%.0f", exp.calculate())
                display.setText(result)
            } else {
                display.setText(result)
            }
            // Set cursor
            display.setSelection(display.text.length)

            // Clear resultDisplay
            resultDisplay.setText("")
        } else {
            resultDisplay.setText(result)
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