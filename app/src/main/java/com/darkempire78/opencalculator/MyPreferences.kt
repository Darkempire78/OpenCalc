package com.darkempire78.opencalculator

import android.content.Context
import androidx.preference.PreferenceManager

class MyPreferences(context: Context?) {

    companion object {
        private const val DARK_STATUS = "io.github.manuelernesto.DARK_STATUS"
    }

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    var darkMode = preferences.getInt(DARK_STATUS, 1)
        set(value) = preferences.edit().putInt(DARK_STATUS, value).apply()

}