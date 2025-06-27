package com.example.pawfectplanner.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.pawfectplanner.MainActivity
import com.example.pawfectplanner.R
import com.example.pawfectplanner.util.LocaleHelper

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
                true
            }

        findPreference<ListPreference>("language")!!
            .setOnPreferenceChangeListener { _, newValue ->
                val newLanguage = newValue as String
                // Apply the new locale
                LocaleHelper.setNewLocale(requireContext(), newLanguage)
                
                // Restart the activity to apply the language change
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                true
            }
    }
}