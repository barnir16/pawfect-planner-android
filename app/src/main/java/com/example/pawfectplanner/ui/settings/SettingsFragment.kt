package com.example.pawfectplanner.ui.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.pawfectplanner.R
import java.util.Locale

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findPreference<SwitchPreferenceCompat>("notifications")!!

        findPreference<Preference>("dark_mode")!!
            .setOnPreferenceClickListener {
                val current = AppCompatDelegate.getDefaultNightMode()
                val next = if (current == AppCompatDelegate.MODE_NIGHT_YES)
                    AppCompatDelegate.MODE_NIGHT_NO
                else
                    AppCompatDelegate.MODE_NIGHT_YES
                AppCompatDelegate.setDefaultNightMode(next)
                requireActivity().recreate()
                true
            }

        findPreference<ListPreference>("language")!!
            .setOnPreferenceChangeListener { _, newValue ->
                val locale = Locale(newValue as String)
                Locale.setDefault(locale)
                val config = resources.configuration
                config.setLocale(locale)
                @Suppress("DEPRECATION")
                requireActivity().resources.updateConfiguration(
                    config,
                    requireActivity().resources.displayMetrics
                )
                requireActivity().recreate()
                true
            }
    }
}
