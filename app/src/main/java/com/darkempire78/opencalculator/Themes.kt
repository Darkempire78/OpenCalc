package com.darkempire78.opencalculator

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat.startActivity
import com.darkempire78.opencalculator.databinding.DialogThemeSelectionBinding
import com.google.android.material.color.DynamicColors

class Themes(private val context: Context) {

    companion object {
        private const val DEFAULT_THEME_INDEX = 0
        const val AMOLED_THEME_INDEX = 1
        private const val MATERIAL_YOU_THEME_INDEX = 2

        private val themeMap = mapOf(
            DEFAULT_THEME_INDEX to R.style.AppTheme,
            AMOLED_THEME_INDEX to R.style.AmoledTheme,
            MATERIAL_YOU_THEME_INDEX to R.style.MaterialYouTheme
        )

        fun openDialogThemeSelector(context: Context, layoutInflater: LayoutInflater) {

            val preferences = MyPreferences(context)

            val builder = AlertDialog.Builder(context)

            val themes = arrayListOf(
                context.getString(R.string.theme_default),
                context.getString(R.string.theme_amoled),
            )
            if (DynamicColors.isDynamicColorAvailable())
                themes.add(context.getString(R.string.theme_material_you))

            val dayNightModes = mapOf(
                context.getString(R.string.theme_follow_system) to AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                context.getString(R.string.theme_dark) to AppCompatDelegate.MODE_NIGHT_YES,
                context.getString(R.string.theme_light) to AppCompatDelegate.MODE_NIGHT_NO,
            )
            val dayNightModeIndexes = mapOf(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM to 0,
                AppCompatDelegate.MODE_NIGHT_YES to 1,
                AppCompatDelegate.MODE_NIGHT_NO to 2
            )

            builder.setTitle(R.string.theme_dialog_title)

            var themeSelection = preferences.theme

            val dialogBinding = DialogThemeSelectionBinding.inflate(layoutInflater)
            val listTheme = dialogBinding.listTheme
            val spinnerDayNightOverride = dialogBinding.spinnerDayNightOverride

            val themeAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_single_choice, themes)
            listTheme.setOnItemClickListener { _, _, position, _ ->
                themeSelection = position
                spinnerDayNightOverride.isEnabled = position != AMOLED_THEME_INDEX
            }
            listTheme.adapter = themeAdapter
            listTheme.setItemChecked(themeSelection, true)

            val overrideAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, dayNightModes.keys.toTypedArray())
            spinnerDayNightOverride.adapter = overrideAdapter
            spinnerDayNightOverride.setSelection(dayNightModeIndexes[preferences.forceDayNight] ?: 0)

            builder.setView(dialogBinding.root)

            builder.setPositiveButton(R.string.apply) { dialog, _ ->
                preferences.theme = themeSelection
                val mode = dayNightModes[(spinnerDayNightOverride.selectedItem as String)] ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                preferences.forceDayNight = mode
                dialog.dismiss()
                reloadActivity(context)
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
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

    /*private fun checkIfDarkModeIsEnabledByDefault(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.resources.configuration.isNightModeActive
        } else {
            when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> true
                Configuration.UI_MODE_NIGHT_NO -> false
                Configuration.UI_MODE_NIGHT_UNDEFINED -> true
                else -> true
            }
        }*/
}
