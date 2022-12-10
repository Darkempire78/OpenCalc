package com.darkempire78.opencalculator

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.darkempire78.opencalculator.databinding.ActivityMainBinding
import com.sothree.slidinguppanel.PanelSlideListener
import com.sothree.slidinguppanel.PanelState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormatSymbols

class MainActivity : AppCompatActivity() {

    private val decimalSeparatorSymbol = DecimalFormatSymbols.getInstance().decimalSeparator.toString()
    private var isInvButtonClicked = false
    private var isEqualLastAction = false
    private var isDegreeModeActivated = true // Set degree by default
    private lateinit var binding: ActivityMainBinding

    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var historyLayoutMgr: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Themes
        val themes = Themes(this)
        setTheme(themes.getTheme())

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

        // Set default animations and disable the fade out default animation
        // https://stackoverflow.com/questions/19943466/android-animatelayoutchanges-true-what-can-i-do-if-the-fade-out-effect-is-un
        val lt = LayoutTransition()
        lt.disableTransitionType(LayoutTransition.DISAPPEARING)
        binding.tableLayout.layoutTransition = lt

        // Set decimalSeparator
        (binding.pointButton as ImageButton).setImageResource(if (decimalSeparatorSymbol == ",") R.drawable.comma else R.drawable.dot)

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
            binding.historyRecylcleView.scrollToPosition(historyAdapter.itemCount - 1)
        }

        binding.slidingLayout.addPanelSlideListener(object : PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                if (slideOffset == 0f) { // If the panel got collapsed
                    binding.slidingLayout.scrollableView = binding.historyRecylcleView
                }
            }
            override fun onPanelStateChanged(panel: View, previousState: PanelState, newState: PanelState) {
                if (newState == PanelState.ANCHORED){ // To prevent the panel from getting stuck in the middle
                    binding.slidingLayout.panelState = PanelState.EXPANDED
                }
            }
        })

        // Focus by default
        binding.input.requestFocus()

        // Do not clear after equal button if you move the cursor
        binding.input.accessibilityDelegate = object : View.AccessibilityDelegate() {
            override fun sendAccessibilityEvent(host: View, eventType: Int) {
                super.sendAccessibilityEvent(host, eventType)
                if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED){
                    isEqualLastAction = false
                }
                if (!binding.input.isCursorVisible) {
                    binding.input.isCursorVisible = true
                }
            }
        }

        // LongClick on result to copy it
        binding.resultDisplay.setOnLongClickListener {
            when {
                binding.resultDisplay.text.toString() != "" -> {
                    val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Copied result", binding.resultDisplay.text)
                    clipboardManager.setPrimaryClip(clip)
                    Toast.makeText(this, R.string.value_copied, Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
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
        MyPreferences(this@MainActivity).saveHistory(this@MainActivity, mutableListOf())
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
                binding.inputHorizontalScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
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

            var newValueFormatted = NumberFormatter.format(newValue)

            withContext(Dispatchers.Main) {
                // Avoid two decimalSeparator in the same number
                // 1. When you click on the decimalSeparator button
                if (value == decimalSeparatorSymbol && decimalSeparatorSymbol in binding.input.text.toString()) {
                    if (binding.input.text.toString().isNotEmpty())  {
                        var lastNumberBefore = ""
                        if (cursorPosition > 0  && binding.input.text.toString().substring(0, cursorPosition).last() in "0123456789\\$decimalSeparatorSymbol") {
                            lastNumberBefore = NumberFormatter.extractNumbers(binding.input.text.toString().substring(0, cursorPosition)).last()
                        }
                        var firstNumberAfter = ""
                        if (cursorPosition < binding.input.text.length-1) {
                            firstNumberAfter = NumberFormatter.extractNumbers(binding.input.text.toString().substring(cursorPosition, binding.input.text.length)).first()
                        }
                        if (decimalSeparatorSymbol in lastNumberBefore || decimalSeparatorSymbol in firstNumberAfter) {
                            return@withContext
                        }
                    }
                }
                // 2. When you click on a former calculation from the history
                if (binding.input.text.isNotEmpty()
                    && cursorPosition > 0
                    && decimalSeparatorSymbol in value
                    && value != decimalSeparatorSymbol // The value should not be *only* the decimal separator
                ) {
                    if (NumberFormatter.extractNumbers(value).isNotEmpty()) {
                        val firstValueNumber = NumberFormatter.extractNumbers(value).first()
                        val lastValueNumber = NumberFormatter.extractNumbers(value).last()
                        var tmpNewValue = newValue
                        if (decimalSeparatorSymbol in firstValueNumber || decimalSeparatorSymbol in lastValueNumber) {
                            var numberBefore = NumberFormatter.extractNumbers(binding.input.text.toString().substring(0, cursorPosition)).last()
                            var numberAfter = ""
                            if (cursorPosition < binding.input.text.length - 1) {
                                numberAfter = NumberFormatter.extractNumbers(binding.input.text.toString().substring(cursorPosition, binding.input.text.length)).first()
                            }
                            var tmpValue = value
                            var numberBeforeParenthesisLength = 0
                            if (decimalSeparatorSymbol in numberBefore) {
                                numberBefore = "($numberBefore)"
                                numberBeforeParenthesisLength += 2
                            }
                            if (decimalSeparatorSymbol in  numberAfter) {
                                tmpValue = "($value)"
                            }
                            tmpNewValue = binding.input.text.toString().substring(0, (cursorPosition + numberBeforeParenthesisLength - numberBefore.length)) + numberBefore + tmpValue + rightValue
                            newValueFormatted = NumberFormatter.format(tmpNewValue)
                        }
                    }
                }

                // Update Display
                binding.input.setText(newValueFormatted)

                // Increase cursor position
                val cursorOffset = newValueFormatted.length - newValue.length
                binding.input.setSelection(cursorPosition + value.length + cursorOffset)

                // Update resultDisplay
                updateResultDisplay()
            }
        }
    }

    private fun updateResultDisplay() {
        lifecycleScope.launch(Dispatchers.Default) {
            val calculation = binding.input.text.toString()

            if (calculation != "") {
                val calculationTmp = Expression().getCleanExpression(binding.input.text.toString())
                var result = Calculator().evaluate(calculationTmp, isDegreeModeActivated)
                var resultString = result.toString()
                var formattedResult = NumberFormatter.format(resultString.replace(".", NumberFormatter.decimalSeparatorSymbol))

                if (resultString != "NaN" && resultString != "Infinity" && resultString != getString(R.string.infinity)) {
                    // If result = -0, change it to 0
                    if (result == -0.0) {
                        result = 0.0
                    }
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

    fun keyDigitPadMappingToDisplay(view: View) {
        updateDisplay(view, (view as Button).text as String)
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

    @SuppressLint("SetTextI18n")
    fun degreeButton(view: View) {
        keyVibration(view)

        if (binding.degreeButton.text.toString() == "DEG") {
            binding.degreeButton.text = "RAD"
            isDegreeModeActivated = false
        } else {
            binding.degreeButton.text = "DEG"
            isDegreeModeActivated = true
        }

        binding.degreeTextView?.text = binding.degreeButton.text.toString()
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
        binding.resultDisplay.setText("")
    }

    fun equalsButton(view: View) {
        lifecycleScope.launch(Dispatchers.Default) {
            keyVibration(view)

            val calculation = binding.input.text.toString()

            if (calculation != "") {
                val calculationTmp = Expression().getCleanExpression(binding.input.text.toString())
                val result = Calculator().evaluate(calculationTmp, isDegreeModeActivated)
                var resultString = result.toString()
                var formattedResult = NumberFormatter.format(resultString.replace(".", NumberFormatter.decimalSeparatorSymbol))

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
                    binding.historyRecylcleView.scrollToPosition(historyAdapter.itemCount - 1)
                }

                if (resultString != "NaN" && resultString != "Infinity" && resultString != getString(R.string.infinity)) {
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
                    if (resultString == "Infinity") {
                        binding.resultDisplay.setText(getString(R.string.infinity))
                    } else {
                        withContext(Dispatchers.Main) { binding.resultDisplay.setText(formattedResult) }
                    }
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

        for (i in 0 until cursorPosition) {
            if (text[i] == '(') {
                openParentheses += 1
            }
            if (text[i] == ')') {
                closeParentheses += 1
            }
        }

        if (openParentheses == closeParentheses
            || binding.input.text.toString().subSequence(textLength - 1, textLength) == "("
            || binding.input.text.toString().subSequence(textLength - 1, textLength) in "×÷+-^"
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

        var cursorPosition = binding.input.selectionStart
        val textLength = binding.input.text.length
        var newValue = ""
        var isFunction = false
        var functionLength = 0

        if (isEqualLastAction) {
            cursorPosition = textLength
        }

        if (cursorPosition != 0 && textLength != 0) {
            // Check if it is a function to delete
            val functionsList = listOf("arccos(", "arcsin(", "arctan(", "cos(", "sin(", "tan(", "ln(", "log(", "exp(")
            for (function in functionsList) {
                val text = binding.input.text.subSequence(0, cursorPosition).toString()
                if (text.endsWith(function)) {
                    newValue = binding.input.text.subSequence(0, cursorPosition - function.length).toString() +
                            binding.input.text.subSequence(cursorPosition, textLength).toString()
                    isFunction = true
                    functionLength = function.length - 1
                    break
                }
            }
            // Else
            if (!isFunction) {
                newValue = binding.input.text.subSequence(0, cursorPosition - 1).toString() +
                        binding.input.text.subSequence(cursorPosition, textLength).toString()
            }

            val newValueFormatted = NumberFormatter.format(newValue)
            val cursorOffset = newValueFormatted.length - newValue.length

            binding.input.setText(newValueFormatted)
            binding.input.setSelection((cursorPosition - 1 + cursorOffset - functionLength).takeIf { it > 0 } ?: 0)
        }

        updateResultDisplay()
    }

    fun scientistModeSwitchButton(view: View) {
        if (binding.scientistModeRow2.visibility != View.VISIBLE) {
            binding.scientistModeRow2.visibility = View.VISIBLE
            binding.scientistModeRow3.visibility = View.VISIBLE
            binding.scientistModeSwitchButton?.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
            binding.degreeTextView?.visibility = View.VISIBLE
            binding.degreeTextView?.text = binding.degreeButton.text.toString()
        } else {
            binding.scientistModeRow2.visibility = View.GONE
            binding.scientistModeRow3.visibility = View.GONE
            binding.scientistModeSwitchButton?.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
            binding.degreeTextView?.visibility = View.GONE
            binding.degreeTextView?.text = binding.degreeButton.text.toString()
        }
    }

    override fun onResume() {
        super.onResume()

        // Disable the keyboard on display EditText
        binding.input.showSoftInputOnFocus = false
    }
}