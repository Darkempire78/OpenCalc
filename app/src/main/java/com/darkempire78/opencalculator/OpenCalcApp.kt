package com.darkempire78.opencalculator

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate.*

class OpenCalcApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // if the theme is overriding the system, the first creation doesn't work properly
        val forceDayNight = MyPreferences(this).forceDayNight
        if (forceDayNight != MODE_NIGHT_UNSPECIFIED && forceDayNight != MODE_NIGHT_FOLLOW_SYSTEM)
            setDefaultNightMode(forceDayNight)
    }
}