package com.darkempire78.opencalculator

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.darkempire78.opencalculator.databinding.ActivityMainBinding
import com.sothree.slidinguppanel.PanelSlideListener
import com.sothree.slidinguppanel.PanelState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormatSymbols
import java.util.*


var appLanguage: Locale = Locale.getDefault()
var currentTheme: Int = 0

class MainActivity : AppCompatActivity() {
    private lateinit var view: View

    private val decimalSeparatorSymbol =
        DecimalFormatSymbols.getInstance().decimalSeparator.toString()
    private val groupingSeparatorSymbol =
        DecimalFormatSymbols.getInstance().groupingSeparator.toString()

    private var isInvButtonClicked = false
    private var isEqualLastAction = false
    private var isDegreeModeActivated = true // Set degree by default
    private var errorStatusOld = false

    private var calculationResult = BigDecimal.ZERO

    private lateinit var binding: ActivityMainBinding
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var historyLayoutMgr: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Themes
        val themes = Themes(this)
        themes.applyDayNightOverride()
        setTheme(themes.getTheme())

        currentTheme = themes.getTheme()

        binding = ActivityMainBinding.inflate(layoutInflater)
        view = binding.root
        setContentView(view)

        // Disable the keyboard on display EditText
        binding.input.showSoftInputOnFocus = false

        // https://www.geeksforgeeks.org/how-to-detect-long-press-in-android/
        binding.backspaceButton.setOnLongClickListener {
            binding.input.setText("")
            binding.resultDisplay.text = ""
            true
        }

        // Set default animations and disable the fade out default animation
        // https://stackoverflow.com/questions/19943466/android-animatelayoutchanges-true-what-can-i-do-if-the-fade-out-effect-is-un
        val lt = LayoutTransition()
        lt.disableTransitionType(LayoutTransition.DISAPPEARING)
        binding.tableLayout.layoutTransition = lt

        // Set decimalSeparator
        binding.pointButton.setImageResource(if (decimalSeparatorSymbol == ",") R.drawable.comma else R.drawable.dot)

        // Set history
        historyLayoutMgr = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.historyRecylcleView.layoutManager = historyLayoutMgr
        historyAdapter = HistoryAdapter(mutableListOf()) { value ->
            run {
                //val valueUpdated = value.replace(".", NumberFormatter.decimalSeparatorSymbol)
                updateDisplay(window.decorView, value)
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

        // Disable history if setting enabled
        val historySize = MyPreferences(this).historySize!!.toInt()
        if (historySize == 0) {
            binding.historyRecylcleView.visibility = View.GONE
            binding.slidingLayoutButton.visibility = View.GONE
            binding.slidingLayout.isEnabled = false
        } else {
            binding.historyRecylcleView.visibility = View.VISIBLE
            binding.slidingLayoutButton.visibility = View.VISIBLE
            binding.slidingLayout.isEnabled = true
        }

        binding.slidingLayout.addPanelSlideListener(object : PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                if (slideOffset == 0f) { // If the panel got collapsed
                    binding.slidingLayout.scrollableView = binding.historyRecylcleView
                }
            }

            override fun onPanelStateChanged(
                panel: View,
                previousState: PanelState,
                newState: PanelState
            ) {
                if (newState == PanelState.ANCHORED) { // To prevent the panel from getting stuck in the middle
                    binding.slidingLayout.panelState = PanelState.EXPANDED
                }
            }
        })

        // Prevent the phone from sleeping (if option enabled)
        if (MyPreferences(this).preventPhoneFromSleepingMode) {
            view.keepScreenOn = true
        }

        // scientific mode enabled by default (if option enabled)
        if (MyPreferences(this).scientificMode) {
            enableOrDisableScientistMode()
        }

        // use radians instead of degrees by default (if option enabled)
        if (MyPreferences(this).useRadiansByDefault) {
            toggleDegreeMode()
        }

        // Focus by default
        binding.input.requestFocus()

        // Makes the input take the whole width of the screen by default
        val screenWidthPX = resources.displayMetrics.widthPixels
        binding.input.minWidth =
            screenWidthPX - (binding.input.paddingRight + binding.input.paddingLeft) // remove the paddingHorizontal

        // Do not clear after equal button if you move the cursor
        binding.input.accessibilityDelegate = object : View.AccessibilityDelegate() {
            override fun sendAccessibilityEvent(host: View, eventType: Int) {
                super.sendAccessibilityEvent(host, eventType)
                if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
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
                    if (MyPreferences(this).longClickToCopyValue) {
                        val clipboardManager =
                            getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        clipboardManager.setPrimaryClip(
                            ClipData.newPlainText(
                                "Copied result",
                                binding.resultDisplay.text
                            )
                        )
                        // Only show a toast for Android 12 and lower.
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                            Toast.makeText(this, R.string.value_copied, Toast.LENGTH_SHORT).show()
                        true
                    } else {
                        false
                    }
                }

                else -> false
            }
        }

        // Handle changes into input to update resultDisplay
        binding.input.addTextChangedListener(object : TextWatcher {
            private var beforeTextLength = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                beforeTextLength = s?.length ?: 0
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateResultDisplay()
                /*val afterTextLength = s?.length ?: 0
                // If the afterTextLength is equals to 0 we have to clear resultDisplay
                if (afterTextLength == 0) {
                    binding.resultDisplay.setText("")
                }

                /* we check if the length of the text entered into the EditText
                is greater than the length of the text before the change (beforeTextLength)
                by more than 1 character. If it is, we assume that this is a paste event. */
                val clipData = clipboardManager.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    //val clipText = clipData.getItemAt(0).coerceToText(this@MainActivity).toString()

                    if (s != null) {
                        //val newValue = s.subSequence(start, start + count).toString()
                        if (
                            (afterTextLength - beforeTextLength > 1)
                            // Removed to avoid anoying notification (https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#PastingSystemNotifications)
                            //|| (afterTextLength - beforeTextLength >= 1 && clipText == newValue) // Supports 1+ new caractere if it is equals to the latest element from the clipboard
                        ) {
                            // Handle paste event here
                            updateResultDisplay()
                        }
                    }
                }*/
            }

            override fun afterTextChanged(s: Editable?) {
                // Do nothing
            }
        })
    }

    fun openAppMenu(view: View) {
        val popup = PopupMenu(this, view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.app_menu, popup.menu)
        popup.show()
    }

    fun openAbout(menuItem: MenuItem) {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent, null)
    }

    fun openSettings(menuItem: MenuItem) {
        val intent = Intent(this, SettingsActivity::class.java)
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
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
        }
    }

    private fun setErrorColor(errorStatus: Boolean) {
        // Only run if the color needs to be updated
        if (errorStatus != errorStatusOld) {
            // Set error color
            if (errorStatus) {
                binding.input.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.calculation_error_color
                    )
                )
                binding.resultDisplay.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.calculation_error_color
                    )
                )
            }
            // Clear error color
            else {
                binding.input.setTextColor(ContextCompat.getColor(this, R.color.text_color))
                binding.resultDisplay.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.text_second_color
                    )
                )
            }
            errorStatusOld = errorStatus
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

            var newValueFormatted =
                NumberFormatter.format(newValue, decimalSeparatorSymbol, groupingSeparatorSymbol)

            withContext(Dispatchers.Main) {
                // Avoid two decimalSeparator in the same number
                // 1. When you click on the decimalSeparator button
                if (value == decimalSeparatorSymbol && decimalSeparatorSymbol in binding.input.text.toString()) {
                    if (binding.input.text.toString().isNotEmpty()) {
                        var lastNumberBefore = ""
                        if (cursorPosition > 0 && binding.input.text.toString()
                                .substring(0, cursorPosition)
                                .last() in "0123456789\\$decimalSeparatorSymbol"
                        ) {
                            lastNumberBefore = NumberFormatter.extractNumbers(
                                binding.input.text.toString().substring(0, cursorPosition),
                                decimalSeparatorSymbol
                            ).last()
                        }
                        var firstNumberAfter = ""
                        if (cursorPosition < binding.input.text.length - 1) {
                            firstNumberAfter = NumberFormatter.extractNumbers(
                                binding.input.text.toString()
                                    .substring(cursorPosition, binding.input.text.length),
                                decimalSeparatorSymbol
                            ).first()
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
                    if (NumberFormatter.extractNumbers(value, decimalSeparatorSymbol)
                            .isNotEmpty()
                    ) {
                        val firstValueNumber =
                            NumberFormatter.extractNumbers(value, decimalSeparatorSymbol).first()
                        val lastValueNumber =
                            NumberFormatter.extractNumbers(value, decimalSeparatorSymbol).last()
                        if (decimalSeparatorSymbol in firstValueNumber || decimalSeparatorSymbol in lastValueNumber) {
                            var numberBefore =
                                binding.input.text.toString().substring(0, cursorPosition)
                            if (numberBefore.last() !in "()*-/+^!√πe") {
                                numberBefore = NumberFormatter.extractNumbers(
                                    numberBefore,
                                    decimalSeparatorSymbol
                                ).last()
                            }
                            var numberAfter = ""
                            if (cursorPosition < binding.input.text.length - 1) {
                                numberAfter = NumberFormatter.extractNumbers(
                                    binding.input.text.toString()
                                        .substring(cursorPosition, binding.input.text.length),
                                    decimalSeparatorSymbol
                                ).first()
                            }
                            var tmpValue = value
                            var numberBeforeParenthesisLength = 0
                            if (decimalSeparatorSymbol in numberBefore) {
                                numberBefore = "($numberBefore)"
                                numberBeforeParenthesisLength += 2
                            }
                            if (decimalSeparatorSymbol in numberAfter) {
                                tmpValue = "($value)"
                            }
                            val tmpNewValue = binding.input.text.toString().substring(
                                0,
                                (cursorPosition + numberBeforeParenthesisLength - numberBefore.length)
                            ) + numberBefore + tmpValue + rightValue
                            newValueFormatted = NumberFormatter.format(
                                tmpNewValue,
                                decimalSeparatorSymbol,
                                groupingSeparatorSymbol
                            )
                        }
                    }
                }

                // Update Display
                binding.input.setText(newValueFormatted)

                // Increase cursor position
                val cursorOffset = newValueFormatted.length - newValue.length
                binding.input.setSelection(cursorPosition + value.length + cursorOffset)
            }
        }
    }

    private fun roundResult(result: BigDecimal): BigDecimal {
        val numberPrecision = MyPreferences(this).numberPrecision!!.toInt()
        var newResult = result.setScale(numberPrecision, RoundingMode.HALF_EVEN)
        if (MyPreferences(this).numberIntoScientificNotation && (newResult >= BigDecimal(9999) || newResult <= BigDecimal(
                0.1
            ))
        ) {
            val scientificString = String.format(Locale.US, "%.4g", result)
            newResult = BigDecimal(scientificString)
        }

        // Fix how is displayed 0 with BigDecimal
        val tempResult = newResult.toString().replace("E-", "").replace("E", "")
        val allCharsEqualToZero = tempResult.all { it == '0' }
        if (
            allCharsEqualToZero
            || newResult.toString().startsWith("0E")
        ) {
            return BigDecimal.ZERO
        }

        return newResult
    }

    private fun enableOrDisableScientistMode() {
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

    // Switch between degree and radian mode
    private fun toggleDegreeMode() {
        if (isDegreeModeActivated) binding.degreeButton.text = getString(R.string.radian)
        else binding.degreeButton.text = getString(R.string.degree)

        binding.degreeTextView.text = binding.degreeButton.text

        // Flip the variable afterwards
        isDegreeModeActivated = !isDegreeModeActivated
    }

    private fun updateResultDisplay() {
        lifecycleScope.launch(Dispatchers.Default) {
            // Reset text color
            setErrorColor(false)

            val calculation = binding.input.text.toString()

            if (calculation != "") {
                division_by_0 = false
                domain_error = false
                syntax_error = false
                is_infinity = false
                require_real_number = false

                val calculationTmp = Expression().getCleanExpression(
                    binding.input.text.toString(),
                    decimalSeparatorSymbol,
                    groupingSeparatorSymbol
                )
                calculationResult =
                    Calculator(MyPreferences(this@MainActivity).numberPrecision!!.toInt()).evaluate(
                        calculationTmp,
                        isDegreeModeActivated
                    )

                // If result is a number and it is finite
                if (!(division_by_0 || domain_error || syntax_error || is_infinity || require_real_number)) {

                    // Round
                    calculationResult = roundResult(calculationResult)
                    var formattedResult = NumberFormatter.format(
                        calculationResult.toString().replace(".", decimalSeparatorSymbol),
                        decimalSeparatorSymbol,
                        groupingSeparatorSymbol
                    )

                    // Remove zeros at the end of the results (after point)
                    if (!MyPreferences(this@MainActivity).numberIntoScientificNotation || !(calculationResult >= BigDecimal(
                            9999
                        ) || calculationResult <= BigDecimal(0.1))
                    ) {
                        val resultSplited = calculationResult.toString().split('.')
                        if (resultSplited.size > 1) {
                            val resultPartAfterDecimalSeparator = resultSplited[1].trimEnd('0')
                            var resultWithoutZeros = resultSplited[0]
                            if (resultPartAfterDecimalSeparator != "") {
                                resultWithoutZeros =
                                    resultSplited[0] + "." + resultPartAfterDecimalSeparator
                            }
                            formattedResult = NumberFormatter.format(
                                resultWithoutZeros.replace(
                                    ".",
                                    decimalSeparatorSymbol
                                ), decimalSeparatorSymbol, groupingSeparatorSymbol
                            )
                        }
                    }


                    withContext(Dispatchers.Main) {
                        if (formattedResult != calculation) {
                            binding.resultDisplay.text = formattedResult
                        } else {
                            binding.resultDisplay.text = ""
                        }
                    }

                } else withContext(Dispatchers.Main) {
                    if (is_infinity && !division_by_0 && !domain_error && !require_real_number) {
                        if (calculationResult < BigDecimal.ZERO) binding.resultDisplay.text = "-" + getString(
                            R.string.infinity
                        )
                        else binding.resultDisplay.text = getString(R.string.value_too_large)
                    } else {
                        withContext(Dispatchers.Main) {
                            binding.resultDisplay.text = ""
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    binding.resultDisplay.text = ""
                }
            }
        }
    }

    fun keyDigitPadMappingToDisplay(view: View) {
        updateDisplay(view, (view as Button).text as String)
    }

    private fun addSymbol(view: View, currentSymbol: String) {
        // Get input text length
        val textLength = binding.input.text.length

        // If the input is not empty
        if (textLength > 0) {
            // Get cursor's current position
            val cursorPosition = binding.input.selectionStart

            // Get next / previous characters relative to the cursor
            val nextChar =
                if (textLength - cursorPosition > 0) binding.input.text[cursorPosition].toString() else "0" // use "0" as default like it's not a symbol
            val previousChar =
                if (cursorPosition > 0) binding.input.text[cursorPosition - 1].toString() else "0"

            if (currentSymbol != previousChar // Ignore multiple presses of the same button
                && currentSymbol != nextChar
                && previousChar != "√" // No symbol can be added on an empty square root
                && previousChar != decimalSeparatorSymbol // Ensure that the previous character is not a comma
                && nextChar != decimalSeparatorSymbol // Ensure that the next character is not a comma
                && (previousChar != "(" // Ensure that we are not at the beginning of a parenthesis
                        || currentSymbol == "-")
            ) { // Minus symbol is an override
                // If previous character is a symbol, replace it
                if (previousChar.matches("[+\\-÷×^]".toRegex())) {
                    keyVibration(view)

                    val leftString =
                        binding.input.text.subSequence(0, cursorPosition - 1).toString()
                    val rightString =
                        binding.input.text.subSequence(cursorPosition, textLength).toString()

                    // Add a parenthesis if there is another symbol before minus
                    if (currentSymbol == "-") {
                        if (previousChar in "+-") {
                            binding.input.setText(leftString + currentSymbol + rightString)
                            binding.input.setSelection(cursorPosition)
                        } else {
                            binding.input.setText(leftString + previousChar + currentSymbol + rightString)
                            binding.input.setSelection(cursorPosition + 1)
                        }
                    } else if (cursorPosition > 1 && binding.input.text[cursorPosition - 2] != '(') {
                        binding.input.setText(leftString + currentSymbol + rightString)
                        binding.input.setSelection(cursorPosition)
                    } else if (currentSymbol == "+") {
                        binding.input.setText(leftString + rightString)
                        binding.input.setSelection(cursorPosition - 1)
                    }
                }
                // If next character is a symbol, replace it
                else if (nextChar.matches("[+\\-÷×^%!]".toRegex())
                    && currentSymbol != "%"
                ) { // Make sure that percent symbol doesn't replace succeeding symbols
                    keyVibration(view)

                    val leftString = binding.input.text.subSequence(0, cursorPosition).toString()
                    val rightString =
                        binding.input.text.subSequence(cursorPosition + 1, textLength).toString()

                    if (cursorPosition > 0 && previousChar != "(") {
                        binding.input.setText(leftString + currentSymbol + rightString)
                        binding.input.setSelection(cursorPosition + 1)
                    } else if (currentSymbol == "+") binding.input.setText(leftString + rightString)
                }
                // Otherwise just update the display
                else if (cursorPosition > 0 || nextChar != "0" && currentSymbol == "-") {
                    updateDisplay(view, currentSymbol)
                } else keyVibration(view)
            } else keyVibration(view)
        } else { // Allow minus symbol, even if the input is empty
            if (currentSymbol == "-") updateDisplay(view, currentSymbol)
            else keyVibration(view)
        }
    }

    fun addButton(view: View) {
        addSymbol(view, "+")
    }

    fun subtractButton(view: View) {
        addSymbol(view, "-")
    }

    fun divideButton(view: View) {
        addSymbol(view, "÷")
    }

    fun multiplyButton(view: View) {
        addSymbol(view, "×")
    }

    fun exponentButton(view: View) {
        addSymbol(view, "^")
    }

    fun pointButton(view: View) {
        updateDisplay(view, decimalSeparatorSymbol)
    }

    fun sineButton(view: View) {
        if (!isInvButtonClicked) {
            updateDisplay(view, "sin(")
        } else {
            updateDisplay(view, "sin⁻¹(")
        }
    }

    fun cosineButton(view: View) {
        if (!isInvButtonClicked) {
            updateDisplay(view, "cos(")
        } else {
            updateDisplay(view, "cos⁻¹(")
        }
    }

    fun tangentButton(view: View) {
        if (!isInvButtonClicked) {
            updateDisplay(view, "tan(")
        } else {
            updateDisplay(view, "tan⁻¹(")
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
        addSymbol(view, "!")
    }

    fun squareButton(view: View) {
        if (!isInvButtonClicked) {
            updateDisplay(view, "√")
        } else {
            if (MyPreferences(this).addModuloButton) {
                updateDisplay(view, "#")
            } else {
                updateDisplay(view, "^2")
            }

        }
    }

    fun percent(view: View) {
        addSymbol(view, "%")
    }

    @SuppressLint("SetTextI18n")
    fun degreeButton(view: View) {
        keyVibration(view)
        toggleDegreeMode()
        updateResultDisplay()
    }

    fun invButton(view: View) {
        keyVibration(view)

        if (!isInvButtonClicked) {
            isInvButtonClicked = true

            // change buttons
            binding.sineButton.setText(R.string.sineInv)
            binding.cosineButton.setText(R.string.cosineInv)
            binding.tangentButton.setText(R.string.tangentInv)
            binding.naturalLogarithmButton.setText(R.string.naturalLogarithmInv)
            binding.logarithmButton.setText(R.string.logarithmInv)
            if (MyPreferences(this).addModuloButton) {
                binding.squareButton.setText(R.string.squareInvModuloVersion)
            } else {
                binding.squareButton.setText(R.string.squareInv)
            }

        } else {
            isInvButtonClicked = false

            // change buttons
            binding.sineButton.setText(R.string.sine)
            binding.cosineButton.setText(R.string.cosine)
            binding.tangentButton.setText(R.string.tangent)
            binding.naturalLogarithmButton.setText(R.string.naturalLogarithm)
            binding.logarithmButton.setText(R.string.logarithm)
            binding.squareButton.setText(R.string.square)
        }
    }

    fun clearButton(view: View) {
        keyVibration(view)
        binding.input.setText("")
        binding.resultDisplay.text = ""
    }

    @SuppressLint("SetTextI18n")
    fun equalsButton(view: View) {
        lifecycleScope.launch(Dispatchers.Default) {
            keyVibration(view)

            val calculation = binding.input.text.toString()

            if (calculation != "") {

                val resultString = calculationResult.toString()
                var formattedResult = NumberFormatter.format(
                    resultString.replace(".", decimalSeparatorSymbol),
                    decimalSeparatorSymbol,
                    groupingSeparatorSymbol
                )

                // If result is a number and it is finite
                if (!(division_by_0 || domain_error || syntax_error || is_infinity || require_real_number)) {

                    // Remove zeros at the end of the results (after point)
                    val resultSplited = resultString.split('.')
                    if (resultSplited.size > 1) {
                        val resultPartAfterDecimalSeparator = resultSplited[1].trimEnd('0')
                        var resultWithoutZeros = resultSplited[0]
                        if (resultPartAfterDecimalSeparator != "") {
                            resultWithoutZeros =
                                resultSplited[0] + "." + resultPartAfterDecimalSeparator
                        }
                        formattedResult = NumberFormatter.format(
                            resultWithoutZeros.replace(
                                ".",
                                decimalSeparatorSymbol
                            ), decimalSeparatorSymbol, groupingSeparatorSymbol
                        )
                    }

                    // Hide the cursor before updating binding.input to avoid weird cursor movement
                    withContext(Dispatchers.Main) {
                        binding.input.isCursorVisible = false
                    }

                    // Display result
                    withContext(Dispatchers.Main) { binding.input.setText(formattedResult) }

                    // Set cursor
                    withContext(Dispatchers.Main) {
                        // Scroll to the end
                        binding.input.setSelection(binding.input.length())

                        // Hide the cursor (do not remove this, it's not a duplicate)
                        binding.input.isCursorVisible = false

                        // Clear resultDisplay
                        binding.resultDisplay.text = ""
                    }

                    if (calculation != formattedResult) {
                        val history = MyPreferences(this@MainActivity).getHistory()

                        // Do not save to history if the previous entry is the same as the current one
                        if (history.isEmpty() || history[history.size - 1].calculation != calculation) {
                            // Store time
                            val currentTime = System.currentTimeMillis().toString()

                            // Save to history
                            history.add(
                                History(
                                    calculation = calculation,
                                    result = formattedResult,
                                    time = currentTime,
                                )
                            )

                            MyPreferences(this@MainActivity).saveHistory(this@MainActivity, history)

                            // Update history variables
                            withContext(Dispatchers.Main) {
                                historyAdapter.appendOneHistoryElement(
                                    History(
                                        calculation = calculation,
                                        result = formattedResult,
                                        time = currentTime,
                                    )
                                )

                                // Remove former results if > historySize preference
                                val historySize =
                                    MyPreferences(this@MainActivity).historySize!!.toInt()
                                while (historySize != -1 && historyAdapter.itemCount >= historySize && historyAdapter.itemCount > 0) {
                                    historyAdapter.removeFirstHistoryElement()
                                }

                                // Scroll to the bottom of the recycle view
                                binding.historyRecylcleView.scrollToPosition(historyAdapter.itemCount - 1)
                            }
                        }
                    }
                    isEqualLastAction = true
                } else {
                    withContext(Dispatchers.Main) {
                        if (syntax_error) {
                            setErrorColor(true)
                            binding.resultDisplay.text = getString(R.string.syntax_error)
                        } else if (domain_error) {
                            setErrorColor(true)
                            binding.resultDisplay.text = getString(R.string.domain_error)
                        } else if (require_real_number) {
                            setErrorColor(true)
                            binding.resultDisplay.text = getString(R.string.require_real_number)
                        } else if (division_by_0) {
                            setErrorColor(true)
                            binding.resultDisplay.text = getString(R.string.division_by_0)
                        } else if (is_infinity) {
                            if (calculationResult < BigDecimal.ZERO) binding.resultDisplay.text = "-" + getString(
                                R.string.infinity
                            )
                            else binding.resultDisplay.text = getString(R.string.value_too_large)
                            //} else if (result.isNaN()) {
                            //    setErrorColor(true)
                            //    binding.resultDisplay.setText(getString(R.string.math_error))
                        } else {
                            binding.resultDisplay.text = formattedResult
                            isEqualLastAction =
                                true // Do not clear the calculation (if you click into a number) if there is an error
                        }
                    }
                }

            } else {
                withContext(Dispatchers.Main) { binding.resultDisplay.text = "" }
            }
        }
    }

    fun leftParenthesisButton(view: View) {
        updateDisplay(view, "(")
    }

    fun rightParenthesisButton(view: View) {
        updateDisplay(view, ")")
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

        if (
            !(textLength > cursorPosition && binding.input.text.toString()[cursorPosition] in "×÷+-^")
            && (
                    openParentheses == closeParentheses
                            || binding.input.text.toString()[cursorPosition - 1] == '('
                            || binding.input.text.toString()[cursorPosition - 1] in "×÷+-^"
                    )
        ) {
            updateDisplay(view, "(")
        } else {
            updateDisplay(view, ")")
        }
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
            val functionsList =
                listOf("cos⁻¹(", "sin⁻¹(", "tan⁻¹(", "cos(", "sin(", "tan(", "ln(", "log(", "exp(")
            for (function in functionsList) {
                val leftPart = binding.input.text.subSequence(0, cursorPosition).toString()
                if (leftPart.endsWith(function)) {
                    newValue = binding.input.text.subSequence(0, cursorPosition - function.length)
                        .toString() +
                            binding.input.text.subSequence(cursorPosition, textLength).toString()
                    isFunction = true
                    functionLength = function.length - 1
                    break
                }
            }
            // Else
            if (!isFunction) {
                // remove the grouping separator
                val leftPart = binding.input.text.subSequence(0, cursorPosition).toString()
                val leftPartWithoutSpaces = leftPart.replace(groupingSeparatorSymbol, "")
                functionLength = leftPart.length - leftPartWithoutSpaces.length

                newValue = leftPartWithoutSpaces.subSequence(0, leftPartWithoutSpaces.length - 1)
                    .toString() +
                        binding.input.text.subSequence(cursorPosition, textLength).toString()
            }

            val newValueFormatted =
                NumberFormatter.format(newValue, decimalSeparatorSymbol, groupingSeparatorSymbol)
            var cursorOffset = newValueFormatted.length - newValue.length
            if (cursorOffset < 0) cursorOffset = 0

            binding.input.setText(newValueFormatted)
            binding.input.setSelection((cursorPosition - 1 + cursorOffset - functionLength).takeIf { it > 0 }
                ?: 0)
        }
    }

    fun scientistModeSwitchButton(view: View) {
        enableOrDisableScientistMode()
    }

    // Update settings
    override fun onResume() {
        super.onResume()

        // Update the theme
        val themes = Themes(this)
        if (currentTheme != themes.getTheme()) {
            (this as Activity).finish()
            ContextCompat.startActivity(this, this.intent, null)
        }

        if (appLanguage != Locale.getDefault()) {
            appLanguage = Locale.getDefault()
            // Clear inputs to avoid conflicts with decimal & grouping separators
            binding.input.setText("")
            binding.resultDisplay.text = ""
        }

        // Split the parentheses button (if option is enabled)
        if (MyPreferences(this).splitParenthesisButton) {
            // Hide the AC button
            binding.clearButton.visibility = View.GONE
            binding.parenthesesButton.visibility = View.GONE

            // Unhide the left & right parenthesis buttons
            binding.leftParenthesisButton?.visibility = View.VISIBLE
            binding.rightParenthesisButton?.visibility = View.VISIBLE
        } else {
            // Unhide the AC button
            binding.clearButton.visibility = View.VISIBLE
            binding.parenthesesButton.visibility = View.VISIBLE

            // Hide the left & right parenthesis buttons
            binding.leftParenthesisButton?.visibility = View.GONE
            binding.rightParenthesisButton?.visibility = View.GONE
        }

        // Prevent phone from sleeping while the app is in foreground
        view.keepScreenOn = MyPreferences(this).preventPhoneFromSleepingMode

        // Remove former results if > historySize preference
        // Remove from the RecycleView
        val historySize = MyPreferences(this@MainActivity).historySize!!.toInt()
        while (historySize != -1 && historyAdapter.itemCount >= historySize && historyAdapter.itemCount > 0) {
            historyAdapter.removeFirstHistoryElement()
        }
        // Remove from the preference store data
        val history = MyPreferences(this@MainActivity).getHistory()
        while (historySize > 0 && history.size > historySize) {
            history.removeAt(0)
        }
        MyPreferences(this@MainActivity).saveHistory(this@MainActivity, history)

        // Disable history if setting enabled
        if (historySize == 0) {
            binding.historyRecylcleView.visibility = View.GONE
            binding.slidingLayoutButton.visibility = View.GONE
            binding.slidingLayout.isEnabled = false
        } else {
            binding.historyRecylcleView.visibility = View.VISIBLE
            binding.slidingLayoutButton.visibility = View.VISIBLE
            binding.slidingLayout.isEnabled = true
        }

        // Disable the keyboard on display EditText
        binding.input.showSoftInputOnFocus = false
    }
}