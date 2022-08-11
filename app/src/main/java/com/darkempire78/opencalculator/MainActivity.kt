package com.darkempire78.opencalculator

import android.animation.LayoutTransition
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mariuszgromada.math.mxparser.Expression
import org.mariuszgromada.math.mxparser.mXparser


class MainActivity : AppCompatActivity() {

    // https://stackoverflow.com/questions/34197026/android-content-pm-applicationinfo-android-content-context-getapplicationinfo
    private var isInvButtonClicked = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Themes
        Themes(this)

        when (MyPreferences(this).darkMode) {
            // System
            -1 -> {
                if (checkIfDarkModeIsEnabledByDefault()) {
                    setTheme(R.style.darkTheme)
                } else {
                    setTheme(R.style.AppTheme)
                }
            }
            // Light mode
            0 -> {
                setTheme(R.style.AppTheme)
            }
            // Dark mode
            1 -> {
                setTheme(R.style.darkTheme)
            }
            // amoled mode
            2 -> {
                setTheme(R.style.amoledTheme)
            }
            // Material You
            3 -> {
                if (checkIfDarkModeIsEnabledByDefault()) {
                    setTheme(R.style.materialYouDark)
                } else {
                    setTheme(R.style.materialYouLight)
                }

            }
            else -> {
                if (checkIfDarkModeIsEnabledByDefault()) {
                    setTheme(R.style.darkTheme)
                } else {
                    setTheme(R.style.AppTheme)
                }
            }
        }

        setContentView(R.layout.activity_main)

        // check the current selected theme
        Themes(this).checkTheme()

        // Disable the keyboard on display EditText
        input.showSoftInputOnFocus = false

        // https://www.geeksforgeeks.org/how-to-detect-long-press-in-android/
        backspaceButton.setOnLongClickListener {
            input.setText("")
            resultDisplay.setText("")
            true
        }

        // Set degree by default
        mXparser.setDegreesMode()

        // Set default animations and disable the fade out default animation
        // https://stackoverflow.com/questions/19943466/android-animatelayoutchanges-true-what-can-i-do-if-the-fade-out-effect-is-un
        val lt = LayoutTransition()
        lt.disableTransitionType(LayoutTransition.DISAPPEARING)
        tableLayout.layoutTransition = lt

//        input.setOnCapturedPointerListener()


    }

    private fun checkIfDarkModeIsEnabledByDefault(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            resources.configuration.isNightModeActive
        } else
            when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> true
                Configuration.UI_MODE_NIGHT_NO -> false
                Configuration.UI_MODE_NIGHT_UNDEFINED -> true
                else -> true
            }

    fun selectThemeDialog(menuItem: MenuItem) {
        Themes(this).openDialogThemeSelector()
    }

    fun openAppMenu(view: View) {
        val popup = PopupMenu(this, view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.app_menu, popup.menu)
        popup.menu.findItem(R.id.app_menu_vibration_button).isChecked =
            MyPreferences(this).vibrationMode;
        popup.show()
    }

    fun checkVibration(menuItem: MenuItem) {
        MyPreferences(this).vibrationMode = !menuItem.isChecked
    }

    fun openAbout(menuItem: MenuItem) {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent, null)
    }

    private fun keyVibration(view: View) {
        if (MyPreferences(this).vibrationMode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS)
            }
        }
    }

    private fun updateDisplay(view: View, value: String) {
        lifecycleScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                // Vibrate when key pressed
                keyVibration(view)
            }

            val formerValue = input.text.toString()
            val cursorPosition = input.selectionStart
            val leftValue = formerValue.subSequence(0, cursorPosition).toString()
            val rightValue = formerValue.subSequence(cursorPosition, formerValue.length).toString()

            val newValue = leftValue + value + rightValue

            val newValueFormatted = NumberFormatter.format(newValue)

            val cursorOffset = newValueFormatted.length - newValue.length
            withContext(Dispatchers.Main) {
                // Update Display
                input.setText(newValueFormatted)

                // Increase cursor position
                input.setSelection(cursorPosition + value.length + cursorOffset)

                // Update resultDisplay
                updateResultDisplay()
            }
        }
    }

    fun replaceSymbolsFromCalculation(calculation: String): String {
        var calculation2 = calculation.replace('×', '*')
        calculation2 = calculation2.replace('÷', '/')
        calculation2 = calculation2.replace("log", "log10")
        calculation2 = calculation.replace(NumberFormatter.groupingSeparatorSymbol,"")
        calculation2 = calculation2.replace(NumberFormatter.decimalSeparatorSymbol,".")
        return calculation2
    }

    private fun updateResultDisplay() {
        lifecycleScope.launch(Dispatchers.Default) {
            var calculation = input.text.toString()

            if (calculation != "") {
                calculation = replaceSymbolsFromCalculation(calculation)


                // Add ")" which lack
                var openParentheses = 0
                var closeParentheses = 0

                for (i in 0..calculation.length - 1) {
                    if (calculation[i] == '(') {
                        openParentheses += 1
                    }
                    if (calculation[i] == ')') {
                        closeParentheses += 1
                    }
                }
                if (closeParentheses < openParentheses) {
                    for (i in 0..openParentheses - closeParentheses - 1) {
                        calculation += ')'
                    }
                }

                val exp = Expression(calculation)
                var result = exp.calculate().toString()
                var formattedResult = NumberFormatter.format(result)

                if (result != "NaN" && result != "Infinity") {
                    // If the double ends with .0 we remove the .0
                    if ((exp.calculate() * 10) % 10 == 0.0) {
                        result = String.format("%.0f", exp.calculate())
                        formattedResult = NumberFormatter.format(result)

                        withContext(Dispatchers.Main) {
                            if (result != calculation){
                                resultDisplay.setText(formattedResult)
                            }
                            else resultDisplay.setText("")
                        }
                    } else {
                        withContext(Dispatchers.Main) {

                            if (result != calculation) {
                                resultDisplay.setText(formattedResult)
                            } else {
                                resultDisplay.setText("")
                            }
                        }
                    }
                } else withContext(Dispatchers.Main) {
                    if (result == "Infinity") {

                        resultDisplay.setText("Infinity")

                    } else {
                        withContext(Dispatchers.Main) {
                            resultDisplay.setText("")
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    resultDisplay.setText("")
                }
            }
        }
    }

    fun zeroButton(view: View) {
        updateDisplay(view, "0")
    }

    fun oneButton(view: View) {
        updateDisplay(view, "1")
    }

    fun twoButton(view: View) {
        updateDisplay(view, "2")
    }

    fun threeButton(view: View) {
        updateDisplay(view, "3")
    }

    fun fourButton(view: View) {
        updateDisplay(view, "4")
    }

    fun fiveButton(view: View) {
        updateDisplay(view, "5")
    }

    fun sixButton(view: View) {
        updateDisplay(view, "6")
    }

    fun sevenButton(view: View) {
        updateDisplay(view, "7")
    }

    fun eightButton(view: View) {
        updateDisplay(view, "8")
    }

    fun nineButton(view: View) {
        updateDisplay(view, "9")
    }

    fun addButton(view: View) {
        updateDisplay(view, "+")
    }

    fun substractButton(view: View) {
        updateDisplay(view, "-")
    }

    fun pointButton(view: View) {
        updateDisplay(view, ".")
    }

    fun devideButton(view: View) {
        updateDisplay(view, "÷")
    }

    fun multiplyButton(view: View) {
        updateDisplay(view, "×")
    }

    fun exponentButton(view: View) {
        updateDisplay(view, "^")
    }

    fun sinusButton(view: View) {
        if (!isInvButtonClicked) {
            updateDisplay(view, "sin(")
        } else {
            updateDisplay(view, "arcsin(")
        }
    }

    fun cosinusButton(view: View) {
        if (!isInvButtonClicked) {
            updateDisplay(view, "cos(")
        } else {
            updateDisplay(view, "arccos(")
        }

    }

    fun tangentButton(view: View) {
        if (!isInvButtonClicked) {
            updateDisplay(view, "tan(")
        } else {
            updateDisplay(view, "arctan(")
        }
    }

    fun eButton(view: View) {
        updateDisplay(view, "e")
    }

    fun naturalLogarithmButton(view: View) {
        if (!isInvButtonClicked) {
            updateDisplay(view, "ln(")
        } else {
            updateDisplay(view, "exp(")
        }

    }

    fun logarithmButton(view: View) {
        if (!isInvButtonClicked) {
            updateDisplay(view, "log(")
        } else {
            updateDisplay(view, "10^")
        }
    }

    fun piButton(view: View) {
        updateDisplay(view, "π")
    }

    fun factorialButton(view: View) {
        updateDisplay(view, "!")
    }

    fun squareButton(view: View) {
        if (!isInvButtonClicked) {
            updateDisplay(view, "√")
        } else {
            updateDisplay(view, "^2")
        }

    }

    fun devideBy100(view: View) {
        updateDisplay(view, "%")
    }

    fun degreeButton(view: View) {
        keyVibration(view)

        if (degreeButton.text.toString() == "DEG") {
            degreeButton.text = "RAD"
            mXparser.setRadiansMode()
        } else {
            degreeButton.text = "DEG"
            mXparser.setDegreesMode()
        }

        degreeTextView.text = degreeButton.text.toString()
        updateResultDisplay()
    }

    fun invButton(view: View) {
        keyVibration(view)

        if (!isInvButtonClicked) {
            isInvButtonClicked = true

            // change buttons
            sinusButton.setText(R.string.sinusInv)
            cosinusButton.setText(R.string.cosinusInv)
            tangentButton.setText(R.string.tangentInv)
            naturalLogarithmButton.setText(R.string.naturalLogarithmInv)
            logarithmButton.setText(R.string.logarithmInv)
            squareButton.setText(R.string.squareInv)
        } else {
            isInvButtonClicked = false

            // change buttons
            sinusButton.setText(R.string.sinus)
            cosinusButton.setText(R.string.cosinus)
            tangentButton.setText(R.string.tangent)
            naturalLogarithmButton.setText(R.string.naturalLogarithm)
            logarithmButton.setText(R.string.logarithm)
            squareButton.setText(R.string.square)
        }
    }

    fun clearButton(view: View) {
        keyVibration(view)

        input.setText("")

        // Clear resultDisplay
        resultDisplay.setText("")
    }

    fun equalsButton(view: View) {

        lifecycleScope.launch(Dispatchers.Default) {
            keyVibration(view)

            var calculation = input.text.toString()
            calculation = replaceSymbolsFromCalculation(calculation)

            if (calculation != "") {
                // Add ")" which lack
                var openParentheses = 0
                var closeParentheses = 0

                for (i in 0..calculation.length - 1) {
                    if (calculation[i] == '(') {
                        openParentheses += 1
                    }
                    if (calculation[i] == ')') {
                        closeParentheses += 1
                    }
                }
                if (closeParentheses < openParentheses) {
                    for (i in 0..openParentheses - closeParentheses - 1) {
                        calculation += ')'
                    }
                }

                val exp = Expression(calculation)
                var result = exp.calculate().toString()
                var formattedResult = NumberFormatter.format(result)

                mXparser.consolePrintln("Res: " + exp.expressionString.toString() + " = " + exp.calculate())

                if (result != "NaN" && result != "Infinity") {
                    if ((exp.calculate() * 10) % 10 == 0.0) {
                        result = String.format("%.0f", exp.calculate())
                        formattedResult = NumberFormatter.format(result)
                        withContext(Dispatchers.Main) { input.setText(formattedResult) }
                    } else {
                        withContext(Dispatchers.Main) { input.setText(formattedResult) }
                    }
                    // Set cursor
                    withContext(Dispatchers.Main) {
                        input.setSelection(input.text.length)

                        // Clear resultDisplay
                        resultDisplay.setText("")
                    }
                } else {
                    withContext(Dispatchers.Main) { resultDisplay.setText(formattedResult) }
                }
            } else {
                withContext(Dispatchers.Main) { resultDisplay.setText("") }
            }
        }
    }

    fun parenthesesButton(view: View) {
        val cursorPosition = input.selectionStart
        val textLength = input.text.length

        var openParentheses = 0
        var closeParentheses = 0

        val text = input.text.toString()

        // https://kotlinlang.org/docs/ranges.html
        // https://www.reddit.com/r/Kotlin/comments/couh07/getting_error_operator_cannot_be_applied_to_char/
        for (i in 0..cursorPosition - 1) {
            if (text[i] == '(') {
                openParentheses += 1
            }
            if (text[i] == ')') {
                closeParentheses += 1
            }
        }

        if (openParentheses == closeParentheses || input.text.toString().subSequence(
                textLength - 1,
                textLength
            ) == "("
        ) {
            updateDisplay(view, "(")
        } else if (closeParentheses < openParentheses && input.text.toString().subSequence(
                textLength - 1,
                textLength
            ) != "("
        ) {
            updateDisplay(view, ")")
        }

        updateResultDisplay()
    }

    fun backspaceButton(view: View) {
        keyVibration(view)

        val cursorPosition = input.selectionStart
        val textLength = input.text.length

        if (cursorPosition != 0 && textLength != 0) {

            val newValue = input.text.subSequence(0, cursorPosition - 1).toString() +
                           input.text.subSequence(cursorPosition, textLength).toString()

            val newValueFormatted = NumberFormatter.format(newValue)

            val cursorOffset = newValueFormatted.length - newValue.length

            input.setText(newValueFormatted)

            input.setSelection((cursorPosition - 1 + cursorOffset).takeIf { it > 0 } ?: 0)

        }

        updateResultDisplay()
    }

    fun scientistModeSwitchButton(view: View) {
        if (scientistModeRow2.visibility != View.VISIBLE) {
            scientistModeRow2.visibility = View.VISIBLE
            scientistModeRow3.visibility = View.VISIBLE
            scientistModeSwitchButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
            degreeTextView.visibility = View.VISIBLE
            degreeTextView.text = degreeButton.text.toString()
        } else {
            scientistModeRow2.visibility = View.GONE
            scientistModeRow3.visibility = View.GONE
            scientistModeSwitchButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
            degreeTextView.visibility = View.GONE
            degreeTextView.text = degreeButton.text.toString()
        }
    }

}