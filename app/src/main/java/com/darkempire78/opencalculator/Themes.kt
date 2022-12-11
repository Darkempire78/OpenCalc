package com.darkempire78.opencalculator

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat.startActivity
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Themes(private val context: Context) {

    companion object {

        // Themes
        private const val DEFAULT_THEME_INDEX = 0
        const val AMOLED_THEME_INDEX = 1
        private const val MATERIAL_YOU_THEME_INDEX = 2

        // Styles - Combinations of theme + day/night mode
        private const val SYSTEM_STYLE_INDEX = 0
        private const val LIGHT_STYLE_INDEX = 1
        private const val DARK_STYLE_INDEX = 2
        private const val AMOLED_STYLE_INDEX = 3
        private const val MATERIAL_YOU_STYLE_INDEX = 4

        private val themeMap = mapOf(
            DEFAULT_THEME_INDEX to R.style.AppTheme,
            AMOLED_THEME_INDEX to R.style.AmoledTheme,
            MATERIAL_YOU_THEME_INDEX to R.style.MaterialYouTheme
        )

        fun openDialogThemeSelector(context: Context) {

            val preferences = MyPreferences(context)

            val builder = MaterialAlertDialogBuilder(context)

            val styles =  hashMapOf(
                SYSTEM_STYLE_INDEX to context.getString(R.string.theme_system),
                LIGHT_STYLE_INDEX to context.getString(R.string.theme_light),
                DARK_STYLE_INDEX to context.getString(R.string.theme_dark),
                AMOLED_STYLE_INDEX to context.getString(R.string.theme_amoled)
            )
            if (DynamicColors.isDynamicColorAvailable())
                styles[MATERIAL_YOU_STYLE_INDEX] = context.getString(R.string.theme_material_you)

            val checkedItem = when (preferences.theme) {
                AMOLED_THEME_INDEX -> AMOLED_STYLE_INDEX
                MATERIAL_YOU_THEME_INDEX -> MATERIAL_YOU_STYLE_INDEX
                else -> {
                    when (preferences.forceDayNight) {
                        AppCompatDelegate.MODE_NIGHT_NO -> LIGHT_STYLE_INDEX
                        AppCompatDelegate.MODE_NIGHT_YES -> DARK_STYLE_INDEX
                        else -> SYSTEM_STYLE_INDEX
                    }
                }
            }

            builder.setSingleChoiceItems(styles.values.toTypedArray(), checkedItem) { dialog, which ->
                when (which) {
                    0 -> {
                        preferences.theme = DEFAULT_THEME_INDEX
                        preferences.forceDayNight = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                    1 -> {
                        preferences.theme = DEFAULT_THEME_INDEX
                        preferences.forceDayNight = AppCompatDelegate.MODE_NIGHT_NO
                    }
                    2 -> {
                        preferences.theme = DEFAULT_THEME_INDEX
                        preferences.forceDayNight = AppCompatDelegate.MODE_NIGHT_YES
                    }
                    3 -> {
                        preferences.theme = AMOLED_THEME_INDEX
                    }
                    4 -> {
                        preferences.theme = MATERIAL_YOU_THEME_INDEX
                        preferences.forceDayNight = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                }
                dialog.dismiss()
                reloadActivity(context)
            }
            val dialog = builder.create()
            dialog.show()
        }

        private fun reloadActivity(context: Context) {
            (context as Activity).finish()
            startActivity(context, context.intent, null)
        }
    }

    fun applyDayNightOverride() {
        val preferences = MyPreferences(context)
        if (preferences.forceDayNight != AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
                && preferences.theme != AMOLED_THEME_INDEX) {
            AppCompatDelegate.setDefaultNightMode(preferences.forceDayNight)
        }
    }

    fun getTheme(): Int = themeMap[MyPreferences(context).theme] ?: R.style.AppTheme
}
