package com.darkempire78.opencalculator

import android.content.Context
import androidx.preference.PreferenceManager

class MyPreferences(context: Context?) {

    // https://proandroiddev.com/dark-mode-on-android-app-with-kotlin-dc759fc5f0e1
    companion object {
        private const val DARK_STATUS = "darkempire78.opencalculator.DARK_STATUS"
        private const val KEY_VIBRATION_STATUS = "darkempire78.opencalculator.KEY_VIBRATION_STATUS"
    }

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    var darkMode = preferences.getInt(DARK_STATUS, -1)
        set(value) = preferences.edit().putInt(DARK_STATUS, value).apply()
    var vibrationMode = preferences.getBoolean(KEY_VIBRATION_STATUS, true)
        set(value) = preferences.edit().putBoolean(KEY_VIBRATION_STATUS, value).apply()
}