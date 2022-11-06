package com.darkempire78.opencalculator

import android.animation.LayoutTransition
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import android.widget.HorizontalScrollView
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.darkempire78.opencalculator.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mariuszgromada.math.mxparser.Expression
import org.mariuszgromada.math.mxparser.mXparser
import java.text.DecimalFormatSymbols

class MainActivity : AppCompatActivity() {

    private val decimalSeparatorSymbol = DecimalFormatSymbols.getInstance().decimalSeparator.toString()
    private var isInvButtonClicked = false
    private var isEqualLastAction = false
    private lateinit var binding: ActivityMainBinding

    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var historyLayoutMgr: LinearLayoutManager

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
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // check the current selected theme
        Themes(this).checkTheme()

        // Disable the keyboard on display EditText
        binding.input.showSoftInputOnFocus = false

        // https://www.geeksforgeeks.org/how-to-detect-long-press-in-android/
        binding.backspaceButton.setOnLongClickListener {
            binding.input.setText("")
            binding.resultDisplay.setText("")
            true
        }

        // Set degree by default
        mXparser.setDegreesMode()

        // Set default animations and disable the fade out default animation
        // https://stackoverflow.com/questions/19943466/android-animatelayoutchanges-true-what-can-i-do-if-the-fade-out-effect-is-un
        val lt = LayoutTransition()
        lt.disableTransitionType(LayoutTransition.DISAPPEARING)
        binding.tableLayout.layoutTransition = lt

        // Set decimalSeparator
        binding.pointButton.text = decimalSeparatorSymbol

        // Set history
        historyLayoutMgr = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.historyRecylcleView.layoutManager = historyLayoutMgr
        historyAdapter = HistoryAdapter(mutableListOf()) {
            value -> run {
                val valueUpdated = value.replace(".", NumberFormatter.decimalSeparatorSymbol)
                updateDisplay(window.decorView, valueUpdated)
            }
        }
        binding.historyRecylcleView.adapter = historyAdapter
        // Set values
        val historyList = MyPreferences(this).getHistory()
        historyAdapter.appendHistory(historyList)
        // Scroll to the bottom of the recycle view
        if (historyAdapter.itemCount > 0) {
            binding.historyRecylcleView.scrollToPosition(historyAdapter.itemCount - 1);
        }

        // Do not clear after equal button if you move the cursor
        binding.input.setAccessibilityDelegate(object : View.AccessibilityDelegate() {
            override fun sendAccessibilityEvent(host: View, eventType: Int) {
                super.sendAccessibilityEvent(host, eventType)
                if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED){
                    isEqualLastAction = false
                }
                if (!binding.input.isCursorVisible) {
                    binding.input.isCursorVisible = true
                }
            }
        })
    }

    private fun checkIfDarkModeIsEnabledByDefault(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            resources.configuration.isNightModeActive
        } else {
            when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> true
                Configuration.UI_MODE_NIGHT_NO -> false
                Configuration.UI_MODE_NIGHT_UNDEFINED -> true
                else -> true
            }
        }

    fun selectThemeDialog(menuItem: MenuItem) {
        Themes(this).openDialogThemeSelector()
    }

    fun openAppMenu(view: View) {
        val popup = PopupMenu(this, view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.app_menu, popup.menu)
        popup.menu.findItem(R.id.app_menu_vibration_button).isChecked =
            MyPreferences(this).vibrationMode
        popup.show()
    }

    fun checkVibration(menuItem: MenuItem) {
        MyPreferences(this).vibrationMode = !menuItem.isChecked
    }

    fun openAbout(menuItem: MenuItem) {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent, null)
    }

    fun clearHistory(menuItem: MenuItem) {
        // Clear preferences
        MyPreferences(this@MainActivity).saveHistory(this@MainActivity, mutableListOf<History>())
        // Clear drawer
        historyAdapter.clearHistory()
    }

    private fun keyVibration(view: View) {
        if (MyPreferences(this).vibrationMode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS)
            }
        }
    }

    private fun updateDisplay(view: View, value: String) {
        // Reset input with current number if following "equal"
        if (isEqualLastAction) {
            val anyNumber = "0123456789$decimalSeparatorSymbol".toCharArray().map {
                it.toString()
            }
            if (anyNumber.contains(value)) {
                    binding.input.setText("")
            } else {
                binding.input.setSelection(binding.input.text.length)
                binding.inputHorizontalScrollView!!.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
            }
            isEqualLastAction = false
        }

        if (!binding.input.isCursorVisible) {
            binding.input.isCursorVisible = true
        }

        lifecycleScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                // Vibrate when key pressed
                keyVibration(view)
            }

            val formerValue = binding.input.text.toString()
            val cursorPosition = binding.input.selectionStart
            val leftValue = formerValue.subSequence(0, cursorPosition).toString()
            val rightValue = formerValue.subSequence(cursorPosition, formerValue.length).toString()

            val newValue = leftValue + value + rightValue

            val newValueFormatted = NumberFormatter.format(newValue)

            val cursorOffset = newValueFormatted.length - newValue.length
            withContext(Dispatchers.Main) {
                // Update Display
                binding.input.setText(newValueFormatted)

                // Increase cursor position
                binding.input.setSelection(cursorPosition + value.length + cursorOffset)

                // Update resultDisplay
                updateResultDisplay()
            }
        }
    }

    private fun replaceSymbolsFromCalculation(calculation: String): String {
        var calculation2 = calculation.replace('×', '*')
        calculation2 = calculation2.replace('÷', '/')
        calculation2 = calculation2.replace("log", "log10")
        calculation2 = calculation2.replace(NumberFormatter.groupingSeparatorSymbol, "")
        calculation2 = calculation2.replace(NumberFormatter.decimalSeparatorSymbol, ".")
        return calculation2
    }

    private fun updateResultDisplay() {
        lifecycleScope.launch(Dispatchers.Default) {
            val calculation = binding.input.text.toString()

            if (calculation != "") {
                val exp = Expression(getCleanExpression(calculation))
                val result = exp.calculate()
                var resultString = result.toString()
                var formattedResult = NumberFormatter.format(resultString.replace(".", NumberFormatter.decimalSeparatorSymbol))

                if (resultString != "NaN" && resultString != "Infinity") {
                    // If the double ends with .0 we remove the .0
                    if ((result * 10) % 10 == 0.0) {
                        resultString = String.format("%.0f", result)
                        formattedResult = NumberFormatter.format(resultString)

                        withContext(Dispatchers.Main) {
                            if (formattedResult != calculation) {
                                binding.resultDisplay.setText(formattedResult)
                            } else binding.resultDisplay.setText("")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            if (formattedResult != calculation) {
                                binding.resultDisplay.setText(formattedResult)
                            } else {
                                binding.resultDisplay.setText("")
                            }
                        }
                    }
                } else withContext(Dispatchers.Main) {
                    if (resultString == "Infinity") {
                        binding.resultDisplay.setText(getString(R.string.infinity))
                    } else {
                        withContext(Dispatchers.Main) {
                            binding.resultDisplay.setText("")
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    binding.resultDisplay.setText("")
                }
            }
        }
    }

    private fun addParenthesis(calculation: String): String {
        // Add ")" which lack
        var cleanCalculation = calculation
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
                cleanCalculation += ')'
            }
        }

        return cleanCalculation
    }

    private fun addMultiply(calculation: String): String {
        // Add "*" which lack

        var cleanCalculation = calculation

        for (i in 0..calculation.length - 1) {
            if (calculation[i] == '(') {
                if (i != 0 && (calculation[i-1] in "123456789)")) {
                    cleanCalculation = cleanCalculation.addCharAtIndex('*', i)
                }
            } else if (calculation[i] == ')') {
                if (i+1 < calculation.length && (calculation[i+1] in "123456789(")) {
                    cleanCalculation = cleanCalculation.addCharAtIndex('*', i+2)
                }
            }
        }

        return cleanCalculation
    }

    fun String.addCharAtIndex(char: Char, index: Int) =
        StringBuilder(this).apply { insert(index, char) }.toString()

    /* Transform any calculation string containing %
     * to respect the user friend (non-mathematical) way (see issue #35)
     */
    private fun getPercentString(calculation: String): String {
        val percentPos = calculation.lastIndexOf('%')
        if (percentPos < 1) {
            return calculation
        }
        // find the last operator before the last %
        val operatorBeforePercentPos = calculation.subSequence(0, percentPos - 1).lastIndexOfAny(charArrayOf('-', '+', '×', '÷', '('))
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
        return calculationStringFirst + calculation[operatorBeforePercentPos] + calculationStringFirst + "×(" + calculation.subSequence(operatorBeforePercentPos + 1, percentPos) + ")" + calculation.subSequence(percentPos, calculation.length)
    }

    private fun getCleanExpression(calculation: String): String {
        var cleanCalculation = replaceSymbolsFromCalculation(calculation)
        cleanCalculation = addParenthesis(cleanCalculation)
        cleanCalculation = addMultiply(cleanCalculation)
        if (cleanCalculation.contains('%')) {
            cleanCalculation = getPercentString(cleanCalculation)
        }

        return cleanCalculation
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
        val decimalSeparator = decimalSeparatorSymbol
        updateDisplay(view, decimalSeparator)
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

        if (binding.degreeButton.text.toString() == "DEG") {
            binding.degreeButton.text = "RAD"
            mXparser.setRadiansMode()
        } else {
            binding.degreeButton.text = "DEG"
            mXparser.setDegreesMode()
        }

        binding.degreeTextView.text = binding.degreeButton.text.toString()
        updateResultDisplay()
    }

    fun invButton(view: View) {
        keyVibration(view)

        if (!isInvButtonClicked) {
            isInvButtonClicked = true

            // change buttons
            binding.sinusButton.setText(R.string.sinusInv)
            binding.cosinusButton.setText(R.string.cosinusInv)
            binding.tangentButton.setText(R.string.tangentInv)
            binding.naturalLogarithmButton.setText(R.string.naturalLogarithmInv)
            binding.logarithmButton.setText(R.string.logarithmInv)
            binding.squareButton.setText(R.string.squareInv)
        } else {
            isInvButtonClicked = false

            // change buttons
            binding.sinusButton.setText(R.string.sinus)
            binding.cosinusButton.setText(R.string.cosinus)
            binding.tangentButton.setText(R.string.tangent)
            binding.naturalLogarithmButton.setText(R.string.naturalLogarithm)
            binding.logarithmButton.setText(R.string.logarithm)
            binding.squareButton.setText(R.string.square)
        }
    }

    fun clearButton(view: View) {
        keyVibration(view)

        binding.input.setText("")

        // Clear resultDisplay
        binding.resultDisplay.setText("")
    }

    fun equalsButton(view: View) {
        lifecycleScope.launch(Dispatchers.Default) {
            keyVibration(view)

            val calculation = binding.input.text.toString()

            print("\n\n--------------")
            var calculationTmp = getCleanExpression(binding.input.text.toString())
            calculationTmp = calculationTmp.replace("%", "/100")
            calculationTmp = calculationTmp.replace("log10", "logten")
            println(Calculator().evaluate(calculationTmp))
            print("\n-------------\n\n")

            if (calculation != "") {
                val exp = Expression(getCleanExpression(calculation))
                val result = exp.calculate()
                var resultString = result.toString()
                var formattedResult = NumberFormatter.format(resultString.replace(".", NumberFormatter.decimalSeparatorSymbol))

                mXparser.consolePrintln("Res: " + exp.expressionString.toString() + " = " + result)

                // Save to history
                if ((result * 10) % 10 == 0.0) {
                    resultString = String.format("%.0f", result)
                    formattedResult = NumberFormatter.format(resultString)
                }
                val history = MyPreferences(this@MainActivity).getHistory()
                history.add(
                    History(
                        calculation = calculation,
                        result = formattedResult,
                    )
                )
                MyPreferences(this@MainActivity).saveHistory(this@MainActivity, history)
                // Update history variables
                withContext(Dispatchers.Main) {
                    historyAdapter.appendOneHistoryElement(History(
                        calculation = calculation,
                        result = formattedResult,
                    ))
                    // Scroll to the bottom of the recycle view
                    binding.historyRecylcleView.scrollToPosition(historyAdapter.itemCount - 1);
                }


                if (resultString != "NaN" && resultString != "Infinity") {
                    if ((result * 10) % 10 == 0.0) {
                        resultString = String.format("%.0f", result)
                        formattedResult = NumberFormatter.format(resultString)
                        withContext(Dispatchers.Main) { binding.input.setText(formattedResult) }
                    } else {
                        withContext(Dispatchers.Main) { binding.input.setText(formattedResult) }
                    }
                    // Set cursor
                    withContext(Dispatchers.Main) {
                        // Hide the cursor
                        binding.input.isCursorVisible = false
                        // Scroll to the beginning
                        binding.input.setSelection(0)
                        // Clear resultDisplay
                        binding.resultDisplay.setText("")
                    }
                } else {
                    withContext(Dispatchers.Main) { binding.resultDisplay.setText(formattedResult) }
                }
                isEqualLastAction = true
            } else {
                withContext(Dispatchers.Main) { binding.resultDisplay.setText("") }
            }
        }
    }

    fun parenthesesButton(view: View) {
        val cursorPosition = binding.input.selectionStart
        val textLength = binding.input.text.length

        var openParentheses = 0
        var closeParentheses = 0

        val text = binding.input.text.toString()

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

        if (openParentheses == closeParentheses
            || binding.input.text.toString().subSequence(textLength - 1, textLength) == "("
            || binding.input.text.toString().subSequence(textLength - 1, textLength) in "×÷+−^"
        ) {
            updateDisplay(view, "(")
        } else if (closeParentheses < openParentheses && binding.input.text.toString().subSequence(
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

        val cursorPosition = binding.input.selectionStart
        val textLength = binding.input.text.length

        if (cursorPosition != 0 && textLength != 0) {
            val newValue = binding.input.text.subSequence(0, cursorPosition - 1).toString() +
                    binding.input.text.subSequence(cursorPosition, textLength).toString()

            val newValueFormatted = NumberFormatter.format(newValue)

            val cursorOffset = newValueFormatted.length - newValue.length

            binding.input.setText(newValueFormatted)

            binding.input.setSelection((cursorPosition - 1 + cursorOffset).takeIf { it > 0 } ?: 0)
        }

        updateResultDisplay()
    }

    fun scientistModeSwitchButton(view: View) {
        if (binding.scientistModeRow2.visibility != View.VISIBLE) {
            binding.scientistModeRow2.visibility = View.VISIBLE
            binding.scientistModeRow3.visibility = View.VISIBLE
            binding.scientistModeSwitchButton?.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
            binding.degreeTextView.visibility = View.VISIBLE
            binding.degreeTextView.text = binding.degreeButton.text.toString()
        } else {
            binding.scientistModeRow2.visibility = View.GONE
            binding.scientistModeRow3.visibility = View.GONE
            binding.scientistModeSwitchButton?.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
            binding.degreeTextView.visibility = View.GONE
            binding.degreeTextView.text = binding.degreeButton.text.toString()
        }
    }

    override fun onResume() {
        super.onResume()

        // Disable the keyboard on display EditText
        binding.input.showSoftInputOnFocus = false
    }
}