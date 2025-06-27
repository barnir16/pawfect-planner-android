package com.example.pawfectplanner.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
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
                val currentMode = LocaleHelper.getDarkModeSetting(requireContext())
                val nextMode = if (currentMode == AppCompatDelegate.MODE_NIGHT_YES)
                    AppCompatDelegate.MODE_NIGHT_NO
                else
                    AppCompatDelegate.MODE_NIGHT_YES
                LocaleHelper.setDarkModeSetting(requireContext(), nextMode)
                true
            }

        findPreference<Preference>("language")!!
            .setOnPreferenceClickListener {
                showLanguageDialog()
                true
            }
    }

    private fun showLanguageDialog() {
        val languages = resources.getStringArray(R.array.language_entries)
        val languageValues = resources.getStringArray(R.array.language_values)
        
        // Get current language to pre-select it
        val currentLanguage = LocaleHelper.getCurrentLanguage(requireContext())
        val currentIndex = languageValues.indexOf(currentLanguage)
        
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.pref_language))
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                val selectedLanguage = languageValues[which]
                if (selectedLanguage != currentLanguage) {
                    // Apply the new locale
                    LocaleHelper.setNewLocale(requireContext(), selectedLanguage)
                    
                    // Restart the activity to apply the language change
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                dialog.dismiss()
            }
            .show()
    }
}