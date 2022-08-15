package com.darkempire78.opencalculator

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.darkempire78.opencalculator.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding

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

        binding = ActivityAboutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // check the current selected theme
        Themes(this).checkTheme()
        setContentView(R.layout.activity_about)

        // Set app version
        val versionName =  "v" + this.packageManager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES).versionName
        binding.aboutAppVersion.text = versionName

        // back button
        binding.aboutBackButton.setOnClickListener() {
            finish()
        }

        // Github
        binding.aboutGithubButton.setOnClickListener() {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/Darkempire78/OpenCalc")
            )
            startActivity(browserIntent)
        }

        // Discord
        binding.aboutDiscordButton.setOnClickListener() {
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
        } else {
            when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> true
                Configuration.UI_MODE_NIGHT_NO -> false
                Configuration.UI_MODE_NIGHT_UNDEFINED -> true
                else -> true
            }
        }
}
