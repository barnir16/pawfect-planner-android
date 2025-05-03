package com.example.pawfectplanner.ui.settings

import android.os.Bundle
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.pawfectplanner.R
import java.util.Locale

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<SwitchPreferenceCompat>("notifications")
            ?.setOnPreferenceChangeListener { _, _ ->
                true
            }

        findPreference<SwitchPreferenceCompat>("dark_mode")
            ?.setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                AppCompatDelegate.setDefaultNightMode(
                    if (enabled) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
                true
            }

        findPreference<ListPreference>("language")
            ?.setOnPreferenceChangeListener { _, newValue ->
                val localeCode = newValue as String
                val locale = Locale(localeCode)
                Locale.setDefault(locale)
                val config = Configuration(resources.configuration)
                config.setLocale(locale)
                requireActivity().resources.updateConfiguration(config, requireActivity().resources.displayMetrics)
                requireActivity().recreate()
                true
            }
    }
}