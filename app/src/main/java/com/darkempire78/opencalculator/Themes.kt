package com.darkempire78.opencalculator

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat.startActivity
import com.google.android.material.color.DynamicColors

class Themes(private val context: Context) {

    private val themeMap = mapOf(
        /* System */      -1 to
            if (DynamicColors.isDynamicColorAvailable()) {
                if (checkIfDarkModeIsEnabledByDefault()) R.style.materialYouDark else R.style.materialYouLight
            } else {
                if (checkIfDarkModeIsEnabledByDefault()) R.style.darkTheme else R.style.AppTheme
            },
        /* Light mode */   0 to R.style.AppTheme,
        /* Dark mode */    1 to R.style.darkTheme,
        /* amoled mode */  2 to R.style.amoledTheme,
        /* Material You */ 3 to if (checkIfDarkModeIsEnabledByDefault()) R.style.materialYouDark else R.style.materialYouLight
    )

    fun openDialogThemeSelector() {
        val builder = AlertDialog.Builder(context)

        val styles = arrayOf("System", "Light", "Dark", "Amoled", "Material You")
        val checkedItem = MyPreferences(context).darkMode + 1

        builder.setSingleChoiceItems(styles, checkedItem) { dialog, which ->
            when (which) {
                0 -> {
                    MyPreferences(context).darkMode = -1
                    dialog.dismiss()
                    reloadActivity()
                }
                1 -> {
                    MyPreferences(context).darkMode = 0
                    dialog.dismiss()
                    reloadActivity()
                }
                2 -> {
                    MyPreferences(context).darkMode = 1
                    dialog.dismiss()
                    reloadActivity()
                }
                3 -> {
                    MyPreferences(context).darkMode = 2
                    dialog.dismiss()
                    reloadActivity()
                }
                4 -> {
                    if (DynamicColors.isDynamicColorAvailable()) {
                        MyPreferences(context).darkMode = 3
                        dialog.dismiss()
                        reloadActivity()
                    } else {
                        Toast.makeText(context, "Sorry Material You isn't Available", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
            }
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun reloadActivity() {
        (context as Activity).finish()
        startActivity(context, context.intent, null)
    }

    fun checkTheme() {
        when (MyPreferences(context).darkMode) {
            -1 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            0 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            1 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            3 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    fun getTheme(): Int = themeMap[MyPreferences(context).darkMode]
        ?: if (checkIfDarkModeIsEnabledByDefault()) R.style.darkTheme else R.style.AppTheme


    private fun checkIfDarkModeIsEnabledByDefault(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.resources.configuration.isNightModeActive
        } else {
            when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> true
                Configuration.UI_MODE_NIGHT_NO -> false
                Configuration.UI_MODE_NIGHT_UNDEFINED -> true
                else -> true
            }
        }
}
