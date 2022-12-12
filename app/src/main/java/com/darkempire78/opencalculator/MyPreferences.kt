package com.darkempire78.opencalculator

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.preference.PreferenceManager
import com.google.gson.Gson

class MyPreferences(context: Context) {

    // https://proandroiddev.com/dark-mode-on-android-app-with-kotlin-dc759fc5f0e1
    companion object {
        private const val THEME = "darkempire78.opencalculator.THEME"
        private const val FORCE_DAY_NIGHT = "darkempire78.opencalculator.FORCE_DAY_NIGHT"

        private const val KEY_VIBRATION_STATUS = "darkempire78.opencalculator.KEY_VIBRATION_STATUS"
        private const val KEY_HISTORY = "darkempire78.opencalculator.HISTORY"
    }

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    var theme = preferences.getInt(THEME, 0)
        set(value) = preferences.edit().putInt(THEME, value).apply()
    var forceDayNight = preferences.getInt(FORCE_DAY_NIGHT, MODE_NIGHT_UNSPECIFIED)
        set(value) = preferences.edit().putInt(FORCE_DAY_NIGHT, value).apply()

    var vibrationMode = preferences.getBoolean(KEY_VIBRATION_STATUS, true)
        set(value) = preferences.edit().putBoolean(KEY_VIBRATION_STATUS, value).apply()
    private var history = preferences.getString(KEY_HISTORY, null)
        set(value) = preferences.edit().putString(KEY_HISTORY, value).apply()

    fun getHistory(): MutableList<History> {
        val gson = Gson()
        return if (preferences.getString(KEY_HISTORY, null) != null) {
            gson.fromJson(history, Array<History>::class.java).asList().toMutableList()
        } else {
            mutableListOf()
        }
    }

    fun saveHistory(context: Context, history: List<History>){
        val gson = Gson()
        val history2 = history.toMutableList()
        if (history2.size > 50) {
            history2.removeAt(0)
        }
        MyPreferences(context).history = gson.toJson(history2) // Convert to json
    }
}
