package com.darkempire78.opencalculator

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat.startActivity
import com.google.android.material.color.DynamicColors

class Themes(private val context: Context) {

    fun openDialogThemeSelector() {
        val builder = AlertDialog.Builder(context)

        val styles = arrayOf("System", "Light", "Dark", "Amoled", "Material You")
        val checkedItem = MyPreferences(context).darkMode + 1

        builder.setSingleChoiceItems(styles, checkedItem) { dialog, which ->
            when (which) {
                0 -> {
                    MyPreferences(context).darkMode = -1
                    dialog.dismiss()
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
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
}
