package com.darkempire78.opencalculator

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson

class MyPreferences(context: Context) {

    // https://proandroiddev.com/dark-mode-on-android-app-with-kotlin-dc759fc5f0e1
    companion object {
        private const val DARK_STATUS = "darkempire78.opencalculator.DARK_STATUS"
        private const val KEY_VIBRATION_STATUS = "darkempire78.opencalculator.KEY_VIBRATION_STATUS"
        private const val KEY_HISTORY = "darkempire78.opencalculator.HISTORY"
    }

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    var darkMode = preferences.getInt(DARK_STATUS, -1)
        set(value) = preferences.edit().putInt(DARK_STATUS, value).apply()
    var vibrationMode = preferences.getBoolean(KEY_VIBRATION_STATUS, true)
        set(value) = preferences.edit().putBoolean(KEY_VIBRATION_STATUS, value).apply()
    var history = preferences.getString(KEY_HISTORY, null)
        set(value) = preferences.edit().putString(KEY_HISTORY, value).apply()

    fun getHistory(): MutableList<History> {
        val gson = Gson()
        return if (preferences.getString(KEY_HISTORY, null) != null) {
            gson.fromJson(history, Array<History>::class.java).asList().toMutableList()
        } else {
            mutableListOf<History>()
        }
    }

    fun saveHistory(context: Context, history: List<History>){
        val gson = Gson()
        MyPreferences(context).history = gson.toJson(history) // Convert to json
    }
}
