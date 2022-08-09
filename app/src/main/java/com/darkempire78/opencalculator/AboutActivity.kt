package com.darkempire78.opencalculator

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Themes
        Themes(this)

        when (MyPreferences(this).darkMode) {
            // System
            -1 -> {
                if (checkIfDarkModeIsEnabledByDefault()) {
                    setTheme(R.style.darkTheme)
                } else {
                    setTheme(R.style.AppTheme)
                }
            }
            // Light mode
            0 -> {
                setTheme(R.style.AppTheme)
            }
            // Dark mode
            1 -> {
                setTheme(R.style.darkTheme)
            }
            // amoled mode
            2 -> {
                setTheme(R.style.amoledTheme)
            }
            // Material You
            3 -> {
                if (checkIfDarkModeIsEnabledByDefault()) {
                    setTheme(R.style.materialYouDark)
                } else {
                    setTheme(R.style.materialYouLight)
                }

            }
            else -> {
                if (checkIfDarkModeIsEnabledByDefault()) {
                    setTheme(R.style.darkTheme)
                } else {
                    setTheme(R.style.AppTheme)
                }
            }
        }

        setContentView(R.layout.activity_main)

        // check the current selected theme
        Themes(this).checkTheme()
        setContentView(R.layout.activity_about)

        // Set app version
        val manager = this.packageManager
        val info = manager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES)
        about_app_version.text = "v${info.versionName}"

        // back button
        about_back_button.setOnClickListener() {
            finish()
        }

        // Github
        about_github_button.setOnClickListener() {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/Darkempire78/OpenCalc")
            )
            startActivity(browserIntent)
        }

        // Discord
        about_discord_button.setOnClickListener() {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://discord.com/invite/sPvJmY7mcV")
            )
            startActivity(browserIntent)
        }
    }

    private fun checkIfDarkModeIsEnabledByDefault(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            resources.configuration.isNightModeActive
        } else
            when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> true
                Configuration.UI_MODE_NIGHT_NO -> false
                Configuration.UI_MODE_NIGHT_UNDEFINED -> true
                else -> true
            }
}