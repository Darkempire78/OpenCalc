package com.darkempire78.opencalculator

import android.content.Intent
import android.content.res.Resources.Theme
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import java.util.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Themes
        val themes = Themes(this)
        themes.applyDayNightOverride()
        setTheme(themes.getTheme())

        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Change the status bar color
        if (MyPreferences(this).theme == 1) { // Amoled theme
            window.statusBarColor = ContextCompat.getColor(this, R.color.amoled_background_color)
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.background_color)
        }

        // back button
        findViewById<ImageView>(R.id.settings_back_button).setOnClickListener {
            finish()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val appLanguagePreference = findPreference<Preference>("darkempire78.opencalculator.APP_LANGUAGE")

            // Remove the app language button if you are using an Android version lower than v33 (Android 13)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                appLanguagePreference?.isVisible = false
            } else {
                // Display the current selected language
                appLanguagePreference?.summary = Locale.getDefault().displayLanguage
            }
            // Select app language button
            appLanguagePreference?.setOnPreferenceClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launchChangeAppLanguageIntent()
                }
                true
            }

            // Theme button
            val appThemePreference = findPreference<Preference>("darkempire78.opencalculator.APP_THEME_SELECTOR")

            appThemePreference?.summary = Themes(this.requireContext()).getThemeNameFromId(MyPreferences(this.requireContext()).theme)

            appThemePreference?.setOnPreferenceClickListener {
                Themes.openDialogThemeSelector(this.requireContext())
                true
            }

            // Font preference
            val appFontPreference = findPreference<Preference>("darkempire78.opencalculator.APP_FONT")
            appFontPreference?.setOnPreferenceChangeListener { _, newValue ->
                applyFont(newValue as String)
                true
            }
        }

        private fun applyFont(fontName: String) {
            val typeface = Typeface.createFromAsset(requireContext().assets, "font/$fontName.ttf")
            val rootView = requireActivity().findViewById<ViewGroup>(android.R.id.content)
            applyFontToViewGroup(rootView, typeface)
        }

        private fun applyFontToViewGroup(viewGroup: ViewGroup, typeface: Typeface) {
            for (i in 0 until viewGroup.childCount) {
                val child = viewGroup.getChildAt(i)
                if (child is ViewGroup) {
                    applyFontToViewGroup(child, typeface)
                } else if (child is TextView) {
                    child.typeface = typeface
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun launchChangeAppLanguageIntent() {
            try {
                Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                    startActivity(this)
                }
            } catch (e: Exception) {
                try {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", requireContext().packageName, null)
                        startActivity(this)
                    }
                } catch (e: Exception) {
                    println(e)
                }
            }
        }
    }
}
