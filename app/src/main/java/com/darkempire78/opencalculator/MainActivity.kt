package com.darkempire78.opencalculator

import android.animation.LayoutTransition
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import org.mariuszgromada.math.mxparser.Expression
import org.mariuszgromada.math.mxparser.mXparser


class MainActivity : AppCompatActivity() {

    // https://stackoverflow.com/questions/34197026/android-content-pm-applicationinfo-android-content-context-getapplicationinfo
    private val display: EditText
        get() = findViewById(R.id.input)
    private val resultDisplay: TextView
        get() = findViewById(R.id.resultDisplay)
    private val scientistModeRow2: TableRow
        get() = findViewById(R.id.scientistModeRow2)
    private val scientistModeRow3: TableRow
        get() = findViewById(R.id.scientistModeRow3)
    private val scientistModeSwitchButton: ImageButton
        get() = findViewById(R.id.scientistModeSwitchButton)
    private val degreeButton: Button
        get() = findViewById(R.id.degreeButton)
    private val degreeTextView: TextView
        get() = findViewById(R.id.DegreeTextView)
    private var isInvButtonClicked = false

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Themes
        Themes(this)

        // Check Vibration preference
        if (MyPreferences(this).vibrationMode) {
            //vibrationAppMenu.isChecked = true
        }

        when (MyPreferences(this).darkMode) {
            -1 -> {
                if (resources.configuration.isNightModeActive) {
                    setTheme(R.style.darkTheme)
                } else {
                    setTheme(R.style.AppTheme)
                }
            }
            1 -> {
                setTheme(R.style.darkTheme)
            }
            2 -> {
                setTheme(R.style.amoledTheme)
            }
            else -> {
                if (resources.configuration.isNightModeActive) {
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
        display.showSoftInputOnFocus = false

        // Long press backspace button
        val backspaceButton = findViewById<ImageButton>(R.id.backspaceButton)

        // https://www.geeksforgeeks.org/how-to-detect-long-press-in-android/
        backspaceButton.setOnLongClickListener {
            display.setText("")
            resultDisplay.text = ""
            true
        }

        // Set degree by default
        mXparser.setDegreesMode()

        // Set default animations and disable the fade out default animation
        // https://stackoverflow.com/questions/19943466/android-animatelayoutchanges-true-what-can-i-do-if-the-fade-out-effect-is-un
        val tableLayout = findViewById<ViewGroup>(R.id.tableLayout)
        val lt = LayoutTransition()
        lt.disableTransitionType(LayoutTransition.DISAPPEARING)
        tableLayout.layoutTransition = lt
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun selectThemeDialog(menuItem: MenuItem) {
        Themes(this).openDialogThemeSelector()
    }

    fun openAppMenu(view: View) {
        val popup = PopupMenu(this, view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.app_menu, popup.menu)
        popup.menu.findItem(R.id.app_menu_vibration_button).isChecked = MyPreferences(this).vibrationMode;
        popup.show()
    }

    fun checkVibration(menuItem: MenuItem) {
        MyPreferences(this).vibrationMode = !menuItem.isChecked
    }

    fun openGithubLink(menuItem: MenuItem) {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://github.com/Darkempire78/OpenCalc")
        )
        startActivity(browserIntent)
    }

    private fun updateDisplay(view: View, value: String) {
        // Vibrate when key pressed
        if (MyPreferences(this).vibrationMode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS)
            }
        }

        
        val formerValue = display.text.toString()
        val cursorPosition = display.selectionStart
        val leftValue = formerValue.subSequence(0, cursorPosition).toString()
        val rightValue = formerValue.subSequence(cursorPosition, formerValue.length).toString()

        val newValue = leftValue + value + rightValue

        // Update Display
        display.setText(newValue)

        // Increase cursor position
        display.setSelection(cursorPosition + value.length)

        // Update resultDisplay
        updateResultDisplay()
    }

    private fun updateResultDisplay() {
        var calculation = display.text.toString()

        if (calculation != "") {
            calculation = calculation.replace('×', '*')
            calculation = calculation.replace('÷', '/')
            calculation = calculation.replace("log", "log10")

            // Add ")" which lack
            var openParentheses = 0
            var closeParentheses = 0

            for (i in 0..calculation.length-1) {
                if (calculation[i] == '(') {
                    openParentheses += 1
                }
                if (calculation[i] == ')') {
                    closeParentheses += 1
                }
            }
            if (closeParentheses < openParentheses) {
                for (i in 0..openParentheses-closeParentheses-1) {
                    calculation += ')'
                }
            }

            val exp = Expression(calculation)
            var result = exp.calculate().toString()

            if (result != "NaN" && result != "Infinity") {
                // If the double ends with .0 we remove the .0
                if ((exp.calculate() * 10) % 10 == 0.0) {
                    result = String.format("%.0f", exp.calculate())
                    if (result != calculation) {
                        resultDisplay.text = result
                    } else {
                        resultDisplay.text = ""
                    }
                } else {
                    if (result != calculation) {
                        resultDisplay.text = result
                    } else {
                        resultDisplay.text = ""
                    }
                }
            } else if (result == "Infinity") {
                resultDisplay.text = "Infinity"
            } else {
                resultDisplay.text = ""
            }
        } else {
            resultDisplay.text = ""
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
        if (!isInvButtonClicked) {
            isInvButtonClicked = true

            // change buttons
            findViewById<Button>(R.id.sinusButton).setText(R.string.sinusInv)
            findViewById<Button>(R.id.cosinusButton).setText(R.string.cosinusInv)
            findViewById<Button>(R.id.tangentButton).setText(R.string.tangentInv)
            findViewById<Button>(R.id.naturalLogarithmButton).setText(R.string.naturalLogarithmInv)
            findViewById<Button>(R.id.logarithmButton).setText(R.string.logarithmInv)
            findViewById<Button>(R.id.squareButton).setText(R.string.squareInv)
        } else {
            isInvButtonClicked = false

            // change buttons
            findViewById<Button>(R.id.sinusButton).setText(R.string.sinus)
            findViewById<Button>(R.id.cosinusButton).setText(R.string.cosinus)
            findViewById<Button>(R.id.tangentButton).setText(R.string.tangent)
            findViewById<Button>(R.id.naturalLogarithmButton).setText(R.string.naturalLogarithm)
            findViewById<Button>(R.id.logarithmButton).setText(R.string.logarithm)
            findViewById<Button>(R.id.squareButton).setText(R.string.square)
        }
    }

    fun clearButton(view: View) {
        display.setText("")

        // Clear resultDisplay
        resultDisplay.text = ""
    }

    fun equalsButton(view: View) {
        var calculation = display.text.toString()
        calculation = calculation.replace('×', '*')
        calculation = calculation.replace('÷', '/')
        calculation = calculation.replace("log", "log10")

        if (calculation != "") {
            // Add ")" which lack
            var openParentheses = 0
            var closeParentheses = 0

            for (i in 0..calculation.length-1) {
                if (calculation[i] == '(') {
                    openParentheses += 1
                }
                if (calculation[i] == ')') {
                    closeParentheses += 1
                }
            }
            if (closeParentheses < openParentheses) {
                for (i in 0..openParentheses-closeParentheses-1) {
                    calculation += ')'
                }
            }

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
                resultDisplay.text = ""
            } else {
                resultDisplay.text = result
            }
        } else {
            resultDisplay.text = ""
        }
    }

    fun parenthesesButton(view: View) {
        val cursorPosition = display.selectionStart
        val textLength = display.text.length

        var openParentheses = 0
        var closeParentheses = 0

        val text = display.text.toString()

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
            updateDisplay(view, "(")
        } else if (closeParentheses < openParentheses && display.text.toString().subSequence(
                textLength - 1,
                textLength
            ) != "(") {
            updateDisplay(view, ")")
        }

        updateResultDisplay()
    }

    fun backspaceButton(view: View) {
        val cursorPosition = display.selectionStart
        val textLength = display.text.length

        if (cursorPosition != 0 && textLength != 0) {
            val newValue = display.text.subSequence(0, cursorPosition - 1).toString() + display.text.subSequence(
                cursorPosition,
                textLength
            ).toString()
            display.setText(newValue)

            display.setSelection(cursorPosition - 1)
        }

        updateResultDisplay()
    }

    fun scientistModeSwitchButton(view: View) {
        if(scientistModeRow2.visibility != View.VISIBLE)
        {
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