package com.darkempire78.opencalculator

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat.startActivity

class Themes(private val context: Context) {

    @RequiresApi(Build.VERSION_CODES.R)
    fun openDialogThemeSelector() {

        val builder = AlertDialog.Builder(context)
        //builder.setTitle(getString(R.string.select_theme_title))

        val styles = arrayOf("System", "Light", "Dark", "Amoled")
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
            }

        }

        val dialog = builder.create()
        dialog.show()
    }

    fun reloadActivity() {
        (context as Activity).finish()
        startActivity(context, context.intent, null);
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
        }
    }
}