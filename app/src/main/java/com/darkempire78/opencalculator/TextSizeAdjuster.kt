package com.darkempire78.opencalculator

import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.util.TypedValue
import android.widget.TextView

class TextSizeAdjuster(private val context: Context) {

    enum class AdjustableTextType {
        Input,
        Output,
    }

    fun adjustTextSize(textView: TextView, adjustableTextType: AdjustableTextType) {
        val screenWidth = context.resources.displayMetrics.widthPixels

        // Text size will be reduced a bit before reaching the screen width, for a smoother experience
        val maxWidth = screenWidth - dpToPx(20f)

        // Get the min and max text sizes
        val (minTextSize, maxTextSize) = getTextSizeBounds(context.resources.configuration, adjustableTextType)

        var textSize = maxTextSize
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)

        val textBounds = Rect()
        val text = textView.text.toString()

        // Measure the text size
        val paint = textView.paint
        paint.getTextBounds(text, 0, text.length, textBounds)

        // Reduce the text size until it fits
        while (textBounds.width() > maxWidth && textSize > minTextSize) {
            textSize -= 1f
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
            paint.getTextBounds(text, 0, text.length, textBounds)
        }
    }

    private fun getTextSizeBounds(configuration: Configuration, adjustableTextType: AdjustableTextType): Pair<Float, Float> {
        val orientation = configuration.orientation
        val screenSize = configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK

        val (minTextSize, maxTextSize) = if (adjustableTextType == AdjustableTextType.Input) {
            getInputTextSizeBounds(orientation, screenSize)
        } else {
            getResultTextSizeBounds(orientation, screenSize)
        }

        return Pair(minTextSize, maxTextSize)
    }

    private fun getInputTextSizeBounds(orientation: Int, screenSize: Int): Pair<Float, Float> {
        return when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                when (screenSize) {
                    Configuration.SCREENLAYOUT_SIZE_SMALL -> Pair(40f, 85f) // TODO: Find the right values
                    Configuration.SCREENLAYOUT_SIZE_NORMAL -> Pair(40f, 85f) // TODO: Find the right values
                    Configuration.SCREENLAYOUT_SIZE_LARGE -> Pair(35f, 85f) // TODO: Find the right values
                    Configuration.SCREENLAYOUT_SIZE_XLARGE -> Pair(35f, 85f) // TODO: Find the right values
                    else -> Pair(40f, 85f) // TODO: Find the right values
                }
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                when (screenSize) {
                    Configuration.SCREENLAYOUT_SIZE_SMALL -> Pair(35f, 85f) // TODO: Find the right values
                    Configuration.SCREENLAYOUT_SIZE_NORMAL -> Pair(25f, 45f)
                    Configuration.SCREENLAYOUT_SIZE_LARGE -> Pair(35f, 85f) // TODO: Find the right values
                    Configuration.SCREENLAYOUT_SIZE_XLARGE -> Pair(35f, 85f) // TODO: Find the right values
                    else -> Pair(25f, 45f) // TODO: Find the right values
                }
            }
            Configuration.ORIENTATION_UNDEFINED -> {
                println("❌ Undefined orientation : screenSize -> $screenSize orientation -> $orientation")
                Pair(0f, 0f)
            }
            else -> {
                println("❌ Undefined orientation (else) : screenSize -> $screenSize orientation -> $orientation")
                Pair(0f, 0f)
            }
        }
    }

    private fun getResultTextSizeBounds(orientation: Int, screenSize: Int): Pair<Float, Float> {
        return when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                when (screenSize) {
                    Configuration.SCREENLAYOUT_SIZE_SMALL -> Pair(40f, 55f) // TODO: Find the right values
                    Configuration.SCREENLAYOUT_SIZE_NORMAL -> Pair(40f, 55f) // TODO: Find the right values
                    Configuration.SCREENLAYOUT_SIZE_LARGE -> Pair(35f, 55f) // TODO: Find the right values
                    Configuration.SCREENLAYOUT_SIZE_XLARGE -> Pair(35f, 55f) // TODO: Find the right values
                    else -> Pair(40f, 85f) // TODO: Find the right values
                }
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                when (screenSize) {
                    Configuration.SCREENLAYOUT_SIZE_SMALL -> Pair(35f, 55f) // TODO: Find the right values
                    Configuration.SCREENLAYOUT_SIZE_NORMAL -> Pair(25f, 55f)
                    Configuration.SCREENLAYOUT_SIZE_LARGE -> Pair(35f, 55f) // TODO: Find the right values
                    Configuration.SCREENLAYOUT_SIZE_XLARGE -> Pair(35f, 55f) // TODO: Find the right values
                    else -> Pair(25f, 45f) // TODO: Find the right values
                }
            }
            Configuration.ORIENTATION_UNDEFINED -> {
                println("❌ Undefined orientation : screenSize -> $screenSize orientation -> $orientation")
                Pair(0f, 0f)
            }
            else -> {
                println("❌ Undefined orientation (else) : screenSize -> $screenSize orientation -> $orientation")
                Pair(0f, 0f)
            }
        }
    }

    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}